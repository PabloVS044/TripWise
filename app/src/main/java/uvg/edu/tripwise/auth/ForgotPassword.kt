package uvg.edu.tripwise.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.theme.TripWiseTheme
import uvg.edu.tripwise.ui.components.AppLogoHeader

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripWiseTheme {
                ForgotPasswordScreen(
                    onBackToLogin = {
                        finish()
                    },
                    onSignInClick = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isEmailSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Obtener las strings aquí, en el contexto composable
    val pleaseEnterEmailMsg = stringResource(R.string.please_enter_email)
    val pleaseEnterValidEmailMsg = stringResource(R.string.please_enter_valid_email)
    val errorSendingEmailMsg = stringResource(R.string.error_sending_email)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 120.dp), // Space for buttons
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
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_login),
                            tint = Color.Black
                        )
                    }
                    AppLogoHeader(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2563EB)
                    )
                }

                TextButton(onClick = onSignInClick) {
                    Text(
                        text = stringResource(R.string.log_in),
                        color = Color(0xFF2563EB),
                        fontSize = 14.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }

        if (!isEmailSent) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.forgot_password_subtitle),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.reset_password),
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
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.please_enter_email),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color(0xFFEBF4FF),
                                unfocusedContainerColor = Color(0xFFEBF4FF)
                            ),
                            singleLine = true
                        )

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
                                if (email.isBlank()) {
                                    errorMessage = pleaseEnterEmailMsg
                                    return@Button
                                }

                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMessage = pleaseEnterValidEmailMsg
                                    return@Button
                                }

                                coroutineScope.launch {
                                    try {
                                        isLoading = true
                                        kotlinx.coroutines.delay(2000)
                                        isEmailSent = true
                                    } catch (e: Exception) {
                                        Log.e("ForgotPasswordActivity", "Reset password error", e)
                                        errorMessage = errorSendingEmailMsg
                                    } finally {
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.sign_in),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.do_you_remember_password),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = stringResource(R.string.sign_in_here),
                                fontSize = 14.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onSignInClick() }
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFFD1FAE5),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.success),
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.email_sent_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.email_sent_message, email),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Text(
                        text = stringResource(R.string.email_sent_instructions),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.back_to_sign_in),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            isEmailSent = false
                            email = ""
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.didnt_receive_email),
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}