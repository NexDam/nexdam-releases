package it.nexdam.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.nexdam.app.data.models.Profile
import it.nexdam.app.data.models.Project
import it.nexdam.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val projects: List<Project>, val profile: Profile?) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadProjects() }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("Non autenticato")

                val profile = try {
                    supabase.postgrest["profiles"]
                        .select { filter { eq("id", userId) }; limit(1); single() }
                        .decodeAs<Profile>()
                } catch (_: Exception) { null }

                val isAdmin = profile?.role == "admin"
                val projects = supabase.postgrest["projects"]
                    .select(Columns.raw("""
                        id, title, description, status, created_at,
                        project_messages(id),
                        project_files(id, name),
                        invoices(id, status, amount, currency)
                    """.trimIndent())) {
                        // L'admin vede tutti i progetti; il cliente solo i propri.
                        if (!isAdmin) filter { eq("client_id", userId) }
                    }.decodeList<Project>()

                _uiState.value = DashboardUiState.Success(projects, profile)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Errore nel caricamento")
            }
        }
    }
}
