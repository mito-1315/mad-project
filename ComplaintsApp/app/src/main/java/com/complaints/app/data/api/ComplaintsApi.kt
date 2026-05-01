package com.complaints.app.data.api

import com.complaints.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * ComplaintsApi — Retrofit interface for complaints endpoints.
 *
 * GET    /api/complaints              → ComplaintsListResponse
 * POST   /api/complaints              → SingleComplaintResponse
 * GET    /api/complaints/stats        → StatsResponse  (admin)
 * GET    /api/complaints/{id}         → SingleComplaintResponse
 * PUT    /api/complaints/{id}         → SingleComplaintResponse (admin)
 * DELETE /api/complaints/{id}         → DeleteResponse (admin)
 */
interface ComplaintsApi {

    @GET("api/complaints")
    suspend fun getComplaints(
        @Query("status")   status:   String? = null,
        @Query("severity") severity: String? = null,
        @Query("category") category: String? = null
    ): Response<ComplaintsListResponse>

    @POST("api/complaints")
    suspend fun submitComplaint(
        @Body request: SubmitComplaintRequest
    ): Response<SingleComplaintResponse>

    @GET("api/complaints/stats")
    suspend fun getStats(): Response<StatsResponse>

    @GET("api/complaints/{id}")
    suspend fun getComplaint(@Path("id") id: String): Response<SingleComplaintResponse>

    @PUT("api/complaints/{id}")
    suspend fun updateComplaint(
        @Path("id")  id:      String,
        @Body request: UpdateComplaintRequest
    ): Response<SingleComplaintResponse>

    @DELETE("api/complaints/{id}")
    suspend fun deleteComplaint(@Path("id") id: String): Response<DeleteResponse>
}
