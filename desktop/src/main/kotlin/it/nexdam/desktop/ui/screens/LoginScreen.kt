package it.nexdam.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.nexdam.desktop.ui.components.TurnstileWidget
import it.nexdam.desktop.ui.theme.*
import it.nexdam.desktop.ui.viewmodels.AuthState
import it.nexdam.desktop.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, vm: AuthViewModel) {
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var captchaToken by remember { mutableStateOf<String?>(null) }

    fun attemptLogin() {
        if (captchaToken != null) vm.login(email, password, captchaToken)
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        // Left decorative panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.45f)
                .align(Alignment.CenterStart)
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ND", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }
                Spacer(Modifier.height(20.dp))
                Text("NexDam", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = OnBg)
                Text("CLIENT PORTAL", fontSize = 12.sp, color = Muted, letterSpacing = 3.sp)
                Spacer(Modifier.height(32.dp))
                Text(
                    "Gestisci i tuoi progetti,\nscambia messaggi e\nscarica i tuoi file.",
                    color = Muted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Right login form
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.CenterEnd)
                .padding(horizontal = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.widthIn(max = 360.dp)) {
                Text("Accedi", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OnBg)
                Text("Inserisci le tue credenziali", fontSize = 13.sp, color = Muted, modifier = Modifier.padding(bottom = 28.dp, top = 4.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
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
                                contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().onKeyEvent {
                        if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                            attemptLogin(); true
                        } else false
                    },
                    colors = fieldColors(),
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
                            .background(Danger.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Text((state as AuthState.Error).message, color = Danger, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { attemptLogin() },
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

        // Divider line
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Divider)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = Divider,
    focusedLabelColor = Primary,
    unfocusedLabelColor = Muted,
    focusedTextColor = OnBg,
    unfocusedTextColor = OnBg,
    cursorColor = Primary,
    focusedContainerColor = SurfaceVar,
    unfocusedContainerColor = SurfaceVar
)
