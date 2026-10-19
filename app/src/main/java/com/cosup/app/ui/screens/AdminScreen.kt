package com.cosup.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosup.app.data.AdminNewsRow
import com.cosup.app.data.AdminReferral
import com.cosup.app.data.AdminSite
import com.cosup.app.data.AuditRowDto
import com.cosup.app.data.SiteEditBody
import com.cosup.app.ui.AdminState
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.components.Badge
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.ui.components.DropdownField
import com.cosup.app.ui.components.ErrorBox
import com.cosup.app.ui.components.LoadingBox
import com.cosup.app.ui.components.PageHead
import com.cosup.app.util.REFERRAL_STATUSES
import com.cosup.app.util.dial
import com.cosup.app.util.formatDate
import com.cosup.app.util.formatDateTime

private val SECTIONS = listOf(
    "dashboard" to "Dashboard",
    "referrals" to "Referrals",
    "news" to "News",
    "sites" to "Sites",
    "audit" to "Audit log",
    "activity" to "Guest activity"
)

@Composable
fun AdminScreen(vm: AppViewModel) {
    val state by vm.admin.collectAsState()
    val busy by vm.busy.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val section = SECTIONS[tab].first

    LaunchedEffect(section) { vm.loadAdmin(section) }

    Column(Modifier.fillMaxSize()) {
        PageHead("Authorised staff only", "Admin")
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
            SECTIONS.forEachIndexed { i, (_, label) ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
            }
        }
        val hasData = when (section) {
            "dashboard" -> state.dashboard != null
            "referrals" -> state.referrals != null
            "news" -> state.news != null
            "sites" -> state.sites != null
            "audit" -> state.audit != null
            else -> state.activity != null
        }
        when {
            state.error != null && !hasData -> ErrorBox(state.error!!) { vm.loadAdmin(section) }
            !hasData -> LoadingBox()
            else -> when (section) {
                "dashboard" -> DashboardTab(state)
                "referrals" -> ReferralsTab(state.referrals.orEmpty(), busy, vm::adminUpdateReferral)
                "news" -> NewsTab(state.news.orEmpty(), busy, vm::adminCreateNews, vm::adminToggleNews)
                "sites" -> SitesTab(state.sites.orEmpty(), busy, vm::adminEditSite)
                "audit" -> AuditTab(state.audit.orEmpty())
                else -> ActivityTab(state)
            }
        }
    }
}

private val listPadding = PaddingValues(20.dp)

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier) {
    CosupCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ------------------------------------------------------------------ dashboard
@Composable
private fun DashboardTab(state: AdminState) {
    val d = state.dashboard ?: return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Users", d.users, Modifier.weight(1f)); StatCard("Sites", d.sites, Modifier.weight(1f)); StatCard("Referrals", d.referrals, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Pending", d.pending, Modifier.weight(1f)); StatCard("News posts", d.news, Modifier.weight(1f)); StatCard("Hopeline clicks", d.hopeline, Modifier.weight(1f))
                }
            }
        }
        item {
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pending referrals", style = MaterialTheme.typography.titleMedium)
                    if (d.pendingList.isEmpty()) Text("None pending.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.pendingList.forEach { r ->
                        Text("#${r.referralId}  ${r.fullName}", fontWeight = FontWeight.Bold)
                        Text(listOfNotNull(r.serviceNeeded, r.source).joinToString(" \u00B7 "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Referrals by status", style = MaterialTheme.typography.titleMedium)
                    d.byStatus.forEach { (k, v) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Badge(k); Text(v.toString()) }
                    }
                    Text("Recent users", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                    d.recentUsers.forEach { u ->
                        Text(u.name, fontWeight = FontWeight.Bold)
                        Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recent activity", style = MaterialTheme.typography.titleMedium)
                    d.audit.forEach { AuditLine(it) }
                }
            }
        }
    }
}

