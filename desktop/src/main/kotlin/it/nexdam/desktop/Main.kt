package it.nexdam.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import it.nexdam.desktop.data.supabase
import it.nexdam.desktop.notifications.AutostartManager
import it.nexdam.desktop.notifications.DesktopNotifier
import it.nexdam.desktop.notifications.MessageRealtimeWatcher
import it.nexdam.desktop.ui.screens.LoginScreen
import it.nexdam.desktop.ui.screens.MainScreen
import it.nexdam.desktop.ui.theme.NexDamTheme
import it.nexdam.desktop.ui.viewmodels.AppViewModel
import it.nexdam.desktop.ui.viewmodels.AuthViewModel
import io.github.jan.supabase.auth.auth

fun main(args: Array<String>) = application {
    val authVm = remember { AuthViewModel() }
    val appVm = remember { AppViewModel() }
    var isLoggedIn by remember { mutableStateOf(supabase.auth.currentUserOrNull() != null) }

    // Quando avviato in autostart resta minimizzata in tray: l'utente la apre dall'icona.
    var isWindowVisible by remember { mutableStateOf(!args.contains("--autostart")) }

    // Avvia/ferma l'ascolto in tempo reale dei nuovi messaggi in base allo stato di login,
    // mostrando notifiche di sistema (system tray) quando arriva un messaggio dal team NexDam.
    // Per ricevere le notifiche anche ad app chiusa, registriamo l'app all'avvio automatico
    // del sistema così resta sempre attiva in background.
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                MessageRealtimeWatcher.start(userId)
                AutostartManager.ensureEnabled()
            }
        } else {
            MessageRealtimeWatcher.stop()
        }
    }

    val trayIcon = remember { BitmapPainter(DesktopNotifier.appIconImage().toComposeImageBitmap()) }

    Tray(
        icon = trayIcon,
        tooltip = "NexDam Client Portal",
        menu = {
            Item("Apri NexDam", onClick = { isWindowVisible = true })
            Separator()
            Item("Esci", onClick = ::exitApplication)
        },
        onAction = { isWindowVisible = true }
    )

    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                // Chiudere la finestra minimizza in tray invece di terminare il processo,
                // così l'ascolto realtime resta attivo e le notifiche continuano ad arrivare.
                isWindowVisible = false
                DesktopNotifier.notifyNewMessage(
                    "NexDam",
                    "L'app continua a girare in background: la trovi nell'area di notifica."
                )
            },
            title = "NexDam Client Portal",
            state = WindowState(width = 1200.dp, height = 760.dp)
        ) {
            NexDamTheme {
                if (isLoggedIn) {
                    MainScreen(vm = appVm, onLogout = {
                        MessageRealtimeWatcher.stop()
                        isLoggedIn = false
                    })
                } else {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                        vm = authVm
                    )
                }
            }
        }
    }
}
