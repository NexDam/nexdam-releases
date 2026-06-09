package it.nexdam.desktop.ui.viewmodels

import it.nexdam.desktop.data.models.*
import it.nexdam.desktop.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AppViewModel {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sendingMessage = MutableStateFlow(false)
    val sendingMessage: StateFlow<Boolean> = _sendingMessage

    fun loadData() {
        scope.launch {
            _loading.value = true
            _error.value = null
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // Load profile
                try {
                    _profile.value = supabase.postgrest["profiles"]
                        .select { filter { eq("id", userId) }; limit(1); single() }
                        .decodeAs<Profile>()
                } catch (_: Exception) {}

                // Load projects — l'admin vede tutti, il cliente solo i propri.
                val isAdmin = _profile.value?.role == "admin"
                _projects.value = supabase.postgrest["projects"]
                    .select(Columns.raw("""
                        id, title, description, status, created_at,
                        project_messages(id),
                        project_files(id, name),
                        invoices(id, status, amount, currency)
                    """.trimIndent())) {
                        if (!isAdmin) filter { eq("client_id", userId) }
                    }.decodeList<Project>()

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectProject(project: Project) {
        scope.launch {
            try {
                val full = supabase.postgrest["projects"]
                    .select(Columns.raw("""
                        id, title, description, status, created_at,
                        project_messages(id, project_id, sender_id, is_admin, body, created_at),
                        project_files(id, project_id, name, url, size_label, created_at),
                        invoices(id, project_id, amount, currency, status, description, due_date, created_at)
                    """.trimIndent())) {
                        filter { eq("id", project.id) }
                        limit(1)
                        single()
                    }.decodeAs<Project>()
                _selectedProject.value = full
            } catch (e: Exception) {
                _selectedProject.value = project
            }
        }
    }

    fun clearSelectedProject() { _selectedProject.value = null }

    fun sendMessage(projectId: String, body: String) {
        if (body.isBlank()) return
        scope.launch {
            _sendingMessage.value = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                // Determina se chi scrive è admin per impostare is_admin correttamente
                // (i messaggi admin fanno scattare la push notification al cliente).
                val senderIsAdmin = try {
                    supabase.postgrest["profiles"]
                        .select { filter { eq("id", userId) }; limit(1); single() }
                        .decodeAs<Profile>().role == "admin"
                } catch (_: Exception) { false }

                supabase.postgrest["project_messages"].insert(
                    buildJsonObject {
                        put("project_id", projectId)
                        put("sender_id", userId)
                        put("is_admin", senderIsAdmin)
                        put("body", body.trim())
                    }
                )
                // Refresh selected project
                _selectedProject.value?.let { selectProject(it) }
            } catch (_: Exception) {
            } finally {
                _sendingMessage.value = false
            }
        }
    }

    fun logout() {
        scope.launch {
            try { supabase.auth.signOut() } catch (_: Exception) {}
        }
    }
}
