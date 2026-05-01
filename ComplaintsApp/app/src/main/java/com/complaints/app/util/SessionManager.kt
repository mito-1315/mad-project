package com.complaints.app.util

import com.complaints.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SessionManager — global in-memory auth state.
 * Equivalent to React's AuthContext / useAuth() hook.
 *
 * Any ViewModel can observe [currentUser] to react to login/logout.
 */
object SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /** Call on app start — restore session from TokenManager */
    fun restoreSession() {
        _currentUser.value = TokenManager.getUser()
    }

    fun login(token: String, user: User) {
        TokenManager.saveToken(token)
        TokenManager.saveUser(user)
        _currentUser.value = user
    }

    fun logout() {
        TokenManager.clear()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    fun isAdmin(): Boolean = _currentUser.value?.role == "admin"
}
