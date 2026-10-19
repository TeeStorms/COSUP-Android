package com.cosup.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cosup.app.data.ProfileUpdate
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.components.Badge
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.ui.components.DropdownField
import com.cosup.app.ui.components.ErrorBox
import com.cosup.app.ui.components.LoadingBox
import com.cosup.app.ui.components.PageHead
import com.cosup.app.util.LANGUAGES
import com.cosup.app.util.formatDate
import com.cosup.app.util.isValidOptionalPhone

@Composable
fun ProfileScreen(vm: AppViewModel, onSignIn: () -> Unit, onAdmin: () -> Unit, onSignOut: () -> Unit) {
    val session by vm.session.collectAsState()
    val profile by vm.profile.collectAsState()
    val error by vm.profileError.collectAsState()
    val busy by vm.busy.collectAsState()
    val isGuest = session?.role == "guest"

    LaunchedEffect(session?.token) { if (session != null && !isGuest) vm.loadProfile() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageHead("Your account", "My profile")

        if (session == null) {
            CosupCard(Modifier.padding(horizontal = 20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sign in to see and edit your details.")
                    Button(onClick = onSignIn) { Text("Sign in / Register") }
                }
            }
            return@Column
        }

        if (isGuest) {
            CosupCard(Modifier.padding(horizontal = 20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Browsing as ${session?.name}", fontWeight = FontWeight.Bold)
                    Text("Guests do not have a profile. Create a free account to save your details and see your referrals.")
                    Button(onClick = onSignIn) { Text("Create a free account") }
                    OutlinedButton(onClick = onSignOut) { Text("End guest session") }
                }
            }
            return@Column
        }

        val p = profile
        if (p == null) {
            if (error != null) ErrorBox(error!!, vm::loadProfile) else LoadingBox()
            return@Column
        }

        var first by remember(p) { mutableStateOf(p.firstName) }
        var last by remember(p) { mutableStateOf(p.lastName) }
        var phone by remember(p) { mutableStateOf(p.phone.orEmpty()) }
        var lang by remember(p) { mutableStateOf(p.langPref) }
        var tried by remember(p) { mutableStateOf(false) }

        CosupCard(Modifier.padding(horizontal = 20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(p.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        first, { first = it }, label = { Text("First name") }, singleLine = true,
                        isError = tried && first.isBlank(), modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        last, { last = it }, label = { Text("Last name") }, singleLine = true,
                        isError = tried && last.isBlank(), modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    phone, { phone = it }, label = { Text("Phone") }, singleLine = true,
                    isError = tried && !isValidOptionalPhone(phone),
                    supportingText = { if (tried && !isValidOptionalPhone(phone)) Text("Enter a valid phone number (digits only).") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
                )
                DropdownField("Language", lang, LANGUAGES, { lang = it })
                Button(
                    onClick = {
                        tried = true
                        if (first.isNotBlank() && last.isNotBlank() && isValidOptionalPhone(phone)) {
                            vm.saveProfile(ProfileUpdate(first.trim(), last.trim(), phone.trim().ifBlank { null }, lang))
                        }
                    },
                    enabled = !busy, modifier = Modifier.fillMaxWidth()
                ) { Text(if (busy) "Saving..." else "Save changes") }
            }
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("My referrals", style = MaterialTheme.typography.titleMedium)
            if (p.referrals.isEmpty()) {
                Text("You have not submitted a referral yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            p.referrals.forEach { r ->
                CosupCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(r.serviceNeeded ?: "General referral", fontWeight = FontWeight.Bold)
                            Badge(r.status.replaceFirstChar { it.uppercase() })
                        }
                        Text(
                            listOfNotNull(formatDate(r.submittedAt).ifBlank { null }, r.preferredArea).joinToString(" \u00B7 "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (session?.role == "admin") {
                Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Open admin panel") }
            }
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        }
    }
}
