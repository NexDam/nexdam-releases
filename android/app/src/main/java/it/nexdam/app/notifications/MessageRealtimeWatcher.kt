package it.nexdam.app.notifications

import android.content.Context
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import it.nexdam.app.data.supabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Si mette in ascolto via Supabase Realtime dei nuovi messaggi inseriti nella
 * tabella `project_messages`. Quando un messaggio proviene dal team NexDam
 * (is_admin = true) ed è relativo ad un progetto dell'utente loggato,
 * mostra una notifica di sistema.
 *
 * Nota: funziona finché il processo dell'app è vivo (foreground o background
 * recente). Per notifiche push "vere" ad app completamente chiusa servirebbe
 * un sistema server-side (es. Firebase Cloud Messaging + Edge Function).
 */
object MessageRealtimeWatcher {

    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start(context: Context, userId: String) {
        if (job?.isActive == true) return

        MessageNotifier.ensureChannel(context)

        job = scope.launch {
            try {
                // Recupera gli id dei progetti dell'utente per filtrare i messaggi pertinenti
                val myProjects = supabase.postgrest["projects"]
                    .select(Columns.raw("id, title")) {
                        filter { eq("client_id", userId) }
                    }
                    .decodeList<JsonObject>()

                val titleByProjectId = myProjects.associate { obj ->
                    val id = obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Progetto"
                    id to title
                }

                if (titleByProjectId.isEmpty()) return@launch

                val realtimeChannel = supabase.realtime.channel("android-project-messages-$userId")

                realtimeChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "project_messages"
                }.onEach { action ->
                    val record = action.record
                    val isAdmin = record["is_admin"]?.jsonPrimitive?.booleanOrNull ?: false
                    val senderId = record["sender_id"]?.jsonPrimitive?.contentOrNull
                    val projectId = record["project_id"]?.jsonPrimitive?.contentOrNull
                    val body = record["body"]?.jsonPrimitive?.contentOrNull

                    if (isAdmin && senderId != userId && projectId != null && body != null) {
                        val title = titleByProjectId[projectId] ?: return@onEach
                        MessageNotifier.notifyNewMessage(context, title, body)
                    }
                }.launchIn(this)

                realtimeChannel.subscribe()
            } catch (_: Exception) {
                // se la sottoscrizione fallisce (es. rete assente), semplicemente non notifichiamo
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
