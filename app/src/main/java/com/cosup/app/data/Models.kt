package com.cosup.app.data

data class ServiceDto(
    val serviceId: Int,
    val serviceName: String,
    val serviceDescription: String,
    val iconPath: String?
)

data class NewsDto(
    val newsId: Int,
    val title: String,
    val body: String,
    val author: String?,
    val category: String?,
    val coverImg: String?,
    val publishedAt: String?,
    val sourceUrl: String?
)

data class SiteDto(
    val siteId: Int,
    val siteName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val operatingHours: String?,
    val phone: String?,
    val isMainCentre: Boolean,
    val coverImg: String?,
    val services: List<String>
)

data class HomeDto(
    val services: List<ServiceDto>,
    val news: List<NewsDto>,
    val sites: List<SiteDto>
)

data class LoginRequest(
    val idToken: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

data class LoginResponse(
    val token: String,
    val role: String,
    val name: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val langPref: String?
)

data class ReferralRequest(
    val fullName: String,
    val contactNumber: String,
    val email: String?,
    val preferredArea: String?,
    val serviceNeeded: String?,
    val message: String?,
    val consent: Boolean
)

data class ReferralDto(
    val referralId: Int,
    val serviceNeeded: String?,
    val preferredArea: String?,
    val status: String,
    val submittedAt: String?
)

data class ProfileDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val langPref: String,
    val role: String?,
    val referrals: List<ReferralDto>
)

data class ProfileUpdate(
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val langPref: String
)

data class HopelineRequest(val channel: String)

data class ApiMessage(val message: String?)

/** Signed-in user as the UI sees it. */
data class SessionInfo(val token: String, val role: String, val name: String)

// ---------------------------------------------------------------- admin
data class AdminReferral(
    val referralId: Int,
    val fullName: String,
    val contactNumber: String,
    val email: String?,
    val serviceNeeded: String?,
    val preferredArea: String?,
    val message: String?,
    val source: String,
    val submittedAt: String?,
    val status: String
)

data class AdminUserRow(val name: String, val email: String)
data class AuditRowDto(val admin: String, val action: String, val at: String?)

data class AdminDashboard(
    val users: Int,
    val sites: Int,
    val referrals: Int,
    val pending: Int,
    val news: Int,
    val hopeline: Int,
    val pendingList: List<AdminReferral>,
    val byStatus: Map<String, Int>,
    val recentUsers: List<AdminUserRow>,
    val audit: List<AuditRowDto>
)

data class AdminNewsRow(
    val newsId: Int,
    val title: String,
    val category: String?,
    val isPublished: Boolean,
    val publishedAt: String?
)

data class AdminSite(
    val siteId: Int,
    val siteName: String,
    val address: String,
    val operatingHours: String?,
    val phone: String?,
    val isMainCentre: Boolean,
    val isActive: Boolean
)

data class ActivityRowDto(
    val who: String,
    val role: String,
    val action: String,
    val detail: String?,
    val page: String?,
    val at: String?
)

data class ActivityPage(val guests: Int, val events: Int, val today: Int, val rows: List<ActivityRowDto>)

data class StatusBody(val status: String)
data class NewsCreateBody(val title: String, val body: String, val category: String?)
data class SiteEditBody(val siteName: String, val address: String, val operatingHours: String?, val phone: String?)
