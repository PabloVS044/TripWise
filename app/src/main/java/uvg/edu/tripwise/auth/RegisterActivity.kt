package uvg.edu.tripwise.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.auth.steps.*
import uvg.edu.tripwise.network.*
import uvg.edu.tripwise.ui.theme.TripWiseTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                RegisterScreen(
                    onRegisterSuccess = {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onBackToLogin = {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf<String?>(null) }

    var propertyName by remember { mutableStateOf("") }
    var propertyDescription by remember { mutableStateOf("") }
    var propertyLocation by remember { mutableStateOf("") }
    var pricePerNight by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("") }
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }

    var selectedInterests by remember { mutableStateOf(setOf<String>()) }

    fun canProceedFromCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> {
                fullName.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        password == confirmPassword
            }
            1 -> selectedRole != null
            2 -> {
                if (selectedRole == "owner") {
                    propertyName.isNotBlank() &&
                            propertyDescription.isNotBlank() &&
                            propertyLocation.isNotBlank() &&
                            pricePerNight.isNotBlank() &&
                            capacity.isNotBlank() &&
                            propertyType.isNotBlank()
                } else true
            }
            3 -> selectedInterests.isNotEmpty()
            else -> false
        }
    }

    fun getMaxSteps(): Int {
        return if (selectedRole == "owner") 4 else 3
    }

    fun mapPropertyType(frontendType: String): String {
        return when (frontendType) {
            "casa" -> "house"
            "apartamento" -> "apartment"
            "cabana" -> "cottage"
            "hotel" -> "villa"
            else -> "house"
        }
    }

    fun mapAmenities(frontendAmenities: Set<String>): List<String> {
        return frontendAmenities.map {
            when (it) {
                "wifi" -> "WiFi gratuito"
                "piscina" -> "Piscina"
                "cocina" -> "Cocina"
                "estacionamiento" -> "Estacionamiento"
                "aire_acondicionado" -> "Aire acondicionado"
                "tv" -> "TV"
                "lavadora" -> "Lavadora"
                "balcon" -> "Balcón"
                else -> it
            }
        }
    }

    fun registerUser() {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val request = CreateUserRequest(
                    name = fullName,
                    email = email,
                    password = password,
                    pfp = null,
                    role = selectedRole,
                    interests = selectedInterests.toList()
                )

                Log.d("RegisterActivity", "Enviando request de usuario: $request")
                val response = RetrofitInstance.api.createUser(request)

                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("RegisterActivity", "Usuario creado exitosamente: ${user?.id}")

                    if (selectedRole == "owner" && propertyName.isNotBlank() && user != null) {
                        val price = pricePerNight.toDoubleOrNull()
                        val cap = capacity.toIntOrNull()

                        if (price == null || price <= 0) {
                            snackbarHostState.showSnackbar("El precio por noche debe ser un número válido mayor a 0")
                            return@launch
                        }

                        if (cap == null || cap <= 0) {
                            snackbarHostState.showSnackbar("La capacidad debe ser un número válido mayor a 0")
                            return@launch
                        }

                        val propertyRequest = CreatePropertyRequest(
                            name = propertyName,
                            description = propertyDescription,
                            location = propertyLocation,
                            pricePerNight = price,
                            capacity = cap,
                            pictures = listOf(
                                "https://cf.bstatic.com/xdata/images/hotel/max1024x768/700469152.jpg?k=81f660295c981edce0f9e6ddd543a99ff966256c092b0a01cde4ee8125f382c0&o=",
                                "https://cf.bstatic.com/xdata/images/hotel/max500/700469161.jpg?k=b58010e414155ba670315e4236e0e9307fd55b2003ecfa49f5af83509ac89468&o=",
                                "https://cf.bstatic.com/xdata/images/hotel/max300/700469173.jpg?k=13697e4cd76f89696a2805452ed778c4cfca2e223bb64cfd21229eb1dacf725c&o="
                            ),
                            amenities = mapAmenities(selectedAmenities),
                            propertyType = mapPropertyType(propertyType),
                            owner = user.id,
                            approved = "pending",
                            latitude = 14.5984,
                            longitude = -90.5155
                        )

                        Log.d("RegisterActivity", "Enviando request de propiedad: $propertyRequest")
                        val propertyResponse = RetrofitInstance.api.createProperty(propertyRequest)

                        if (propertyResponse.isSuccessful) {
                            Log.d("RegisterActivity", "Propiedad creada exitosamente")
                        } else {
                            val errorBody = propertyResponse.errorBody()?.string()
                            Log.e("RegisterActivity", "Error al crear propiedad: ${propertyResponse.code()} - $errorBody")
                            snackbarHostState.showSnackbar("Usuario creado, pero error al crear propiedad: ${propertyResponse.code()}")
                            return@launch
                        }
                    }

                    snackbarHostState.showSnackbar("Registro exitoso")
                    onRegisterSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterActivity", "Error al registrar usuario: ${response.code()} - $errorBody")
                    errorMessage = "Error al registrar usuario: ${response.code()}"
                    snackbarHostState.showSnackbar(errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                Log.e("RegisterActivity", "Register error", e)
                snackbarHostState.showSnackbar(errorMessage!!)
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentStep) {
            0 -> {
                BasicInfoScreen(
                    fullName = fullName,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    onFullNameChange = { fullName = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            1 -> {
                RoleSelectionScreen(
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            2 -> {
                if (selectedRole == "owner") {
                    PropertySetupScreen(
                        propertyName = propertyName,
                        propertyDescription = propertyDescription,
                        propertyLocation = propertyLocation,
                        pricePerNight = pricePerNight,
                        capacity = capacity,
                        propertyType = propertyType,
                        selectedAmenities = selectedAmenities,
                        onPropertyNameChange = { propertyName = it },
                        onPropertyDescriptionChange = { propertyDescription = it },
                        onPropertyLocationChange = { propertyLocation = it },
                        onPricePerNightChange = { pricePerNight = it },
                        onCapacityChange = { capacity = it },
                        onPropertyTypeChange = { propertyType = it },
                        onSelectedAmenitiesChange = { selectedAmenities = it },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    InterestsScreen(
                        selectedInterests = selectedInterests,
                        onInterestsChanged = { selectedInterests = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            3 -> {
                if (selectedRole == "owner") {
                    InterestsScreen(
                        selectedInterests = selectedInterests,
                        onInterestsChanged = { selectedInterests = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / getMaxSteps(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    enabled = !isLoading
                ) {
                    Text("Atrás")
                }
            } else {
                OutlinedButton(
                    onClick = onBackToLogin,
                    enabled = !isLoading
                ) {
                    Text("Iniciar Sesión")
                }
            }

            Button(
                onClick = {
                    if (currentStep == getMaxSteps() - 1) {
                        registerUser()
                    } else {
                        currentStep++
                    }
                },
                enabled = canProceedFromCurrentStep() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (currentStep == getMaxSteps() - 1) "Registrar" else "Siguiente"
                    )
                }
            }
        }

        // Snackbar para mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}