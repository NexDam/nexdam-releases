package it.nexdam.desktop.ui.viewmodels

import it.nexdam.desktop.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        scope.launch {
            _state.value = AuthState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login fallito")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}
