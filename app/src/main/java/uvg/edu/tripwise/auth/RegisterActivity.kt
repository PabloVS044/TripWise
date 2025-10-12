package uvg.edu.tripwise.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
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

    val wifiText = stringResource(R.string.wifi)
    val poolText = stringResource(R.string.pool)
    val kitchenText = stringResource(R.string.kitchen)
    val parkingText = stringResource(R.string.parking)
    val airConditioningText = stringResource(R.string.air_conditioning)
    val tvText = stringResource(R.string.tv)
    val washingMachineText = stringResource(R.string.washing_machine)
    val balconyText = stringResource(R.string.balcony)
    val invalidPricePerNightMsg = stringResource(R.string.invalid_price_per_night)
    val invalidCapacityMsg = stringResource(R.string.invalid_capacity)
    val userCreatedPropertyErrorMsg = stringResource(R.string.user_created_property_error)
    val registrationSuccessfulMsg = stringResource(R.string.registration_successful)
    val registrationErrorMsg = stringResource(R.string.registration_error)
    val errorGenericMsg = stringResource(R.string.error_generic)

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
        return if (selectedRole == "owner") 3 else 3
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
                "wifi" -> wifiText
                "piscina" -> poolText
                "cocina" -> kitchenText
                "estacionamiento" -> parkingText
                "aire_acondicionado" -> airConditioningText
                "tv" -> tvText
                "lavadora" -> washingMachineText
                "balcon" -> balconyText
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
                            snackbarHostState.showSnackbar(invalidPricePerNightMsg)
                            return@launch
                        }

                        if (cap == null || cap <= 0) {
                            snackbarHostState.showSnackbar(invalidCapacityMsg)
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
                            snackbarHostState.showSnackbar("$userCreatedPropertyErrorMsg ${propertyResponse.code()}")
                            return@launch
                        }
                    }

                    snackbarHostState.showSnackbar(registrationSuccessfulMsg)
                    onRegisterSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterActivity", "Error al registrar usuario: ${response.code()} - $errorBody")
                    errorMessage = "$registrationErrorMsg ${response.code()}"
                    snackbarHostState.showSnackbar(errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "$errorGenericMsg ${e.message ?: ""}"
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
                    totalSteps = getMaxSteps(),
                    modifier = Modifier.fillMaxSize()
                )
            }

            1 -> {
                RoleSelectionScreen(
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it },
                    totalSteps = getMaxSteps(),
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
                        totalSteps = getMaxSteps(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    InterestsScreen(
                        selectedInterests = selectedInterests,
                        onInterestsChanged = { selectedInterests = it },
                        totalSteps = getMaxSteps(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

        }

        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / getMaxSteps(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = Color(0xFF2563EB),
            trackColor = Color(0xFFE5E7EB)
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
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF2563EB))
                ) {
                    Text(
                        text = stringResource(R.string.back),
                        color = Color(0xFF2563EB),
                        fontSize = 16.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onBackToLogin,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF2563EB))
                ) {
                    Text(
                        text = stringResource(R.string.sign_in),
                        color = Color(0xFF2563EB),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (currentStep == getMaxSteps() - 1) {
                        registerUser()
                    } else {
                        currentStep++
                    }
                },
                enabled = canProceedFromCurrentStep() && !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (currentStep == getMaxSteps() - 1) stringResource(R.string.register) else stringResource(R.string.next),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = Color(0xFF374151),
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}