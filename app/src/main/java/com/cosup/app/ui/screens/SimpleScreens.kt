package com.cosup.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cosup.app.R
import com.cosup.app.ui.ContentState
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.ui.components.ErrorBox
import com.cosup.app.ui.components.LoadingBox
import com.cosup.app.ui.components.NewsCard
import com.cosup.app.ui.components.PageHead
import com.cosup.app.ui.components.PartnersFooter
import com.cosup.app.ui.components.ServiceCard

@Composable
fun ServicesScreen(content: ContentState, onRetry: () -> Unit, onFindSite: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            PageHead("What we offer", "Our services", "Six core services, delivered with dignity and without judgement.")
        }
        if (content.loading) item { LoadingBox() }
        else if (content.error != null) item { ErrorBox(content.error, onRetry) }
        else items(content.services) { s -> ServiceCard(s, Modifier.padding(horizontal = 20.dp), onFindSite) }
        item { PartnersFooter() }
    }
}

@Composable
fun NewsScreen(content: ContentState, onRetry: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            PageHead(
                "COSUP in the news", "News",
                "Real stories about COSUP from the University of Pretoria, the media and research partners. Each story opens on the publisher's website."
            )
        }
        if (content.loading) item { LoadingBox() }
        else if (content.error != null) item { ErrorBox(content.error, onRetry) }
        else if (content.news.isEmpty()) item {
            Text("No news yet. Check back soon.", Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else items(content.news) { n -> NewsCard(n, Modifier.padding(horizontal = 20.dp)) }
        item { PartnersFooter() }
    }
}

@Composable
fun AboutScreen() {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageHead("Who we are", "About COSUP") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Our story", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "The Community Oriented Substance Use Programme (COSUP) has served Tshwane since 2015 across 16 sites. It is a partnership between the City of Tshwane and the University of Pretoria, providing rights-based harm reduction, treatment and reintegration support to people who use drugs.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Our approach is simple: meet people where they are, treat them with dignity, and connect them with the care and opportunities they need.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            Image(
                painterResource(R.drawable.about), contentDescription = "COSUP community",
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Our values", style = MaterialTheme.typography.headlineSmall)
                ValueCard("Dignity", "Every person is treated with respect and without judgement.")
                ValueCard("Confidentiality", "Your information is protected and handled under POPIA.")
                ValueCard("Community", "Care is built with and for the communities we serve.")
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Partners", style = MaterialTheme.typography.headlineSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Partner(R.drawable.city_of_tshwane, "City of Tshwane")
                    Partner(R.drawable.university_of_pretoria, "University of Pretoria")
                }
            }
        }
        item { PartnersFooter() }
    }
}

@Composable
private fun ValueCard(title: String, text: String) {
    CosupCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Partner(res: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Image(
            painterResource(res), contentDescription = name,
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit
        )
        Text(name, style = MaterialTheme.typography.labelMedium)
    }
}
