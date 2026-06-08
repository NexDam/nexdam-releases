package it.nexdam.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.auth.auth
import it.nexdam.app.data.supabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Riceve i push Firebase Cloud Messaging anche ad app completamente chiusa
 * e mostra una notifica di sistema. Tiene inoltre aggiornato il token del
 * dispositivo sul backend Supabase, così da poter inviare i push lato server.
 */
class NexDamFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["projectTitle"] ?: message.notification?.title ?: "NexDam"
        val body = data["body"] ?: message.notification?.body ?: return

        MessageNotifier.ensureChannel(applicationContext)
        MessageNotifier.notifyNewMessage(applicationContext, title, body)
    }

    private fun registerToken(token: String) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        scope.launch {
            try {
                DeviceTokenRegistrar.register(userId = userId, token = token)
            } catch (_: Exception) {
                // se la registrazione fallisce (es. rete assente), il token verrà
                // ritentato al prossimo avvio dell'app tramite DeviceTokenRegistrar.syncCurrentToken
            }
        }
    }
}
