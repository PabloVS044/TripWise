package uvg.edu.tripwise.auth.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.tripwise.R
import uvg.edu.tripwise.ui.components.AppLogoHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoScreen(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    totalSteps: Int = 3, // Added parameter for dynamic steps
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) }

        item {
            AppLogoHeader(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }

        item {
            Text(
                text = stringResource(R.string.create_account_message),
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
        }

        item {
            StepIndicator(
                currentStep = 1,
                totalSteps = totalSteps,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.basic_info),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = stringResource(R.string.complete_personal_data),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.confirm_password)) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        focusedLabelColor = Color(0xFF2563EB)
                    ),
                    isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
                    supportingText = {
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text(
                                text = stringResource(R.string.passwords_do_not_match),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
    }
}