package com.cosup.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cosup.app.data.SiteDto
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

/** OpenStreetMap view - the same tiles the website's Leaflet map uses. No API key needed. */
@Composable
fun SitesMap(
    sites: List<SiteDto>,
    me: Pair<Double, Double>?,
    focus: SiteDto?,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = ctx.packageName
            osmdroidBasePath = File(ctx.cacheDir, "osmdroid")
            osmdroidTileCache = File(ctx.cacheDir, "osmdroid/tiles")
        }
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(10.0)
            controller.setCenter(GeoPoint(-25.72, 28.2)) // Pretoria, same default as the website
        }
    }
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.overlays.clear()
            sites.forEach { s ->
                val m = Marker(mv)
                m.position = GeoPoint(s.latitude, s.longitude)
                m.title = s.siteName
                m.snippet = s.address
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mv.overlays.add(m)
            }
            if (me != null) {
                val m = Marker(mv)
                m.position = GeoPoint(me.first, me.second)
                m.title = "You are here"
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mv.overlays.add(m)
            }
            if (focus != null) {
                mv.controller.animateTo(GeoPoint(focus.latitude, focus.longitude))
                mv.controller.setZoom(14.0)
            }
            mv.invalidate()
        }
    )
}
