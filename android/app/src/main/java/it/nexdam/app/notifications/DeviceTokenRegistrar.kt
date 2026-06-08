package it.nexdam.app.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.postgrest.postgrest
import it.nexdam.app.data.supabase
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

/**
 * Tiene allineata la tabella `device_push_tokens` su Supabase con il token FCM
 * del dispositivo, così che l'Edge Function lato server possa inviare le push
 * all'utente giusto anche ad app chiusa.
 */
object DeviceTokenRegistrar {

    @Serializable
    private data class DeviceTokenRow(
        val user_id: String,
        val token: String,
        val platform: String = "android"
    )

    suspend fun register(userId: String, token: String) {
        Log.d("NexDamPush", "Upsert device_push_tokens user_id=$userId token=${token.take(16)}…")
        supabase.postgrest["device_push_tokens"]
            .upsert(DeviceTokenRow(user_id = userId, token = token)) {
                onConflict = "user_id,token"
            }
        Log.d("NexDamPush", "Upsert device_push_tokens completato")
    }

    /** Da chiamare dopo il login per assicurarsi che il token corrente sia registrato. */
    suspend fun syncCurrentToken(userId: String) {
        Log.d("NexDamPush", "Richiedo token FCM a Firebase…")
        val token = FirebaseMessaging.getInstance().token.await()
        Log.d("NexDamPush", "Token FCM ottenuto: ${token.take(16)}…")
        register(userId = userId, token = token)
    }
}
