package com.complaints.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Complaint model — matches the backend complaint JSON from MongoDB.
 */
data class Complaint(
    @SerializedName("_id")                val id: String,
    @SerializedName("title")             val title: String,
    @SerializedName("description")       val description: String,
    @SerializedName("category")          val category: String,
    @SerializedName("submittedBy")       val submittedBy: SubmittedBy?,
    @SerializedName("severity")          val severity: String,          // High | Medium | Low | Unclassified
    @SerializedName("severityConfidence")val severityConfidence: Double?,
    @SerializedName("status")            val status: String,            // pending | in-progress | resolved | rejected
    @SerializedName("adminNotes")        val adminNotes: String?,
    @SerializedName("createdAt")         val createdAt: String
)

data class SubmittedBy(
    @SerializedName("_id")   val id: String?,
    @SerializedName("name")  val name: String?,
    @SerializedName("email") val email: String?
)
