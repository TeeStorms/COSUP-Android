package com.cosup.app.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class TravelMode(val label: String, val speedKmh: Int, val param: String) {
    DRIVE("Drive", 40, "driving"),
    WALK("Walk", 5, "walking"),
    TRANSIT("Transit", 22, "transit")
}

/** Builds a full image URL. IconPath / CoverImg are stored relative to the website's /images folder. */
fun imageUrl(baseUrl: String, path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return base + "images/" + path.trimStart('/')
}

fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 19) return ""
    return try {
        LocalDateTime.parse(iso.substring(0, 19)).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    } catch (e: Exception) {
        ""
    }
}

fun formatDateTime(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 19) return ""
    return try {
        LocalDateTime.parse(iso.substring(0, 19)).format(DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.ENGLISH))
    } catch (e: Exception) {
        ""
    }
}

/** Straight-line distance in km (haversine) - same maths as the website's Find a Site page. */
fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * r * asin(sqrt(h))
}

fun formatDist(km: Double): String =
    if (km < 1) "${(km * 1000).roundToInt()} m" else String.format(Locale.ENGLISH, "%.1f km", km)

fun formatTime(mins: Int): String {
    if (mins < 60) return "$mins min"
    val h = mins / 60
    val m = mins % 60
    return if (m > 0) "$h h $m min" else "$h h"
}

/** Rough travel time: distance x 1.3 road factor / average speed. */
fun etaMinutes(km: Double, mode: TravelMode): Int =
    max(1, ((km * 1.3) / mode.speedKmh * 60).roundToInt())

fun mapsUrl(destLat: Double, destLon: Double, mode: TravelMode, originLat: Double?, originLon: Double?): String {
    val origin = if (originLat != null && originLon != null) "&origin=$originLat,$originLon" else ""
    return "https://www.google.com/maps/dir/?api=1$origin&destination=$destLat,$destLon&travelmode=${mode.param}"
}

private val PHONE = Regex("^[0-9+ ]{9,15}$")
private val PHONE_OPTIONAL = Regex("^[0-9+ ]{0,15}$")
private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private val ALIAS = Regex("^[\\p{L}\\p{N} _.\\-]{2,30}$")

fun isValidPhone(s: String) = PHONE.matches(s.trim())
fun isValidOptionalPhone(s: String) = PHONE_OPTIONAL.matches(s.trim())
fun isValidEmail(s: String) = EMAIL.matches(s.trim())
fun isValidAlias(s: String) = ALIAS.matches(s.trim().replace(Regex("\\s+"), " "))

val LANGUAGES = listOf(
    "en" to "English", "zu" to "isiZulu", "xh" to "isiXhosa",
    "af" to "Afrikaans", "st" to "Sesotho", "tn" to "Setswana"
)

val REFERRAL_STATUSES = listOf("pending", "contacted", "enrolled", "closed")
