package uvg.edu.tripwise.host

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.data.model.User
import uvg.edu.tripwise.data.repository.UserRepository
import uvg.edu.tripwise.ui.components.LogoAppTopBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme

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
        val sessId = prefs.getString(SessionKeys.USER_ID, prefs.getString(SessionKeys.LEGACY_USER_ID, null))
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

// Modelo UI local
data class HostProfileUi(
    val name: String,
    val email: String,
    val location: String,
    val phone: String,
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
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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

    val profile = remember(user) {
        HostProfileUi(
            name = user?.name ?: "—",
            email = user?.email ?: "—",
            location = "—",
            phone = "—",
            memberSinceIso = "—"
        )
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) avatarBitmap = decodeBitmapFromUri(context, uri)
    }

    Scaffold(
        topBar = {
            Box {
                LogoAppTopBar(onLogout = { })
                IconButton(
                    onClick = onBack,
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
            // ===== Avatar + cámara (overlay en esquina inferior derecha) =====
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(140.dp)) {
                    // Avatar
                    Surface(shape = CircleShape, color = SelectedBg, modifier = Modifier.matchParentSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!.asImageBitmap(),
                                    contentDescription = stringResource(R.string.cd_profile_photo),
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = stringResource(R.string.cd_profile_placeholder),
                                    tint = Color.Black,
                                    modifier = Modifier.size(64.dp)
                                )
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

            Spacer(Modifier.height(20.dp))
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
            InfoRow(icon = Icons.Default.Email,     text = profile.email.ifBlank { stringResource(R.string.placeholder_em_dash) }) {
                if (profile.email.isNotBlank() && profile.email != "—") {
                    val i = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:${profile.email}") }
                    context.startActivity(i)
                }
            }
            InfoRow(icon = Icons.Default.LocationOn, text = profile.location.ifBlank { stringResource(R.string.placeholder_em_dash) })
            InfoRow(icon = Icons.Default.Phone,      text = profile.phone.ifBlank { stringResource(R.string.placeholder_em_dash) })
            InfoRow(
                icon = Icons.Default.Today,
                text = stringResource(R.string.member_since, formatMemberSince(profile.memberSinceIso))
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onEditProfile,
                colors = ButtonDefaults.buttonColors(containerColor = SelectedBlue, contentColor = Color.White),
                shape = RoundedCornerShape(Corner)
            ) { Text(stringResource(R.string.action_edit_profile)) }
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
        Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(18.dp))
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
            "01" -> "Enero"; "02" -> "Febrero"; "03" -> "Marzo"; "04" -> "Abril"
            "05" -> "Mayo"; "06" -> "Junio"; "07" -> "Julio"; "08" -> "Agosto"
            "09" -> "Septiembre"; "10" -> "Octubre"; "11" -> "Noviembre"; "12" -> "Diciembre"
            else -> mm
        }
        "$month $yy"
    } catch (_: Exception) { iso }
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
    } catch (_: Throwable) { null }
}
