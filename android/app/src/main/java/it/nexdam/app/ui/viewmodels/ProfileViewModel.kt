package it.nexdam.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.nexdam.app.data.models.Profile
import it.nexdam.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val p = supabase.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                        limit(1)
                        single()
                    }.decodeAs<Profile>()
                _profile.value = p
            } catch (_: Exception) {}
        }
    }
}
