package com.complaints.app.data.api

import com.complaints.app.data.model.AuthResponse
import com.complaints.app.data.model.LoginRequest
import com.complaints.app.data.model.MeResponse
import com.complaints.app.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * AuthApi — Retrofit interface for authentication endpoints.
 *
 * POST /api/auth/login   → LoginRequest  → AuthResponse
 * POST /api/auth/signup  → SignupRequest → AuthResponse
 * GET  /api/auth/me                      → MeResponse  (protected)
 */
interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<MeResponse>
}
