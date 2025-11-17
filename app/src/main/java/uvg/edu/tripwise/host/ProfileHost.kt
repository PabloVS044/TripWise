package uvg.edu.tripwise.host

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UpdatePasswordRequest
import uvg.edu.tripwise.network.UpdateUserRequest
import uvg.edu.tripwise.network.UploadResponse
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

private val BrandBlue    = Color(0xFF1F47B2)
private val SelectedBlue = Color(0xFF2F5BFF)
private val SelectedBg   = Color(0xFFEFF4FF)
private val PageBg       = Color(0xFFF7F7FB)
private val GreyText     = Color(0xFF6B7280)
private val Corner       = 12.dp

const val EXTRA_USER_ID = "extra_user_id"

class ProfileHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val act = this

        val intentUserId = intent?.getStringExtra(EXTRA_USER_ID)
        val prefs  = getSharedPreferences(SessionKeys.PREFS, MODE_PRIVATE)
        val sessId = prefs.getString(
            SessionKeys.USER_ID,
            prefs.getString(SessionKeys.LEGACY_USER_ID, null)
        )
        val resolvedUserId = intentUserId ?: sessId

        setContent {
            TripWiseTheme {
                ProfileHostScreen(
                    userId = resolvedUserId,
                    onBack = { act.onBackPressedDispatcher.onBackPressed() },
                    onEditProfile = {}
                )
            }
        }
    }
}

data class HostProfileUi(
    val name: String,
    val email: String,
    val memberSinceIso: String
)

