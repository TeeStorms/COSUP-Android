package com.cosup.app.ui.sites

import com.cosup.app.data.SiteDto

/** One row in the Find a Site list: the site plus how far it is from the user (if known). */
data class SiteRow(
    val site: SiteDto,
    val km: Double?,
    val nearest: Boolean,
    val me: Pair<Double, Double>?
)
