package it.nexdam.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.nexdam.app.data.models.Project
import it.nexdam.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class ProjectUiState {
    object Loading : ProjectUiState()
    data class Success(val project: Project) : ProjectUiState()
    data class Error(val message: String) : ProjectUiState()
}

class ProjectViewModel(private val projectId: String) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
    val uiState: StateFlow<ProjectUiState> = _uiState

    private val _sendingMessage = MutableStateFlow(false)
    val sendingMessage: StateFlow<Boolean> = _sendingMessage

    init { loadProject() }

    fun loadProject() {
        viewModelScope.launch {
            _uiState.value = ProjectUiState.Loading
            try {
                val project = supabase.postgrest["projects"]
                    .select(Columns.raw("""
                        id, title, description, status, created_at, updated_at,
                        project_messages(id, project_id, sender_id, is_admin, body, created_at),
                        project_files(id, project_id, name, url, size_label, uploaded_by_admin, created_at),
                        invoices(id, project_id, amount, currency, status, description, due_date, paid_at, created_at)
                    """.trimIndent())) {
                        filter { eq("id", projectId) }
                        limit(1)
                        single()
                    }.decodeAs<Project>()

                _uiState.value = ProjectUiState.Success(project)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Errore")
            }
        }
    }

    fun sendMessage(body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            _sendingMessage.value = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                supabase.postgrest["project_messages"].insert(
                    buildJsonObject {
                        put("project_id", projectId)
                        put("sender_id", userId)
                        put("is_admin", false)
                        put("body", body.trim())
                    }
                )
                loadProject()
            } catch (_: Exception) {
            } finally {
                _sendingMessage.value = false
            }
        }
    }
}
