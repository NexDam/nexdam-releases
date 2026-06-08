package it.nexdam.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import it.nexdam.app.data.supabase
import it.nexdam.app.notifications.DeviceTokenRegistrar
import it.nexdam.app.notifications.MessageRealtimeWatcher
import it.nexdam.app.ui.navigation.NexDamNavGraph
import it.nexdam.app.ui.navigation.Screen
import it.nexdam.app.ui.theme.NexDamTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexDamTheme {
                val navController = rememberNavController()

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* esito ignorato: se negato, semplicemente non mostreremo notifiche */ }

                val startDestination = remember {
                    if (supabase.auth.currentUserOrNull() != null)
                        Screen.Dashboard.route
                    else
                        Screen.Login.route
                }

                // Ascolta i cambiamenti di sessione in tempo reale
                // Es: dopo verifica email, l'utente torna nell'app e viene
                // portato automaticamente alla Dashboard senza dover fare login
                val sessionStatus by supabase.auth.sessionStatus.collectAsState()

                LaunchedEffect(sessionStatus) {
                    when (sessionStatus) {
                        is SessionStatus.Authenticated -> {
                            val current = navController.currentDestination?.route
                            if (current == Screen.Login.route || current == Screen.Register.route) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }

                            // Richiede il permesso di notifica (Android 13+) e avvia
                            // l'ascolto in tempo reale dei nuovi messaggi del progetto.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            val userId = supabase.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                MessageRealtimeWatcher.start(applicationContext, userId)
                                // Registra il token FCM del dispositivo: permette di ricevere
                                // le notifiche push anche ad app completamente chiusa.
                                launch {
                                    try {
                                        DeviceTokenRegistrar.syncCurrentToken(userId)
                                        Log.d("NexDamPush", "Token FCM registrato con successo per userId=$userId")
                                    } catch (e: Exception) {
                                        // rete assente o permesso Google Play Services mancante: si ritenterà al prossimo avvio
                                        Log.e("NexDamPush", "Registrazione token FCM fallita", e)
                                    }
                                }
                            }
                        }
                        is SessionStatus.NotAuthenticated -> {
                            val current = navController.currentDestination?.route
                            if (current != null &&
                                current != Screen.Login.route &&
                                current != Screen.Register.route) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            MessageRealtimeWatcher.stop()
                        }
                        else -> {}
                    }
                }

                NexDamNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
