package com.complaints.app.data.repository

import com.complaints.app.data.api.ApiClient
import com.complaints.app.data.model.*
import com.google.gson.Gson

/**
 * ComplaintRepository — wraps ComplaintsApi calls.
 */
class ComplaintRepository {

    private val api  = ApiClient.complaintsApi
    private val gson = Gson()

    suspend fun getComplaints(
        status: String? = null,
        severity: String? = null,
        category: String? = null
    ): Result<List<Complaint>> {
        return try {
            val response = api.getComplaints(
                status   = status?.takeIf { it != "All" },
                severity = severity?.takeIf { it != "All" },
                category = category?.takeIf { it != "All" }
            )
            if (response.isSuccessful)
                Result.success(response.body()!!.complaints)
            else
                Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun submitComplaint(
        title: String, description: String, category: String
    ): Result<Complaint> {
        return try {
            val response = api.submitComplaint(SubmitComplaintRequest(title, description, category))
            if (response.isSuccessful)
                Result.success(response.body()!!.complaint)
            else
                Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getStats(): Result<StatsResponse> {
        return try {
            val response = api.getStats()
            if (response.isSuccessful)
                Result.success(response.body()!!)
            else
                Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateComplaint(
        id: String, status: String?, adminNotes: String?
    ): Result<Complaint> {
        return try {
            val response = api.updateComplaint(id, UpdateComplaintRequest(status, adminNotes))
            if (response.isSuccessful)
                Result.success(response.body()!!.complaint)
            else
                Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun deleteComplaint(id: String): Result<Unit> {
        return try {
            val response = api.deleteComplaint(id)
            if (response.isSuccessful)
                Result.success(Unit)
            else
                Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val json = gson.fromJson(errorBody, Map::class.java)
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
