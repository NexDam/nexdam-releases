package it.nexdam.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.nexdam.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String, captchaToken: String?) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                    this.captchaToken = captchaToken
                }
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        username: String,
        company: String,
        phone: String,
        captchaToken: String?
    ) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.captchaToken = captchaToken
                    data = buildJsonObject {
                        put("username", JsonPrimitive(username.trim()))
                        if (company.isNotBlank()) put("company", JsonPrimitive(company.trim()))
                        if (phone.isNotBlank()) put("phone", JsonPrimitive(phone.trim()))
                    }
                }
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}
