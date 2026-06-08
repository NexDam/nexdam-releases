package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.nexdam.app.ui.components.TurnstileWidget
import it.nexdam.app.ui.theme.*
import it.nexdam.app.ui.viewmodels.AuthState
import it.nexdam.app.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var captchaToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top gradient accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Primary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo area
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("ND", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            }

            Spacer(Modifier.height(16.dp))
            Text("NexDam", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OnBackground)
            Text(
                "Client Portal",
                fontSize = 13.sp,
                color = Muted,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Card form
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .padding(20.dp)
            ) {
                Column {
                    Text("Accedi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                    Text("Inserisci le tue credenziali", fontSize = 13.sp, color = Muted, modifier = Modifier.padding(bottom = 20.dp, top = 4.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = nexDamTextFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = nexDamTextFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    TurnstileWidget(
                        modifier = Modifier.fillMaxWidth(),
                        onToken = { captchaToken = it }
                    )

                    if (state is AuthState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Error.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text((state as AuthState.Error).message, color = Error, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { vm.login(email, password, captchaToken) },
                        enabled = state !is AuthState.Loading && email.isNotBlank() && password.isNotBlank() && captchaToken != null,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (state is AuthState.Loading) {
                            CircularProgressIndicator(color = Background, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Accedi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Non hai un account?", color = Muted, fontSize = 14.sp)
                TextButton(onClick = onGoToRegister) {
                    Text("Registrati", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        // Footer
        Text(
            "© 2025 NexDam · Secure Web Infrastructure",
            color = Muted.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun nexDamTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = Primary,
    unfocusedLabelColor = Muted,
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    cursorColor = Primary,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant
)
