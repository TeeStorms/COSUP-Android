package com.cosup.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cosup.app.data.AdminDashboard
import com.cosup.app.data.AdminNewsRow
import com.cosup.app.data.AdminReferral
import com.cosup.app.data.AdminSite
import com.cosup.app.data.ActivityPage
import com.cosup.app.data.AuditRowDto
import com.cosup.app.data.SiteEditBody
import com.cosup.app.databinding.FragmentAdminBinding
import com.cosup.app.databinding.ItemAdminActivityBinding
import com.cosup.app.databinding.ItemAdminHeaderBinding
import com.cosup.app.databinding.ItemAdminLineBinding
import com.cosup.app.databinding.ItemAdminNewsBinding
import com.cosup.app.databinding.ItemAdminNewsFormBinding
import com.cosup.app.databinding.ItemAdminReferralBinding
import com.cosup.app.databinding.ItemAdminSiteBinding
import com.cosup.app.databinding.ViewStatBinding
import com.cosup.app.ui.AdminState
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.bindOptions
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.dp
import com.cosup.app.ui.common.str
import com.cosup.app.ui.common.visible
import com.cosup.app.util.REFERRAL_STATUSES
import com.cosup.app.util.dial
import com.cosup.app.util.formatDate
import com.cosup.app.util.formatDateTime
import com.google.android.material.tabs.TabLayout

