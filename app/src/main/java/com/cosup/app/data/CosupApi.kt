package com.cosup.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT

/** Mirrors the /api endpoints added to the CosupWeb project (MobileApiController). */
interface CosupApi {
    @GET("api/home") suspend fun home(): HomeDto
    @GET("api/services") suspend fun services(): List<ServiceDto>
    @GET("api/sites") suspend fun sites(): List<SiteDto>
    @GET("api/news") suspend fun news(): List<NewsDto>

    @POST("api/auth/firebase-login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @POST("api/auth/logout") suspend fun logout(): Response<Unit>

    @GET("api/profile") suspend fun profile(): ProfileDto
    @PUT("api/profile") suspend fun updateProfile(@Body body: ProfileUpdate): Response<Unit>

    @POST("api/referrals") suspend fun submitReferral(@Body body: ReferralRequest): Response<ApiMessage>
    @POST("api/hopeline") suspend fun logHopeline(@Body body: HopelineRequest): Response<Unit>

    // ---- admin (role = admin only) ----
    @GET("api/admin/dashboard") suspend fun adminDashboard(): AdminDashboard
    @GET("api/admin/referrals") suspend fun adminReferrals(): List<AdminReferral>
    @PUT("api/admin/referrals/{id}/status")
    suspend fun adminUpdateReferral(@Path("id") id: Int, @Body body: StatusBody): Response<Unit>
    @GET("api/admin/news") suspend fun adminNews(): List<AdminNewsRow>
    @POST("api/admin/news") suspend fun adminCreateNews(@Body body: NewsCreateBody): Response<Unit>
    @POST("api/admin/news/{id}/toggle") suspend fun adminToggleNews(@Path("id") id: Int): Response<Unit>
    @GET("api/admin/sites") suspend fun adminSites(): List<AdminSite>
    @PUT("api/admin/sites/{id}") suspend fun adminEditSite(@Path("id") id: Int, @Body body: SiteEditBody): Response<Unit>
    @GET("api/admin/audit") suspend fun adminAudit(): List<AuditRowDto>
    @GET("api/admin/activity") suspend fun adminActivity(): ActivityPage
}