@Composable
fun ProfileHostScreen(
    userId: String?,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    repo: UserRepository = UserRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var uploadInProgress by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            try {
                loading = true
                user = repo.getUserById(userId)
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        } else {
            error = context.getString(R.string.error_no_user_session)
        }
    }
    LaunchedEffect(user) {
        avatarUrl = user?.pfp
    }
    LaunchedEffect(isEditing, user) {
        if (isEditing && user != null) {
            editName = user?.name.orEmpty()
            editEmail = user?.email.orEmpty()
            formError = null
        }
    }

    val profile = remember(user) {
        HostProfileUi(
            name = user?.name ?: "—",
            email = user?.email ?: "—",
            memberSinceIso = user?.createdAt ?: "—"
        )
    }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarBitmap = decodeBitmapFromUri(context, uri)
            scope.launch {
                uploadInProgress = true
                uploadError = null
                try {
                    val response: UploadResponse = uploadImageToCloudinary(context, uri)
                    if (!userId.isNullOrBlank()) {
                        try {
                            RetrofitInstance.api.updateUser(
                                userId,
                                UpdateUserRequest(pfp = response.url)
                            )
                            val refreshed = withContext(Dispatchers.IO) {
                                repo.getUserById(userId)
                            }
                            user = refreshed
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    avatarUrl = response.url

                    Toast.makeText(
                        context,
                        context.getString(R.string.success_image_uploaded),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    uploadError = context.getString(
                        R.string.error_uploading_image_detail,
                        e.message ?: ""
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_uploading_image),
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    uploadInProgress = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                LogoAppTopBar(onLogout = { })
                IconButton(
                    onClick = {
                        if (isEditing) {
                            isEditing = false
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp, top = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = BrandBlue
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(PageBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(140.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = SelectedBg,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when {
                                !avatarUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = stringResource(R.string.cd_profile_photo),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                avatarBitmap != null -> {
                                    Image(
                                        bitmap = avatarBitmap!!.asImageBitmap(),
                                        contentDescription = stringResource(R.string.cd_profile_photo),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = stringResource(R.string.cd_profile_placeholder),
                                        tint = Color.Black,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = SelectedBg,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(6.dp, 6.dp)
                            .size(40.dp)
                            .clickable { imagePicker.launch("image/*") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.cd_change_photo),
                                tint = SelectedBlue
                            )
                        }
                    }
                }
            }

            if (uploadInProgress) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally)
                )
            }

            if (uploadError != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = uploadError ?: "",
                    color = Color(0xFFE11D48),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            if (!isEditing) {
                Text(
                    when {
                        loading -> stringResource(R.string.loading_ellipsis)
                        error != null -> stringResource(R.string.label_error)
                        else -> profile.name
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )

                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error!!, color = Color(0xFFE11D48), fontSize = 12.sp)
                }

                Spacer(Modifier.height(12.dp))
                InfoRow(
                    icon = Icons.Default.Email,
                    text = profile.email.ifBlank { stringResource(R.string.placeholder_em_dash) }
                ) {
                    if (profile.email.isNotBlank() && profile.email != "—") {
                        val i = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${profile.email}")
                        }
                        context.startActivity(i)
                    }
                }
                InfoRow(
                    icon = Icons.Default.Today,
                    text = stringResource(
                        R.string.member_since,
                        formatMemberSince(context, profile.memberSinceIso)
                    )
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { isEditing = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SelectedBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(Corner)
                ) { Text(stringResource(R.string.action_edit_profile)) }
            } else {
                Text(
                    text = stringResource(R.string.edit_profile_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.label_full_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text(stringResource(R.string.label_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(stringResource(R.string.label_current_password)) },
                    singleLine = true,
                    visualTransformation = if (showCurrentPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.label_new_password)) },
                    singleLine = true,
                    visualTransformation = if (showNewPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.label_confirm_new_password)) },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )

                if (formError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formError ?: "",
                        color = Color(0xFFE11D48),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Corner)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (userId.isNullOrBlank()) {
                                    formError = context.getString(R.string.error_no_user_session)
                                    return@launch
                                }
                                if (editName.isBlank() || editEmail.isBlank()) {
                                    formError = context.getString(R.string.error_name_email_required)
                                    return@launch
                                }

                                if ((newPassword.isNotBlank() || confirmPassword.isNotBlank() || currentPassword.isNotBlank())
                                    && newPassword != confirmPassword
                                ) {
                                    formError = context.getString(R.string.error_password_mismatch)
                                    return@launch
                                }

                                isSaving = true
                                formError = null
                                try {
                                    withContext(Dispatchers.IO) {
                                        RetrofitInstance.api.updateUser(
                                            userId,
                                            UpdateUserRequest(
                                                name = editName,
                                                email = editEmail
                                            )
                                        )
                                    }
                                    val refreshed = withContext(Dispatchers.IO) {
                                        repo.getUserById(userId)
                                    }
                                    user = refreshed
                                    if (newPassword.isNotBlank() || currentPassword.isNotBlank() || confirmPassword.isNotBlank()) {
                                        val resp = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.updatePassword(
                                                userId,
                                                UpdatePasswordRequest(
                                                    currentPassword = currentPassword,
                                                    newPassword = newPassword
                                                )
                                            )
                                        }
                                        if (!resp.isSuccessful) {
                                            formError = if (resp.code() == 401) {
                                                context.getString(R.string.error_current_password_incorrect)
                                            } else {
                                                context.getString(R.string.error_update_password_generic)
                                            }
                                            return@launch
                                        }
                                    }

                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.success_profile_updated),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    isEditing = false
                                } catch (e: Exception) {
                                    formError = context.getString(
                                        R.string.error_saving_changes_detail,
                                        e.message ?: ""
                                    )
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SelectedBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(Corner)
                    ) {
                        val label = if (isSaving) {
                            stringResource(R.string.action_saving_ellipsis)
                        } else {
                            stringResource(R.string.action_save_changes)
                        }
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(text = text, color = GreyText)
    }
}

private fun formatMemberSince(context: Context, iso: String?): String {
    if (iso.isNullOrBlank()) return context.getString(R.string.placeholder_em_dash)
    return try {
        val y = iso.take(7)
        val (yy, mm) = y.split("-")
        val monthRes = when (mm) {
            "01" -> R.string.month_january
            "02" -> R.string.month_february
            "03" -> R.string.month_march
            "04" -> R.string.month_april
            "05" -> R.string.month_may
            "06" -> R.string.month_june
            "07" -> R.string.month_july
            "08" -> R.string.month_august
            "09" -> R.string.month_september
            "10" -> R.string.month_october
            "11" -> R.string.month_november
            "12" -> R.string.month_december
            else -> null
        }
        val monthName = monthRes?.let { context.getString(it) } ?: mm
        "$monthName $yy"
    } catch (_: Exception) {
        iso
    }
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (_: Throwable) {
        null
    }
}

suspend fun uploadImageToCloudinary(
    context: Context,
    uri: Uri
): UploadResponse {
    return withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el archivo")
        val bytes = inputStream.use { it.readBytes() }
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        val part = MultipartBody.Part.createFormData(
            name = "imagen",
            filename = "profile_${System.currentTimeMillis()}.jpg",
            body = requestBody
        )

        RetrofitInstance.api.uploadImage(part)
    }
}
