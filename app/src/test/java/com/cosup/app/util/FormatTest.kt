package com.cosup.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatTest {

    @Test
    fun imageUrl_buildsFromRelativePath() {
        assertEquals(
            "https://site.example/images/icons/hiv.png",
            imageUrl("https://site.example/", "icons/hiv.png")
        )
    }

    @Test
    fun imageUrl_addsMissingSlashAndKeepsAbsoluteUrls() {
        assertEquals("https://site.example/images/news1.jpg", imageUrl("https://site.example", "/news1.jpg"))
        assertEquals("https://other.example/a.png", imageUrl("https://site.example/", "https://other.example/a.png"))
    }

    @Test
    fun imageUrl_nullOrBlankGivesNull() {
        assertNull(imageUrl("https://site.example/", null))
        assertNull(imageUrl("https://site.example/", "  "))
    }

    @Test
    fun distance_pretoriaToJohannesburgIsAboutFiftyFourKm() {
        val km = distanceKm(-25.7479, 28.2293, -26.2041, 28.0473)
        assertEquals(54.0, km, 3.0)
    }

    @Test
    fun distance_samePointIsZero() {
        assertEquals(0.0, distanceKm(-25.7, 28.2, -25.7, 28.2), 0.0001)
    }

    @Test
    fun formatDist_usesMetresUnderOneKm() {
        assertEquals("500 m", formatDist(0.5))
        assertEquals("2.3 km", formatDist(2.34))
    }

    @Test
    fun formatTime_handlesHours() {
        assertEquals("45 min", formatTime(45))
        assertEquals("1 h 15 min", formatTime(75))
        assertEquals("2 h", formatTime(120))
    }

    @Test
    fun eta_driveIsFasterThanWalk() {
        assertTrue(etaMinutes(10.0, TravelMode.DRIVE) < etaMinutes(10.0, TravelMode.WALK))
        assertEquals(1, etaMinutes(0.0, TravelMode.DRIVE))
    }

    @Test
    fun mapsUrl_includesOriginOnlyWhenKnown() {
        val withOrigin = mapsUrl(-25.7, 28.2, TravelMode.WALK, -25.8, 28.1)
        assertTrue(withOrigin.contains("origin=-25.8,28.1"))
        assertTrue(withOrigin.contains("travelmode=walking"))
        assertFalse(mapsUrl(-25.7, 28.2, TravelMode.DRIVE, null, null).contains("origin="))
    }

    @Test
    fun formatDate_formatsIsoAndSurvivesBadInput() {
        assertEquals("18 Oct 2026", formatDate("2026-10-18T10:30:00"))
        assertEquals("18 Oct 2026", formatDate("2026-10-18T10:30:00.1234567"))
        assertEquals("", formatDate(null))
        assertEquals("", formatDate("not a date at all......"))
    }

    @Test
    fun phoneValidation_matchesWebsiteRule() {
        assertTrue(isValidPhone("0800 611 197"))
        assertTrue(isValidPhone("+27 12 345 6789"))
        assertFalse(isValidPhone("12345"))
        assertFalse(isValidPhone("abc123456789"))
        assertTrue(isValidOptionalPhone(""))
    }

    @Test
    fun emailAndAliasValidation() {
        assertTrue(isValidEmail("a@b.co"))
        assertFalse(isValidEmail("a@b"))
        assertTrue(isValidAlias("Sunny"))
        assertFalse(isValidAlias("x"))
        assertFalse(isValidAlias("bad<name>"))
    }

    @Test
    fun formatDateTime_formatsAndSurvivesBadInput() {
        assertEquals("18 Oct 10:30", formatDateTime("2026-10-18T10:30:00"))
        assertEquals("", formatDateTime(null))
        assertEquals("", formatDateTime("garbage garbage garbage"))
    }

    @Test
    fun referralStatuses_matchWebsite() {
        assertEquals(listOf("pending", "contacted", "enrolled", "closed"), REFERRAL_STATUSES)
    }
}
