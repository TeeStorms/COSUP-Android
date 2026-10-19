package com.cosup.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cosup.app.data.SiteDto
import com.cosup.app.ui.ContentState
import com.cosup.app.ui.components.ErrorBox
import com.cosup.app.ui.components.LoadingBox
import com.cosup.app.ui.components.SiteCard
import com.cosup.app.util.distanceKm
import com.cosup.app.util.formatDist
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun FindSiteScreen(content: ContentState, onRetry: () -> Unit, onCallLogged: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var me by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var msg by remember { mutableStateOf("") }
    var focus by remember { mutableStateOf<SiteDto?>(null) }

    fun fetchLocation() {
        msg = "Finding your location..."
        scope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(ctx)
                val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (loc == null) {
                    msg = "Could not get your location. Please try again."
                } else {
                    me = loc.latitude to loc.longitude
                    focus = null
                    val nearest = content.sites.minByOrNull { distanceKm(loc.latitude, loc.longitude, it.latitude, it.longitude) }
                    msg = if (nearest != null)
                        "Showing the nearest sites first. Nearest: ${nearest.siteName} (\u2248 ${formatDist(distanceKm(loc.latitude, loc.longitude, nearest.latitude, nearest.longitude))})."
                    else ""
                }
            } catch (e: Exception) {
                msg = "Could not get your location. Please try again."
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) fetchLocation()
        else msg = "Location access was blocked. Allow it in Settings, then try again."
    }

    val q = query.trim().lowercase()
    val filtered = content.sites
        .filter { s -> q.isEmpty() || (s.siteName + " " + s.address + " " + s.services.joinToString(" ")).lowercase().contains(q) }
        .let { list ->
            val m = me
            if (m == null) list else list.sortedBy { distanceKm(m.first, m.second, it.latitude, it.longitude) }
        }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("LOCATOR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("Find a site", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Site, suburb or service") },
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }) { Text("\uD83D\uDCCD Near me") }
            }
            if (msg.isNotBlank()) Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (content.loading) {
            LoadingBox()
        } else if (content.error != null) {
            ErrorBox(content.error, onRetry)
        } else {
            SitesMap(sites = filtered, me = me, focus = focus, modifier = Modifier.fillMaxWidth().height(230.dp))
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.siteId }) { s ->
                    val m = me
                    val km = if (m != null) distanceKm(m.first, m.second, s.latitude, s.longitude) else null
                    SiteCard(
                        site = s, distanceKm = km,
                        nearest = m != null && s.siteId == filtered.firstOrNull()?.siteId,
                        me = m, onClick = { focus = s }, onCall = onCallLogged
                    )
                }
                item {
                    Text(
                        "Distances are straight-line estimates and travel times are approximate. Tap Drive, Walk or Transit for the live route in Google Maps.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
