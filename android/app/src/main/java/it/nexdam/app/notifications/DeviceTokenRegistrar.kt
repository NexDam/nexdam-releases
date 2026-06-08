package it.nexdam.app.notifications

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
        supabase.postgrest["device_push_tokens"]
            .upsert(DeviceTokenRow(user_id = userId, token = token)) {
                onConflict = "user_id, token"
            }
    }

    /** Da chiamare dopo il login per assicurarsi che il token corrente sia registrato. */
    suspend fun syncCurrentToken(userId: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        register(userId = userId, token = token)
    }
}
