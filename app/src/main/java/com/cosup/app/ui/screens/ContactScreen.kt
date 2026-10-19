package com.cosup.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosup.app.data.ReferralRequest
import com.cosup.app.data.SessionInfo
import com.cosup.app.ui.ContentState
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.ui.components.DropdownField
import com.cosup.app.ui.components.PageHead
import com.cosup.app.util.HOPELINE_DISPLAY
import com.cosup.app.util.isValidEmail
import com.cosup.app.util.isValidPhone

@Composable
fun ContactScreen(
    content: ContentState,
    session: SessionInfo?,
    busy: Boolean,
    onSubmit: (ReferralRequest, () -> Unit) -> Unit,
    onFindSite: () -> Unit,
    onCall: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf(session?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var service by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val nameError = submitted && (fullName.isBlank() || fullName.length > 160)
    val phoneError = submitted && !isValidPhone(phone)
    val emailError = submitted && email.isNotBlank() && !isValidEmail(email)
    val messageError = message.length > 1000
    val consentError = submitted && !consent

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageHead(
            "Get help", "Self-referral form",
            "A COSUP caseworker will contact you. You can also call the Hopeline on $HOPELINE_DISPLAY."
        )
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniCard("Find a clinic", "Open the site map", Modifier.weight(1f), onFindSite)
            MiniCard("Call Hopeline", HOPELINE_DISPLAY, Modifier.weight(1f), onCall)
        }
        CosupCard(Modifier.padding(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it }, label = { Text("Full name") },
                    isError = nameError, supportingText = { if (nameError) Text("Please enter your full name.") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("Contact number") },
                    isError = phoneError, supportingText = { if (phoneError) Text("Enter a valid phone number (digits only).") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, label = { Text("Email (optional)") },
                    isError = emailError, supportingText = { if (emailError) Text("Enter a valid email address.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "Preferred site", value = area,
                    options = content.sites.map { it.siteName to it.siteName },
                    onSelect = { area = it }, emptyLabel = "No preference"
                )
                DropdownField(
                    label = "Service needed", value = service,
                    options = content.services.map { it.serviceName to it.serviceName },
                    onSelect = { service = it }, emptyLabel = "Not sure"
                )
                OutlinedTextField(
                    value = message, onValueChange = { message = it }, label = { Text("Message") },
                    isError = messageError, supportingText = { Text("${message.length}/1000") },
                    minLines = 4, modifier = Modifier.fillMaxWidth()
                )
                Row(
                    Modifier.fillMaxWidth().clickable { consent = !consent },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = consent, onCheckedChange = { consent = it })
                    Text("I accept the POPIA consent for COSUP to process my information.", style = MaterialTheme.typography.bodySmall)
                }
                if (consentError) {
                    Text("Please accept the POPIA consent to continue.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        submitted = true
                        if (fullName.isNotBlank() && fullName.length <= 160 && isValidPhone(phone) &&
                            (email.isBlank() || isValidEmail(email)) && !messageError && consent
                        ) {
                            onSubmit(
                                ReferralRequest(
                                    fullName = fullName.trim(), contactNumber = phone.trim(),
                                    email = email.trim().ifBlank { null },
                                    preferredArea = area.ifBlank { null },
                                    serviceNeeded = service.ifBlank { null },
                                    message = message.trim().ifBlank { null },
                                    consent = true
                                )
                            ) {
                                phone = ""; email = ""; area = ""; service = ""; message = ""; consent = false; submitted = false
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Submit referral")
                }
            }
        }
    }
}

@Composable
private fun MiniCard(title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    CosupCard(modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
