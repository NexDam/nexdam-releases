package it.nexdam.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import it.nexdam.desktop.data.supabase
import it.nexdam.desktop.ui.screens.LoginScreen
import it.nexdam.desktop.ui.screens.MainScreen
import it.nexdam.desktop.ui.theme.NexDamTheme
import it.nexdam.desktop.ui.viewmodels.AppViewModel
import it.nexdam.desktop.ui.viewmodels.AuthViewModel
import io.github.jan.supabase.auth.auth

fun main() = application {
    val authVm = remember { AuthViewModel() }
    val appVm = remember { AppViewModel() }
    var isLoggedIn by remember { mutableStateOf(supabase.auth.currentUserOrNull() != null) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NexDam Client Portal",
        state = WindowState(width = 1200.dp, height = 760.dp)
    ) {
        NexDamTheme {
            if (isLoggedIn) {
                MainScreen(vm = appVm, onLogout = { isLoggedIn = false })
            } else {
                LoginScreen(
                    onLoginSuccess = { isLoggedIn = true },
                    vm = authVm
                )
            }
        }
    }
}
