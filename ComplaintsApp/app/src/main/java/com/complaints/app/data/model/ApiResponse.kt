package com.complaints.app.data.model

import com.google.gson.annotations.SerializedName

// ── Auth API response models ──────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class SignupRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role")     val role: String
)

data class AuthResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("token")   val token: String,
    @SerializedName("user")    val user: User
)

data class MeResponse(
    @SerializedName("user") val user: User
)

// ── Complaint API response models ─────────────────────────

data class ComplaintsListResponse(
    @SerializedName("count")      val count: Int,
    @SerializedName("complaints") val complaints: List<Complaint>
)

data class SingleComplaintResponse(
    @SerializedName("message")   val message: String?,
    @SerializedName("complaint") val complaint: Complaint
)

data class SubmitComplaintRequest(
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category")    val category: String
)

data class UpdateComplaintRequest(
    @SerializedName("status")     val status: String?,
    @SerializedName("adminNotes") val adminNotes: String?
)

data class StatsResponse(
    @SerializedName("total")      val total: Int,
    @SerializedName("byStatus")   val byStatus: Map<String, Int>,
    @SerializedName("bySeverity") val bySeverity: Map<String, Int>,
    @SerializedName("byCategory") val byCategory: Map<String, Int>
)

data class DeleteResponse(
    @SerializedName("message") val message: String
)

data class ApiError(
    @SerializedName("message") val message: String?,
    @SerializedName("errors")  val errors: List<ValidationError>?
)

data class ValidationError(
    @SerializedName("msg")   val msg: String,
    @SerializedName("param") val param: String?
)