class AdminFragment : Fragment() {
    private var _b: FragmentAdminBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    private val sections = listOf(
        "dashboard" to "Dashboard", "referrals" to "Referrals", "news" to "News",
        "sites" to "Sites", "audit" to "Audit log", "activity" to "Guest activity"
    )
    private var current = 0
    private var lastRendered: Any? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAdminBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sections.forEach { b.adminTabs.addTab(b.adminTabs.newTab().setText(it.second)) }
        b.adminTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                current = tab.position
                lastRendered = null
                b.adminContent.removeAllViews()
                vm.loadAdmin(sections[current].first)
                render(vm.admin.value)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) { vm.loadAdmin(sections[current].first) }
        })
        b.btnRetry.setOnClickListener { vm.loadAdmin(sections[current].first) }
        collectFlow(vm.admin) { render(it) }
        vm.loadAdmin(sections[0].first)
    }

    private fun dataFor(state: AdminState): Any? = when (sections[current].first) {
        "dashboard" -> state.dashboard
        "referrals" -> state.referrals
        "news" -> state.news
        "sites" -> state.sites
        "audit" -> state.audit
        else -> state.activity
    }

    private fun render(state: AdminState) {
        val data = dataFor(state)
        b.loading.visible(state.loading && data == null)
        b.errorBox.visible(!state.loading && state.error != null && data == null)
        b.errorText.text = state.error
        if (data == null || data === lastRendered) return // do not redraw (and wipe typed text) for unrelated updates
        lastRendered = data
        b.adminContent.removeAllViews()
        @Suppress("UNCHECKED_CAST")
        when (sections[current].first) {
            "dashboard" -> dashboard(data as AdminDashboard)
            "referrals" -> referrals(data as List<AdminReferral>)
            "news" -> news(data as List<AdminNewsRow>)
            "sites" -> sites(data as List<AdminSite>)
            "audit" -> audit(data as List<AuditRowDto>)
            else -> activity(data as ActivityPage)
        }
    }

    // ------------------------------------------------------------ small builders
    private val content get() = b.adminContent

    private fun header(text: String) {
        content.addView(ItemAdminHeaderBinding.inflate(layoutInflater, content, false).root.also { it.text = text })
    }

    private fun line(title: String, sub: String) {
        val l = ItemAdminLineBinding.inflate(layoutInflater, content, false)
        l.lineTitle.text = title
        l.lineSub.text = sub
        content.addView(l.root)
    }

    private fun statRow(vararg items: Pair<String, Int>) {
        val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        items.forEach { (label, value) ->
            val s = ViewStatBinding.inflate(layoutInflater, row, false)
            s.statValue.text = value.toString()
            s.statLabel.text = label
            s.root.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { setMargins(content.dp(4), content.dp(4), content.dp(4), content.dp(4)) }
            row.addView(s.root)
        }
        content.addView(row)
    }

    // ------------------------------------------------------------ sections
    private fun dashboard(d: AdminDashboard) {
        statRow("Users" to d.users, "Sites" to d.sites, "Referrals" to d.referrals)
        statRow("Pending" to d.pending, "News posts" to d.news, "Hopeline clicks" to d.hopeline)
        header("Pending referrals")
        if (d.pendingList.isEmpty()) line("None pending.", "")
        d.pendingList.forEach { line("#${it.referralId}  ${it.fullName}", listOfNotNull(it.serviceNeeded, it.source).joinToString(" \u00B7 ")) }
        header("Referrals by status")
        d.byStatus.forEach { (k, v) -> line(k, v.toString()) }
        header("Recent users")
        d.recentUsers.forEach { line(it.name, it.email) }
        header("Recent activity")
        d.audit.forEach { line(it.action, "${it.admin} \u00B7 ${formatDateTime(it.at)}") }
    }

    private fun referrals(list: List<AdminReferral>) {
        if (list.isEmpty()) line("No referrals yet.", "")
        list.forEach { r ->
            val i = ItemAdminReferralBinding.inflate(layoutInflater, content, false)
            var selected = r.status
            i.arName.text = "#${r.referralId}  ${r.fullName}"
            i.arPhone.text = r.contactNumber
            i.arPhone.setOnClickListener { requireContext().dial(r.contactNumber) }
            i.arEmail.visible(!r.email.isNullOrBlank()); i.arEmail.text = r.email
            i.arMeta.text = listOfNotNull(r.serviceNeeded, r.preferredArea, r.source, formatDate(r.submittedAt).ifBlank { null }).joinToString(" \u00B7 ")
            i.arMessage.visible(!r.message.isNullOrBlank()); i.arMessage.text = r.message
            i.ddStatus.bindOptions(REFERRAL_STATUSES.map { it to it }, r.status) {
                selected = it
                i.arSave.isEnabled = it != r.status
            }
            i.arSave.setOnClickListener { vm.adminUpdateReferral(r.referralId, selected) }
            content.addView(i.root)
        }
    }

    private fun news(list: List<AdminNewsRow>) {
        val f = ItemAdminNewsFormBinding.inflate(layoutInflater, content, false)
        f.nPublish.setOnClickListener {
            val title = f.etNTitle.str.trim()
            val body = f.etNBody.str.trim()
            f.tilNTitle.error = if (title.isEmpty()) "Title is required." else null
            f.tilNBody.error = if (body.isEmpty()) "Body is required." else null
            if (title.isNotEmpty() && body.isNotEmpty()) {
                vm.adminCreateNews(title, body, f.etNCategory.str.trim().ifEmpty { null })
            }
        }
        content.addView(f.root)
        list.forEach { n ->
            val i = ItemAdminNewsBinding.inflate(layoutInflater, content, false)
            i.anTitle.text = "#${n.newsId}  ${n.title}"
            i.anMeta.text = listOfNotNull(n.category, formatDate(n.publishedAt).ifBlank { null }).joinToString(" \u00B7 ")
            i.anState.text = if (n.isPublished) "published" else "draft"
            i.anToggle.text = if (n.isPublished) "Unpublish" else "Publish"
            i.anToggle.setOnClickListener { vm.adminToggleNews(n.newsId) }
            content.addView(i.root)
        }
    }

    private fun sites(list: List<AdminSite>) {
        list.forEach { s ->
            val i = ItemAdminSiteBinding.inflate(layoutInflater, content, false)
            i.asMain.visible(s.isMainCentre)
            i.etSName.setText(s.siteName)
            i.etSAddress.setText(s.address)
            i.etSHours.setText(s.operatingHours.orEmpty())
            i.etSPhone.setText(s.phone.orEmpty())
            i.asSave.setOnClickListener {
                val name = i.etSName.str.trim()
                val address = i.etSAddress.str.trim()
                i.tilSName.error = if (name.isEmpty()) "Site name is required." else null
                i.tilSAddress.error = if (address.isEmpty()) "Address is required." else null
                if (name.isNotEmpty() && address.isNotEmpty()) {
                    vm.adminEditSite(
                        s.siteId,
                        SiteEditBody(name, address, i.etSHours.str.trim().ifEmpty { null }, i.etSPhone.str.trim().ifEmpty { null })
                    )
                }
            }
            content.addView(i.root)
        }
    }

    private fun audit(list: List<AuditRowDto>) {
        if (list.isEmpty()) line("Nothing logged yet.", "")
        list.forEach { line(it.action, "${it.admin} \u00B7 ${formatDateTime(it.at)}") }
    }

    private fun activity(a: ActivityPage) {
        statRow("Guests" to a.guests, "Events logged" to a.events, "Events today" to a.today)
        a.rows.forEach { r ->
            val i = ItemAdminActivityBinding.inflate(layoutInflater, content, false)
            i.acWho.text = r.who
            i.acRole.visible(r.role.isNotBlank()); i.acRole.text = r.role
            i.acAction.text = r.action
            i.acDetail.visible(!r.detail.isNullOrBlank()); i.acDetail.text = r.detail
            i.acMeta.text = listOfNotNull(formatDateTime(r.at).ifBlank { null }, r.page).joinToString(" \u00B7 ")
            content.addView(i.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
