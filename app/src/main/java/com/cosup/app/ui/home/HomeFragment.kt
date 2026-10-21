package com.cosup.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cosup.app.MainActivity
import com.cosup.app.R
import com.cosup.app.databinding.FragmentHomeBinding
import com.cosup.app.databinding.ItemNewsHighlightBinding
import com.cosup.app.databinding.ItemServiceBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.ContentState
import com.cosup.app.ui.common.bindService
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.set
import com.cosup.app.ui.common.visible
import com.cosup.app.util.formatDate
import com.cosup.app.util.openUrl

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        b.statSites.set("-", "Sites")
        b.statServices.set("-", "Core services")
        b.statFree.set("Free", "and confidential")
        b.statHope.set("24/7", "Hopeline: 0800 611 197")

        b.btnReferral.setOnClickListener { main.goTab(R.id.contactFragment) }
        b.btnFindSite.setOnClickListener { main.goTab(R.id.sitesFragment) }
        b.btnMap.setOnClickListener { main.goTab(R.id.sitesFragment) }
        b.btnAllServices.setOnClickListener { main.goTab(R.id.servicesFragment) }
        b.btnAllNews.setOnClickListener { main.goTab(R.id.newsFragment) }
        b.btnAbout.setOnClickListener { main.navigateTo(R.id.aboutFragment) }
        b.hopeline.btnCall.setOnClickListener { main.callHopeline() }
        b.btnRetry.setOnClickListener { vm.refresh() }

        collectFlow(vm.content) { render(it) }
    }

    private fun render(c: ContentState) {
        b.loading.visible(c.loading)
        b.errorBox.visible(!c.loading && c.error != null)
        b.errorText.text = c.error
        b.contentBox.visible(!c.loading && c.error == null)
        if (c.loading || c.error != null) return

        b.statSites.set(c.sites.size.toString(), "Sites")
        b.statServices.set(c.services.size.toString(), "Core services")
        b.sitesTitle.text = "${c.sites.size} sites to reach you"

        b.servicesContainer.removeAllViews()
        c.services.forEach { s ->
            val item = ItemServiceBinding.inflate(layoutInflater, b.servicesContainer, false)
            bindService(item, s)
            b.servicesContainer.addView(item.root)
        }

        b.newsContainer.removeAllViews()
        b.newsEmpty.visible(c.news.isEmpty())
        c.news.take(3).forEach { n ->
            val item = ItemNewsHighlightBinding.inflate(layoutInflater, b.newsContainer, false)
            item.hlTitle.text = n.title
            item.hlMeta.text = listOfNotNull(n.author, formatDate(n.publishedAt).ifBlank { null }).joinToString(" \u00B7 ")
            item.root.setOnClickListener {
                val url = n.sourceUrl
                if (!url.isNullOrBlank()) requireContext().openUrl(url)
                else (requireActivity() as MainActivity).goTab(R.id.newsFragment)
            }
            b.newsContainer.addView(item.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
