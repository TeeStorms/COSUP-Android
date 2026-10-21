package com.cosup.app.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.cosup.app.data.NewsDto
import com.cosup.app.data.ServiceDto
import com.cosup.app.databinding.ItemNewsBinding
import com.cosup.app.databinding.ItemServiceBinding
import com.cosup.app.databinding.ItemSiteBinding
import com.cosup.app.databinding.ViewBadgeBinding
import com.cosup.app.databinding.ViewStatBinding
import com.cosup.app.ui.sites.SiteRow
import com.cosup.app.util.TravelMode
import com.cosup.app.util.dial
import com.cosup.app.util.etaMinutes
import com.cosup.app.util.formatDate
import com.cosup.app.util.formatDist
import com.cosup.app.util.formatTime
import com.cosup.app.util.mapsUrl
import com.cosup.app.util.openUrl

fun ViewStatBinding.set(value: String, label: String) {
    statValue.text = value
    statLabel.text = label
}

fun badgeView(context: Context, text: String, parent: ViewGroup): TextView =
    ViewBadgeBinding.inflate(LayoutInflater.from(context), parent, false).root.also { it.text = text }

fun bindService(b: ItemServiceBinding, s: ServiceDto, onFind: (() -> Unit)? = null) {
    b.svcName.text = s.serviceName
    b.svcDesc.text = s.serviceDescription
    b.svcIcon.loadRemote(s.iconPath)
    b.btnFind.visible(onFind != null)
    b.btnFind.setOnClickListener { onFind?.invoke() }
}

fun bindNews(b: ItemNewsBinding, n: NewsDto) {
    val ctx = b.root.context
    b.newsCover.loadRemote(n.coverImg)
    b.newsCategory.visible(!n.category.isNullOrBlank())
    b.newsCategory.text = n.category
    b.newsTitle.text = n.title
    b.newsMeta.text = listOfNotNull(n.author, formatDate(n.publishedAt).ifBlank { null }).joinToString(" \u00B7 ")
    b.newsBody.text = n.body
    val url = n.sourceUrl
    b.btnRead.visible(!url.isNullOrBlank())
    b.btnRead.setOnClickListener { if (!url.isNullOrBlank()) ctx.openUrl(url) }
}

fun bindSite(b: ItemSiteBinding, row: SiteRow, onClick: () -> Unit, onCall: () -> Unit) {
    val ctx = b.root.context
    val site = row.site
    val km = row.km
    b.siteName.text = site.siteName
    b.siteMain.visible(site.isMainCentre)
    b.siteNearest.visible(row.nearest)
    b.siteAddress.text = site.address
    b.siteHours.visible(!site.operatingHours.isNullOrBlank())
    b.siteHours.text = site.operatingHours
    b.siteDistance.visible(km != null)
    if (km != null) b.siteDistance.text = "\uD83D\uDCCD \u2248 ${formatDist(km)} away"

    b.siteTags.removeAllViews()
    site.services.forEach { b.siteTags.addView(badgeView(ctx, it, b.siteTags)) }

    listOf(
        Triple(b.btnDrive, TravelMode.DRIVE, "\uD83D\uDE97"),
        Triple(b.btnWalk, TravelMode.WALK, "\uD83D\uDEB6"),
        Triple(b.btnTransit, TravelMode.TRANSIT, "\uD83D\uDE8C")
    ).forEach { (btn, mode, icon) ->
        btn.text = if (km != null) "$icon ${mode.label}\n${formatTime(etaMinutes(km, mode))}" else "$icon ${mode.label}"
        btn.setOnClickListener {
            ctx.openUrl(mapsUrl(site.latitude, site.longitude, mode, row.me?.first, row.me?.second))
        }
    }

    val phone = site.phone
    b.btnCall.visible(!phone.isNullOrBlank())
    b.btnCall.text = "Call $phone"
    b.btnCall.setOnClickListener { if (!phone.isNullOrBlank()) { onCall(); ctx.dial(phone) } }
    b.root.setOnClickListener { onClick() }
}
