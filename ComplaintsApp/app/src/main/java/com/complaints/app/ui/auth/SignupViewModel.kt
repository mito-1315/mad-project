package com.complaints.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complaints.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignupUiState(
    val name:        String = "",
    val email:       String = "",
    val password:    String = "",
    val confirm:     String = "",
    val role:        String = "student",
    val nameError:     String = "",
    val emailError:    String = "",
    val passwordError: String = "",
    val confirmError:  String = "",
    val apiError:    String = "",
    val isLoading:   Boolean = false,
    val successRole: String? = null
)

class SignupViewModel : ViewModel() {

    private val repo = AuthRepository()
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String)     { _uiState.value = _uiState.value.copy(name    = v, nameError    = "") }
    fun onEmailChange(v: String)    { _uiState.value = _uiState.value.copy(email   = v, emailError   = "") }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, passwordError = "") }
    fun onConfirmChange(v: String)  { _uiState.value = _uiState.value.copy(confirm  = v, confirmError  = "") }
    fun onRoleChange(v: String)     { _uiState.value = _uiState.value.copy(role    = v) }

    fun signup() {
        val s = _uiState.value
        var nErr = ""; var eErr = ""; var pErr = ""; var cErr = ""
        if (s.name.trim().length < 2)                        nErr = "Name must be at least 2 characters"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) eErr = "Enter a valid email"
        if (s.password.length < 6)                           pErr = "Password must be at least 6 characters"
        if (s.password != s.confirm)                         cErr = "Passwords do not match"

        if (listOf(nErr, eErr, pErr, cErr).any { it.isNotEmpty() }) {
            _uiState.value = s.copy(nameError=nErr, emailError=eErr, passwordError=pErr, confirmError=cErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, apiError = "")
            val result = repo.signup(s.name.trim(), s.email.trim(), s.password, s.role)
            result.fold(
                onSuccess = { auth ->
                    _uiState.value = _uiState.value.copy(isLoading = false, successRole = auth.user.role)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        apiError  = err.message ?: "Signup failed. Try again."
                    )
                }
            )
        }
    }

    fun clearSuccess() { _uiState.value = _uiState.value.copy(successRole = null) }
}
