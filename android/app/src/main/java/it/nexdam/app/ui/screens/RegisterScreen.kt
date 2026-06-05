package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            onRegistered()
        }
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
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = nexDamTextFieldColors()
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
            onClick = { vm.register(email, password, fullName) },
            enabled = state !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = Background, modifier = Modifier.size(20.dp))
            } else {
                Text("Registrati", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onGoToLogin) {
            Text("Hai già un account? Accedi", color = Primary)
        }
    }
}
