package com.complaints.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.complaints.app.data.model.User
import com.google.gson.Gson

/**
 * TokenManager — secure storage for JWT token and user info.
 * Uses EncryptedSharedPreferences (AES256 encryption under the hood).
 *
 * Equivalent to localStorage.setItem('token') in the React frontend.
 */
object TokenManager {

    private const val PREFS_FILE = "secure_prefs"
    private const val KEY_TOKEN   = "auth_token"
    private const val KEY_USER    = "auth_user"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    fun saveUser(user: User) {
        prefs?.edit()?.putString(KEY_USER, gson.toJson(user))?.apply()
    }

    fun getUser(): User? {
        val json = prefs?.getString(KEY_USER, null) ?: return null
        return try { gson.fromJson(json, User::class.java) } catch (e: Exception) { null }
    }

    fun clear() {
        prefs?.edit()?.remove(KEY_TOKEN)?.remove(KEY_USER)?.apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null && getUser() != null
}
