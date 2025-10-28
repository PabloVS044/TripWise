package uvg.edu.tripwise.profile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions // ++ NUEVO ++
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Visibility // ++ NUEVO ++
import androidx.compose.material.icons.filled.VisibilityOff // ++ NUEVO ++
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType // ++ NUEVO ++
import androidx.compose.ui.text.input.PasswordVisualTransformation // ++ NUEVO ++
import androidx.compose.ui.text.input.VisualTransformation // ++ NUEVO ++
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.components.AppBottomNavBar
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.viewModel.ProfileViewModel

class ProfileActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}

data class InterestItem(
    val name: String,
    val icon: ImageVector,
    val key: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedInterests by remember { mutableStateOf(emptySet<String>()) }

    // --- INICIO DE CAMBIOS ---
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    // --- FIN DE CAMBIOS ---

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(user) {
        user?.let {
            name = it.name
            email = it.email
            selectedInterests = it.interests?.toSet() ?: emptySet()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            snackbarHostState.showSnackbar("¡Perfil actualizado con éxito!")
        }
    }

    val availableInterests = listOf(
        InterestItem(stringResource(R.string.adventure), Icons.Default.Hiking, "aventura"),
        InterestItem(stringResource(R.string.beach), Icons.Default.BeachAccess, "playa"),
        InterestItem(stringResource(R.string.mountain), Icons.Default.Terrain, "montaña"),
        InterestItem(stringResource(R.string.city), Icons.Default.LocationCity, "ciudad"),
        InterestItem(stringResource(R.string.culture), Icons.Default.Museum, "cultura"),
        InterestItem(stringResource(R.string.gastronomy), Icons.Default.Restaurant, "gastronomia"),
        InterestItem(stringResource(R.string.history), Icons.Default.Castle, "historia"),
        InterestItem(stringResource(R.string.nature), Icons.Default.Park, "naturaleza"),
        InterestItem(stringResource(R.string.relax), Icons.Default.Spa, "relax"),
        InterestItem(stringResource(R.string.photography), Icons.Default.CameraAlt, "fotografia")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = Color(0xFF0066CC)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            AppBottomNavBar(currentScreen = "Profile")
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (uiState.isLoading && user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (user != null) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ... (Sección de Imagen y Nombre de Usuario - sin cambios) ...
                item {
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = selectedImageUri ?: user.pfp
                            ),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Edita tu información", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(24.dp))
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // --- INICIO DE CAMBIOS (Campos de Contraseña) ---
                item {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña Actual") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (currentPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(imageVector = image, "toggle password visibility")
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (newPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(imageVector = image, "toggle password visibility")
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, "toggle password visibility")
                            }
                        }
                    )
                    Spacer(Modifier.height(32.dp))
                }
                // --- FIN DE CAMBIOS ---

                // ... (Sección de Intereses y Botón de Guardar - sin cambios) ...
                item {
                    Text("Tus Intereses", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(16.dp))
                }

                items(availableInterests) { interest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = interest.icon, contentDescription = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = interest.name)
                        }
                        Switch(
                            checked = selectedInterests.contains(interest.key),
                            onCheckedChange = {
                                val newInterests = if (selectedInterests.contains(interest.key)) {
                                    selectedInterests - interest.key
                                } else {
                                    selectedInterests + interest.key
                                }
                                selectedInterests = newInterests
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                name = name,
                                email = email,
                                profileImageUri = selectedImageUri,
                                interests = selectedInterests.toList(),
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                        enabled = !uiState.isLoading && !uiState.isImageUploading
                    ) {
                        if (uiState.isLoading || uiState.isImageUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Guardar Cambios", color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}