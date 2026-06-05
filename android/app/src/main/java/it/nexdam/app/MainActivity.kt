package it.nexdam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import it.nexdam.app.data.supabase
import it.nexdam.app.ui.navigation.NexDamNavGraph
import it.nexdam.app.ui.navigation.Screen
import it.nexdam.app.ui.theme.NexDamTheme
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexDamTheme {
                val navController = rememberNavController()
                val isLoggedIn = remember {
                    supabase.auth.currentUserOrNull() != null
                }
                NexDamNavGraph(
                    navController = navController,
                    startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                )
            }
        }
    }
}
