package com.complaints.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * User model — matches the backend user JSON:
 * { id, name, email, role }
 */
data class User(
    @SerializedName("id")    val id: String,
    @SerializedName("name")  val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role")  val role: String   // "student" | "admin"
)
