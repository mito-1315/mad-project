package com.complaints.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complaints.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email:    String = "",
    val password: String = "",
    val emailError:    String = "",
    val passwordError: String = "",
    val apiError:  String = "",
    val isLoading: Boolean = false,
    val successRole: String? = null   // null = not done yet
)

class LoginViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = "")
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = "")
    }

    fun login() {
        val state = _uiState.value
        // Client-side validation (mirrors validate() in LoginPage.js)
        var emailErr = ""; var passErr = ""
        if (state.email.isBlank())                emailErr = "Email is required"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches())
                                                   emailErr = "Invalid email format"
        if (state.password.isBlank())              passErr  = "Password is required"

        if (emailErr.isNotEmpty() || passErr.isNotEmpty()) {
            _uiState.value = state.copy(emailError = emailErr, passwordError = passErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, apiError = "")
            val result = repo.login(state.email.trim(), state.password)
            result.fold(
                onSuccess = { auth ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successRole = auth.user.role
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        apiError  = err.message ?: "Login failed. Try again."
                    )
                }
            )
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successRole = null)
    }
}
