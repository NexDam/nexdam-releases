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
fun RegisterScreen(
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showEmailConfirm by remember { mutableStateOf(false) }
    var captchaToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            showEmailConfirm = true
        }
    }

    if (showEmailConfirm) {
        // Schermata "Controlla la tua email"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Controlla la tua email",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Abbiamo inviato un link di conferma a:\n$email\n\nClicca il link per attivare il tuo account, poi accedi.",
                    fontSize = 14.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onGoToLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Vai al Login", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crea account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        Text("NexDam Client Portal", fontSize = 14.sp, color = Muted, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nome completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = nexDamTextFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = nexDamTextFieldColors()
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
            colors = nexDamTextFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        TurnstileWidget(
            modifier = Modifier.fillMaxWidth(),
            onToken = { captchaToken = it }
        )

        if (state is AuthState.Error) {
            Text(
                (state as AuthState.Error).message,
                color = Error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.register(email, password, fullName, captchaToken) },
            enabled = state !is AuthState.Loading &&
                fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                captchaToken != null,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = Background, modifier = Modifier.size(20.dp))
            } else {
                Text("Registrati", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onGoToLogin) {
            Text("Hai già un account? Accedi", color = Primary)
        }
    }
}
