package com.complaints.app

import android.app.Application
import com.complaints.app.util.SessionManager
import com.complaints.app.util.TokenManager

/**
 * ComplaintsApp — Application class.
 * Initializes TokenManager (EncryptedSharedPreferences) on startup,
 * then restores session state from stored token.
 */
class ComplaintsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize secure storage
        TokenManager.init(this)
        // Restore session so NavGraph can read the current user
        SessionManager.restoreSession()
    }
}
