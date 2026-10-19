package com.cosup.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosup.app.BuildConfig
import com.cosup.app.R
import com.cosup.app.ui.theme.CosupGreenPale
import com.cosup.app.ui.theme.CosupRed
import com.cosup.app.util.HOPELINE_DISPLAY
import com.cosup.app.util.imageUrl
import java.time.Year

/** Loads an image that lives in the website's /images folder (service icons, news covers...). */
@Composable
fun RemoteImage(path: String?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val url = imageUrl(BuildConfig.API_BASE_URL, path) ?: return
    AsyncImage(model = url, contentDescription = null, modifier = modifier, contentScale = contentScale)
}

@Composable
fun CosupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun Eyebrow(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun PageHead(eyebrow: String, title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Eyebrow(eyebrow)
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionHead(eyebrow: String, title: String) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Eyebrow(eyebrow)
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun Badge(text: String) {
    Surface(shape = CircleShape, color = CosupGreenPale) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF1F6B23),
            fontWeight = FontWeight.Bold
        )
    }
}

/** Service icons sit on a pale tile so they look right in light and dark mode. */
@Composable
fun ServiceIcon(path: String?, size: Int = 56) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape(14.dp)).background(CosupGreenPale),
        contentAlignment = Alignment.Center
    ) {
        RemoteImage(path, Modifier.fillMaxSize().padding(6.dp), ContentScale.Fit)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
fun HopelineBanner(onCall: () -> Unit) {
    CosupCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(
                painterResource(R.drawable.hopeline), contentDescription = "COSUP Hopeline",
                modifier = Modifier.fillMaxWidth().height(60.dp), contentScale = ContentScale.Fit
            )
            Text("Need to talk to someone now?", style = MaterialTheme.typography.titleMedium)
            Text("The COSUP Hopeline is free and confidential.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onCall,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CosupRed)
            ) { Text(HOPELINE_DISPLAY, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
fun PartnersFooter() {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Serving Tshwane since 2015 across 16 sites. A partnership between the City of Tshwane and the University of Pretoria.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.city_of_tshwane), "City of Tshwane", Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.university_of_pretoria), "University of Pretoria", Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
        }
        Text(
            "\u00A9 ${Year.now().value} COSUP \u00B7 The 5 Watermelons",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Read-only dropdown. `emptyLabel` is shown for the blank choice (e.g. "No preference"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>, // value to display text
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    emptyLabel: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val shown = options.firstOrNull { it.first == value }?.second ?: (if (value.isEmpty()) emptyLabel.orEmpty() else value)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = shown,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (emptyLabel != null) {
                DropdownMenuItem(text = { Text(emptyLabel) }, onClick = { onSelect(""); expanded = false })
            }
            options.forEach { (v, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(v); expanded = false })
            }
        }
    }
}
