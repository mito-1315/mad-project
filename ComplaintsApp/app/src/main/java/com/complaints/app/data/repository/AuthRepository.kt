package com.complaints.app.data.repository

import com.complaints.app.data.api.ApiClient
import com.complaints.app.data.model.AuthResponse
import com.complaints.app.data.model.LoginRequest
import com.complaints.app.data.model.SignupRequest
import com.complaints.app.util.SessionManager
import com.google.gson.Gson

/**
 * AuthRepository — wraps AuthApi calls and handles errors.
 * Returns Result<T> so ViewModels don't need to catch exceptions.
 */
class AuthRepository {

    private val api = ApiClient.authApi
    private val gson = Gson()

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                SessionManager.login(body.token, body.user)
                Result.success(body)
            } else {
                val error = parseError(response.errorBody()?.string())
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun signup(
        name: String, email: String, password: String, role: String
    ): Result<AuthResponse> {
        return try {
            val response = api.signup(SignupRequest(name, email, password, role))
            if (response.isSuccessful) {
                val body = response.body()!!
                SessionManager.login(body.token, body.user)
                Result.success(body)
            } else {
                val error = parseError(response.errorBody()?.string())
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val json = gson.fromJson(errorBody, Map::class.java)
            // Backend can return either { message: "..." } or { errors: [{msg:"..."}] }
            val message = json["message"] as? String
            if (message != null) return message
            @Suppress("UNCHECKED_CAST")
            val errors = json["errors"] as? List<Map<String, Any>>
            errors?.firstOrNull()?.get("msg") as? String ?: "Request failed"
        } catch (e: Exception) {
            "Request failed"
        }
    }
}
