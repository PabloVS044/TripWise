package uvg.edu.tripwise.auth

import android.content.Context.MODE_PRIVATE
import uvg.edu.tripwise.MainActivity
import uvg.edu.tripwise.admin.DashboardActivity
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.Login
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uvg.edu.tripwise.R
import uvg.edu.tripwise.components.TripWiseLoadingOverlay
import uvg.edu.tripwise.discover.DiscoverActivity
import uvg.edu.tripwise.host.PropertiesHostActivity
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.ui.components.AppLogoHeader
import java.io.IOException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Verificar si el usuario ya está logueado
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            Log.d("LoginActivity", "Usuario ya logueado: ID=${sessionManager.getUserId()}, Rol=${sessionManager.getUserRole()}")
            val role = sessionManager.getUserRole()?.lowercase() ?: ""
            val intent = when (role) {
                "admin" -> Intent(this, DashboardActivity::class.java)
                "user" -> Intent(this, DiscoverActivity::class.java)
                "owner" -> Intent(this, PropertiesHostActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            TripWiseTheme {
                LoginScreen(
                    onBackToHome = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onLoginSuccess = { role ->
                        val intent = when (role.lowercase()) {
                            "admin" -> Intent(this, DashboardActivity::class.java)
                            "user" -> Intent(this, DiscoverActivity::class.java)
                            "owner" -> Intent(this, PropertiesHostActivity::class.java)
                            else -> Intent(this, MainActivity::class.java)
                        }
                            .apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        startActivity(intent)
                        finish()
                    },
                    onSignUpClick = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    },
                    onForgotPasswordClick = {
                        val intent = Intent(this, ForgotPasswordActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackToHome: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val pleaseFillAllFieldsMsg = stringResource(R.string.please_fill_all_fields)
    val userNotFoundMsg = stringResource(R.string.user_not_found)
    val incorrectCredentialsMsg = stringResource(R.string.incorrect_credentials)
    val serverErrorMsg = stringResource(R.string.server_error)
    val connectionErrorMsg = stringResource(R.string.connection_error)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackToHome) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back_to_home),
                                tint = Color.Black
                            )
                        }
                    }

                    TextButton(onClick = onSignUpClick) {
                        Text(
                            text = stringResource(R.string.sign_up),
                            color = Color(0xFF2563EB),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AppLogoHeader(
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.sign_in_subtitle),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(25.dp)) }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sign_in),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Text(
                        text = stringResource(R.string.email),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color(0xFFEBF4FF),
                            unfocusedContainerColor = Color(0xFFEBF4FF)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Text(
                        text = stringResource(R.string.password),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) stringResource(R.string.show_password) else stringResource(R.string.hide_password)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color(0xFFEBF4FF),
                            unfocusedContainerColor = Color(0xFFEBF4FF)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2563EB)
                                ),
                                enabled = !isLoading
                            )
                            Text(
                                text = stringResource(R.string.remember_me),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = stringResource(R.string.forgot_password),
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                onForgotPasswordClick()
                            }
                        )
                    }

                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFDC2626),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = pleaseFillAllFieldsMsg
                                return@Button
                            }

                            errorMessage = null
                            isLoading = true

                            coroutineScope.launch {
                                try {
                                    Log.d("LoginActivity", "Intentando login con email: $email")
                                    val response = RetrofitInstance.api.login(Login(email, password))
                                    Log.d("LoginActivity", "Respuesta del servidor: ${response.code()}")

                                    if (response.isSuccessful) {
                                        val loginResponse = response.body()

                                        if (loginResponse != null) {
                                            val userId = loginResponse._id
                                            val token = loginResponse.token
                                            val userEmail = loginResponse.email
                                            val role = loginResponse.role

                                            Log.d("LoginActivity", "Login exitoso: ID=$userId, Rol=$role")
                                            sessionManager.saveUserDetails(token, userId, userEmail, role)

                                            // Guardar también en SharedPreferences para compatibilidad
                                            val prefs = context.getSharedPreferences("auth", MODE_PRIVATE)
                                            prefs.edit()
                                                .putString("USER_ID", userId)
                                                .putString("user_id", userId)
                                                .putString("TOKEN", token)
                                                .putString("ROLE", role)
                                                .apply()

                                            // Pequeño delay para UX
                                            kotlinx.coroutines.delay(800)
                                            onLoginSuccess(role)
                                        } else {
                                            Log.e("LoginActivity", "Login failed: Response body is null")
                                            errorMessage = serverErrorMsg
                                            isLoading = false
                                        }
                                    } else {
                                        when (response.code()) {
                                            404 -> errorMessage = userNotFoundMsg
                                            401 -> errorMessage = incorrectCredentialsMsg
                                            else -> {
                                                Log.e("LoginActivity", "Error HTTP: ${response.code()}")
                                                errorMessage = "$serverErrorMsg ${response.code()}"
                                            }
                                        }
                                        isLoading = false
                                    }
                                } catch (e: HttpException) {
                                    Log.e("LoginActivity", "Error HTTP: ${e.message()}", e)
                                    errorMessage = serverErrorMsg
                                    isLoading = false
                                } catch (e: IOException) {
                                    Log.e("LoginActivity", "Error de conexión: ${e.message}", e)
                                    errorMessage = connectionErrorMsg
                                    isLoading = false
                                } catch (e: Exception) {
                                    Log.e("LoginActivity", "Error inesperado: ${e.message}", e)
                                    errorMessage = serverErrorMsg
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.sign_in),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.or_continue_with),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialLoginButton(
                            text = stringResource(R.string.google),
                            icon = Icons.Default.Email,
                            onClick = { /* TODO: Google login */ },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        )

                        SocialLoginButton(
                            text = stringResource(R.string.facebook),
                            icon = Icons.Default.Facebook,
                            onClick = { /* TODO: Facebook login */ },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_account),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = stringResource(R.string.sign_up_here),
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                onSignUpClick()
                            }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            TripWiseLoadingOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                message = "Iniciando sesión..."
            )
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}