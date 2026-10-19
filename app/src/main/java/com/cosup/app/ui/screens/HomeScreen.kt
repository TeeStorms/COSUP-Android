package com.cosup.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosup.app.R
import com.cosup.app.ui.ContentState
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.ui.components.ErrorBox
import com.cosup.app.ui.components.HopelineBanner
import com.cosup.app.ui.components.LoadingBox
import com.cosup.app.ui.components.PartnersFooter
import com.cosup.app.ui.components.SectionHead
import com.cosup.app.ui.components.ServiceCard
import com.cosup.app.ui.theme.CosupGreen
import com.cosup.app.util.HOPELINE_DISPLAY
import com.cosup.app.util.formatDate
import com.cosup.app.util.openUrl

@Composable
fun HomeScreen(
    content: ContentState,
    onRetry: () -> Unit,
    onReferral: () -> Unit,
    onFindSite: () -> Unit,
    onServices: () -> Unit,
    onNews: () -> Unit,
    onAbout: () -> Unit,
    onCall: () -> Unit
) {
    val ctx = LocalContext.current
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Hero(onReferral, onFindSite) }
        item {
            Stats(
                sites = if (content.loading) "-" else content.sites.size.toString(),
                services = if (content.loading) "-" else content.services.size.toString()
            )
        }

        if (content.loading) {
            item { LoadingBox() }
        } else if (content.error != null) {
            item { ErrorBox(content.error, onRetry) }
        } else {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) { SectionHead("What we offer", "Our services") }
            }
            items(content.services) { s ->
                ServiceCard(s, Modifier.padding(horizontal = 20.dp))
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.Center) {
                    OutlinedButton(onClick = onServices) { Text("See all services") }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHead("Find a site", "${content.sites.size} sites to reach you")
                    Text(
                        "Search by suburb or service, see opening hours and call a site directly.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onFindSite) { Text("Open the map") }
                }
            }
            item {
                CosupCard(Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("News highlights", style = MaterialTheme.typography.titleMedium)
                        if (content.news.isEmpty()) {
                            Text("Check back soon for updates.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        content.news.take(3).forEachIndexed { i, n ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Column(
                                Modifier.fillMaxWidth().clickable {
                                    if (!n.sourceUrl.isNullOrBlank()) ctx.openUrl(n.sourceUrl) else onNews()
                                },
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(n.title, fontWeight = FontWeight.Bold)
                                Text(
                                    listOfNotNull(n.author, formatDate(n.publishedAt).ifBlank { null }).joinToString(" \u00B7 "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(onClick = onNews) { Text("All news") }
                    }
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 20.dp)) { HopelineBanner(onCall) } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedButton(onClick = onAbout) { Text("About COSUP") }
            }
        }
        item { PartnersFooter() }
    }
}

@Composable
private fun Hero(onReferral: () -> Unit, onFindSite: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(400.dp)) {
        Image(
            painterResource(R.drawable.banner), contentDescription = "COSUP community banner",
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x66000000), Color(0xE60D1F15)))
            )
        )
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(shape = CircleShape, color = Color(0x26FFFFFF)) {
                Text(
                    "Community Oriented Substance Use Programme",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Support that meets you where you are.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.headlineLarge, color = Color.White
            )
            Text(
                "Free, confidential harm-reduction and treatment services across Pretoria and surrounding communities.",
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                color = Color(0xE6FFFFFF)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onReferral) { Text("Self-referral") }
                OutlinedButton(
                    onClick = onFindSite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Find a site near you") }
            }
        }
    }
}

@Composable
private fun Stats(sites: String, services: String) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat(sites, "Sites", Modifier.weight(1f))
            Stat(services, "Core services", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("Free", "and confidential", Modifier.weight(1f))
            Stat("24/7", "Hopeline: $HOPELINE_DISPLAY", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    CosupCard(modifier) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = CosupGreen, fontWeight = FontWeight.ExtraBold)
            Text(
                label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
