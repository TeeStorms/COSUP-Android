package com.cosup.app.ui.sites

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cosup.app.data.SiteDto
import com.cosup.app.databinding.FragmentSitesBinding
import com.cosup.app.databinding.ItemSiteBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.SimpleListAdapter
import com.cosup.app.ui.common.bindSite
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.visible
import com.cosup.app.util.distanceKm
import com.cosup.app.util.formatDist
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File

class SitesFragment : Fragment() {
    private var _b: FragmentSitesBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    private var sites: List<SiteDto> = emptyList()
    private var me: Pair<Double, Double>? = null
    private var focus: SiteDto? = null
    private var query = ""
    private lateinit var adapter: SimpleListAdapter<SiteRow, ItemSiteBinding>

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) fetchLocation()
            else showMessage("Location access was blocked. Allow it in Settings, then try again.")
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // osmdroid must be configured before the MapView is inflated.
        val ctx = requireContext()
        Configuration.getInstance().apply {
            userAgentValue = ctx.packageName
            osmdroidBasePath = File(ctx.cacheDir, "osmdroid")
            osmdroidTileCache = File(ctx.cacheDir, "osmdroid/tiles")
        }
        _b = FragmentSitesBinding.inflate(inflater, container, false)
        return b.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.map.setTileSource(TileSourceFactory.MAPNIK)
        b.map.setMultiTouchControls(true)
        b.map.controller.setZoom(10.0)
        b.map.controller.setCenter(GeoPoint(-25.72, 28.2)) // Pretoria, same default as the website
        // Let the map pan inside the scrolling page.
        b.map.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        adapter = SimpleListAdapter<SiteRow, ItemSiteBinding>(
            ItemSiteBinding::inflate,
            { ib, row ->
                bindSite(ib, row,
                    onClick = {
                        focus = row.site
                        drawMap(currentList())
                        b.scroll.smoothScrollTo(0, b.map.top)
                    },
                    onCall = { vm.logHopeline("call") })
            },
            { x, y -> x.site.siteId == y.site.siteId }
        )
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter

        b.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty()
            focus = null
            refresh()
        }
        b.btnNearMe.setOnClickListener {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        b.btnRetry.setOnClickListener { vm.refresh() }

        collectFlow(vm.content) { c ->
            b.loading.visible(c.loading)
            b.errorBox.visible(!c.loading && c.error != null)
            b.errorText.text = c.error
            b.list.visible(!c.loading && c.error == null)
            sites = c.sites
            refresh()
        }
    }

    private fun currentList(): List<SiteDto> {
        val q = query.trim().lowercase()
        var list = sites.filter { s ->
            q.isEmpty() || (s.siteName + " " + s.address + " " + s.services.joinToString(" ")).lowercase().contains(q)
        }
        val m = me
        if (m != null) list = list.sortedBy { distanceKm(m.first, m.second, it.latitude, it.longitude) }
        return list
    }

    private fun refresh() {
        val list = currentList()
        val m = me
        adapter.submitList(list.mapIndexed { i, s ->
            SiteRow(s, m?.let { distanceKm(it.first, it.second, s.latitude, s.longitude) }, m != null && i == 0, m)
        })
        drawMap(list)
    }

    private fun drawMap(list: List<SiteDto>) {
        val map = _b?.map ?: return
        map.overlays.clear()
        list.forEach { s ->
            val marker = Marker(map)
            marker.position = GeoPoint(s.latitude, s.longitude)
            marker.title = s.siteName
            marker.snippet = s.address
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(marker)
        }
        me?.let {
            val marker = Marker(map)
            marker.position = GeoPoint(it.first, it.second)
            marker.title = "You are here"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            map.overlays.add(marker)
        }
        focus?.let {
            map.controller.animateTo(GeoPoint(it.latitude, it.longitude))
            map.controller.setZoom(14.0)
        }
        map.invalidate()
    }

    private fun showMessage(text: String) {
        _b?.locateMsg?.let {
            it.text = text
            it.visible(text.isNotBlank())
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        showMessage("Finding your location...")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(requireContext())
                val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (_b == null) return@launch
                if (loc == null) {
                    showMessage("Could not get your location. Please try again.")
                } else {
                    me = loc.latitude to loc.longitude
                    focus = null
                    refresh()
                    val nearest = sites.minByOrNull { distanceKm(loc.latitude, loc.longitude, it.latitude, it.longitude) }
                    showMessage(
                        if (nearest != null)
                            "Showing the nearest sites first. Nearest: ${nearest.siteName} (\u2248 ${formatDist(distanceKm(loc.latitude, loc.longitude, nearest.latitude, nearest.longitude))})."
                        else ""
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showMessage("Could not get your location. Please try again.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        _b?.map?.onResume()
    }

    override fun onPause() {
        _b?.map?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        _b?.map?.onDetach()
        super.onDestroyView()
        _b = null
    }
}
