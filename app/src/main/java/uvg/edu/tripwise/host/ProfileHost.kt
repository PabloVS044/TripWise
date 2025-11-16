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
import androidx.compose.runtime.rememberCoroutineScope
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
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UploadResponse
import uvg.edu.tripwise.network.UpdateUserRequest
import uvg.edu.tripwise.network.UpdatePasswordRequest

/* ====== Paleta / Tokens ====== */
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
                    onEditProfile = {} // ya no lo usamos, manejo local
                )
            }
        }
    }
}

// Modelo UI local
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

    // Vista / Edición
    var isEditing by remember { mutableStateOf(false) }

    // Bitmap solo para preview inmediata
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // URL de Cloudinary persistente (user.pfp)
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Estado de subida a Cloudinary
    var uploadInProgress by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // ====== ESTADO FORM EDITAR PERFIL ======
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

    // Cargar usuario
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

    // Cada vez que llega el usuario desde backend, tomar su pfp
    LaunchedEffect(user) {
        avatarUrl = user?.pfp
    }

    // Cuando entramos a modo edición, rellenar campos con datos actuales
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

    // Picker de imagen que además sube a Cloudinary y guarda pfp
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Preview local inmediata
            avatarBitmap = decodeBitmapFromUri(context, uri)

            // Subir a Cloudinary vía backend
            scope.launch {
                uploadInProgress = true
                uploadError = null
                try {
                    val response: UploadResponse = uploadImageToCloudinary(context, uri)

                    // Guardar URL en backend como pfp del usuario
                    if (!userId.isNullOrBlank()) {
                        try {
                            RetrofitInstance.api.updateUser(
                                userId,
                                UpdateUserRequest(pfp = response.url)
                            )
                            // refrescar usuario
                            val refreshed = withContext(Dispatchers.IO) {
                                repo.getUserById(userId)
                            }
                            user = refreshed
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Actualizar URL local para que se vea sin salir de la pantalla
                    avatarUrl = response.url

                    Toast.makeText(
                        context,
                        "Imagen subida correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    uploadError = "Error al subir imagen: ${e.message}"
                    Toast.makeText(
                        context,
                        "Error al subir imagen",
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
                            // Si estamos editando, salir de la edición
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

            // ===== Avatar + cámara (igual en ambos modos) =====
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(140.dp)) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = SelectedBg,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when {
                                // 1) Preferir URL de Cloudinary (persistente)
                                !avatarUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = stringResource(R.string.cd_profile_photo),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                // 2) Si no hay URL, mostrar bitmap local
                                avatarBitmap != null -> {
                                    Image(
                                        bitmap = avatarBitmap!!.asImageBitmap(),
                                        contentDescription = stringResource(R.string.cd_profile_photo),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                // 3) Placeholder
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
                    // Botón cámara
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
                // ================= MODO VISTA PERFIL =================
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
                // Ya no mostramos teléfono ni ubicación
                InfoRow(
                    icon = Icons.Default.Today,
                    text = stringResource(
                        R.string.member_since,
                        formatMemberSince(profile.memberSinceIso)
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
                // ================= MODO EDICIÓN PERFIL =================
                Text(
                    text = "Edita tu información",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Nombre completo
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(Corner)
                )

                // Correo electrónico
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Correo Electrónico") },
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

                // Contraseña actual
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña Actual") },
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

                // Nueva contraseña
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
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

                // Confirmar nueva contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Nueva Contraseña") },
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
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (userId.isNullOrBlank()) {
                                    formError = "No hay usuario en sesión."
                                    return@launch
                                }

                                // Validaciones básicas
                                if (editName.isBlank() || editEmail.isBlank()) {
                                    formError = "Nombre y correo no pueden estar vacíos."
                                    return@launch
                                }

                                if ((newPassword.isNotBlank() || confirmPassword.isNotBlank() || currentPassword.isNotBlank())
                                    && newPassword != confirmPassword
                                ) {
                                    formError = "La nueva contraseña y su confirmación no coinciden."
                                    return@launch
                                }

                                isSaving = true
                                formError = null
                                try {
                                    // 1) Actualizar nombre / email
                                    withContext(Dispatchers.IO) {
                                        RetrofitInstance.api.updateUser(
                                            userId,
                                            UpdateUserRequest(
                                                name = editName,
                                                email = editEmail
                                            )
                                        )
                                    }

                                    // refrescar usuario
                                    val refreshed = withContext(Dispatchers.IO) {
                                        repo.getUserById(userId)
                                    }
                                    user = refreshed

                                    // 2) Actualizar contraseña (solo si el usuario llenó campos)
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
                                                "La contraseña actual es incorrecta."
                                            } else {
                                                "Error al actualizar la contraseña."
                                            }
                                            return@launch
                                        }
                                    }

                                    Toast.makeText(
                                        context,
                                        "Datos actualizados correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Limpiar y salir de edición
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    isEditing = false
                                } catch (e: Exception) {
                                    formError = "Error al guardar cambios: ${e.message}"
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
                        Text(if (isSaving) "Guardando..." else "Guardar cambios")
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

private fun formatMemberSince(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val y = iso.take(7)
        val (yy, mm) = y.split("-")
        val month = when (mm) {
            "01" -> "Enero"
            "02" -> "Febrero"
            "03" -> "Marzo"
            "04" -> "Abril"
            "05" -> "Mayo"
            "06" -> "Junio"
            "07" -> "Julio"
            "08" -> "Agosto"
            "09" -> "Septiembre"
            "10" -> "Octubre"
            "11" -> "Noviembre"
            "12" -> "Diciembre"
            else -> mm
        }
        "$month $yy"
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

/**
 * Sube una imagen seleccionada al backend (/upload), que a su vez la manda a Cloudinary.
 * Devuelve el UploadResponse con la URL resultante.
 */
suspend fun uploadImageToCloudinary(
    context: Context,
    uri: Uri
): UploadResponse {
    return withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        // Leer bytes del Uri
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el archivo")
        val bytes = inputStream.use { it.readBytes() }

        // Mime type
        val mimeType = contentResolver.getType(uri) ?: "image/*"

        // RequestBody
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        // MultipartBody.Part — el nombre "imagen" debe coincidir con upload.single("imagen")
        val part = MultipartBody.Part.createFormData(
            name = "imagen",
            filename = "profile_${System.currentTimeMillis()}.jpg",
            body = requestBody
        )

        // Llamada real al endpoint RetrofitInstance.api.uploadImage
        RetrofitInstance.api.uploadImage(part)
    }
}
