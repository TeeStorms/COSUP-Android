package com.cosup.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosup.app.data.NewsDto
import com.cosup.app.data.ServiceDto
import com.cosup.app.data.SiteDto
import com.cosup.app.ui.theme.CosupGreen
import com.cosup.app.ui.theme.CosupGreenDark
import com.cosup.app.util.TravelMode
import com.cosup.app.util.dial
import com.cosup.app.util.etaMinutes
import com.cosup.app.util.formatDate
import com.cosup.app.util.formatDist
import com.cosup.app.util.formatTime
import com.cosup.app.util.mapsUrl
import com.cosup.app.util.openUrl

@Composable
fun ServiceCard(service: ServiceDto, modifier: Modifier = Modifier, onFindSite: (() -> Unit)? = null) {
    CosupCard(modifier) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                ServiceIcon(service.iconPath)
                Text(service.serviceName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            Text(service.serviceDescription, style = MaterialTheme.typography.bodyMedium)
            if (onFindSite != null) {
                OutlinedButton(onClick = onFindSite) { Text("Find a site") }
            }
        }
    }
}

@Composable
fun NewsCard(news: NewsDto, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    CosupCard(modifier) {
        RemoteImage(news.coverImg, Modifier.fillMaxWidth().height(180.dp), ContentScale.Crop)
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!news.category.isNullOrBlank()) Badge(news.category)
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(news.author, formatDate(news.publishedAt).ifBlank { null }).joinToString(" \u00B7 "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(news.body, style = MaterialTheme.typography.bodyMedium)
            if (!news.sourceUrl.isNullOrBlank()) {
                OutlinedButton(onClick = { ctx.openUrl(news.sourceUrl) }) { Text("Read the full article \u2197") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SiteCard(
    site: SiteDto,
    distanceKm: Double?,
    nearest: Boolean,
    me: Pair<Double, Double>?,
    onClick: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    CosupCard(modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick)) {
        Column(
            Modifier.padding(18.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(site.siteName, style = MaterialTheme.typography.titleMedium)
                if (site.isMainCentre) Badge("Main centre")
                if (nearest) Badge("Nearest to you")
            }
            Text(site.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!site.operatingHours.isNullOrBlank()) Text(site.operatingHours, style = MaterialTheme.typography.bodySmall)
            if (distanceKm != null) {
                Text(
                    "\uD83D\uDCCD \u2248 ${formatDist(distanceKm)} away",
                    color = CosupGreenDark, fontWeight = FontWeight.Bold
                )
            }
            if (site.services.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    site.services.forEach { Badge(it) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelMode.entries.forEach { mode ->
                    val icon = when (mode) { TravelMode.DRIVE -> "\uD83D\uDE97"; TravelMode.WALK -> "\uD83D\uDEB6"; TravelMode.TRANSIT -> "\uD83D\uDE8C" }
                    Button(
                        onClick = { ctx.openUrl(mapsUrl(site.latitude, site.longitude, mode, me?.first, me?.second)) },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CosupGreen)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$icon ${mode.label}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            if (distanceKm != null) {
                                Text(formatTime(etaMinutes(distanceKm, mode)), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            if (!site.phone.isNullOrBlank()) {
                OutlinedButton(onClick = { onCall(); ctx.dial(site.phone) }) { Text("Call ${site.phone}") }
            }
        }
    }
}