@Composable
private fun AuditLine(a: AuditRowDto) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Text(a.action)
        Text("${a.admin} \u00B7 ${formatDateTime(a.at)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ------------------------------------------------------------------ referrals
@Composable
private fun ReferralsTab(list: List<AdminReferral>, busy: Boolean, onSave: (Int, String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (list.isEmpty()) item { Text("No referrals yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(list, key = { it.referralId }) { r ->
            val ctx = LocalContext.current
            var status by remember(r.referralId, r.status) { mutableStateOf(r.status) }
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("#${r.referralId}  ${r.fullName}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        r.contactNumber,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { ctx.dial(r.contactNumber) }
                    )
                    if (!r.email.isNullOrBlank()) Text(r.email, style = MaterialTheme.typography.bodySmall)
                    Text(
                        listOfNotNull(r.serviceNeeded, r.preferredArea, r.source, formatDate(r.submittedAt).ifBlank { null }).joinToString(" \u00B7 "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!r.message.isNullOrBlank()) Text(r.message)
                    DropdownField("Status", status, REFERRAL_STATUSES.map { it to it }, { status = it })
                    Button(onClick = { onSave(r.referralId, status) }, enabled = !busy && status != r.status) { Text("Save") }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ news
@Composable
private fun NewsTab(
    list: List<AdminNewsRow>, busy: Boolean,
    onCreate: (String, String, String?) -> Unit, onToggle: (Int) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Publish a news item", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        title, { title = it.take(200) }, label = { Text("Title") }, singleLine = true,
                        isError = tried && title.isBlank(), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        category, { category = it.take(60) }, label = { Text("Category") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        body, { body = it }, label = { Text("Body") }, minLines = 4,
                        isError = tried && body.isBlank(), modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            tried = true
                            if (title.isNotBlank() && body.isNotBlank()) {
                                onCreate(title.trim(), body.trim(), category.trim().ifBlank { null })
                                title = ""; category = ""; body = ""; tried = false
                            }
                        },
                        enabled = !busy
                    ) { Text("Publish") }
                }
            }
        }
        items(list, key = { it.newsId }) { n ->
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("#${n.newsId}  ${n.title}", fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(n.category, formatDate(n.publishedAt).ifBlank { null }).joinToString(" \u00B7 "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Badge(if (n.isPublished) "published" else "draft")
                        OutlinedButton(onClick = { onToggle(n.newsId) }, enabled = !busy) {
                            Text(if (n.isPublished) "Unpublish" else "Publish")
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ sites
@Composable
private fun SitesTab(list: List<AdminSite>, busy: Boolean, onSave: (Int, SiteEditBody) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(list, key = { it.siteId }) { s ->
            var name by remember(s) { mutableStateOf(s.siteName) }
            var address by remember(s) { mutableStateOf(s.address) }
            var hours by remember(s) { mutableStateOf(s.operatingHours.orEmpty()) }
            var phone by remember(s) { mutableStateOf(s.phone.orEmpty()) }
            var tried by remember(s) { mutableStateOf(false) }
            CosupCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (s.isMainCentre) Badge("Main centre")
                    OutlinedTextField(name, { name = it }, label = { Text("Site name") }, singleLine = true, isError = tried && name.isBlank(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(address, { address = it }, label = { Text("Address") }, isError = tried && address.isBlank(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(hours, { hours = it }, label = { Text("Operating hours") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(
                        onClick = {
                            tried = true
                            if (name.isNotBlank() && address.isNotBlank()) {
                                onSave(s.siteId, SiteEditBody(name.trim(), address.trim(), hours.trim().ifBlank { null }, phone.trim().ifBlank { null }))
                            }
                        },
                        enabled = !busy
                    ) { Text("Save") }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ audit + activity
@Composable
private fun AuditTab(list: List<AuditRowDto>) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (list.isEmpty()) item { Text("Nothing logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(list) { a -> CosupCard { Column(Modifier.padding(14.dp)) { AuditLine(a) } } }
    }
}

@Composable
private fun ActivityTab(state: AdminState) {
    val a = state.activity ?: return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Guests", a.guests, Modifier.weight(1f))
                StatCard("Events logged", a.events, Modifier.weight(1f))
                StatCard("Events today", a.today, Modifier.weight(1f))
            }
        }
        items(a.rows) { r ->
            CosupCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(r.who, fontWeight = FontWeight.Bold)
                        if (r.role.isNotBlank()) Badge(r.role)
                    }
                    Text(r.action)
                    if (!r.detail.isNullOrBlank()) Text(r.detail, style = MaterialTheme.typography.bodySmall)
                    Text(
                        listOfNotNull(formatDateTime(r.at).ifBlank { null }, r.page).joinToString(" \u00B7 "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
