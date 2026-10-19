package com.cosup.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosup.app.R
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.components.CosupCard
import com.cosup.app.util.findActivity
import com.cosup.app.util.isValidAlias
import com.cosup.app.util.isValidEmail
import com.cosup.app.util.isValidOptionalPhone
import androidx.compose.runtime.collectAsState

@Composable
fun AuthScreen(vm: AppViewModel, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val auth by vm.auth.collectAsState()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showForgot by rememberSaveable { mutableStateOf(false) }
    var showGuest by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    // Sign in
    var siEmail by rememberSaveable { mutableStateOf("") }
    var siPassword by rememberSaveable { mutableStateOf("") }
    // Register
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var rgEmail by rememberSaveable { mutableStateOf("") }
    var rgPassword by rememberSaveable { mutableStateOf("") }
    var rgConsent by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.clearAuthMessage()
        vm.authDone.collect { onDone() }
    }

    val error = localError ?: auth.error

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(R.drawable.logo), "COSUP",
            Modifier.height(64.dp), contentScale = ContentScale.Fit
        )
        Text("Welcome to COSUP", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Free and confidential. Sign in or create an account.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        )

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0; localError = null }, text = { Text("Sign in") })
            Tab(selected = tab == 1, onClick = { tab = 1; localError = null }, text = { Text("Register") })
        }

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
        }

        val activity = ctx.findActivity()
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { activity?.let { vm.signInWithProvider(it, "google.com") } },
                enabled = !auth.busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Continue with Google") }
            OutlinedButton(
                onClick = { activity?.let { vm.signInWithProvider(it, "yahoo.com") } },
                enabled = !auth.busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Continue with Yahoo") }
            OutlinedButton(
                onClick = { activity?.let { vm.signInWithProvider(it, "microsoft.com") } },
                enabled = !auth.busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Continue with Microsoft") }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HorizontalDivider(Modifier.weight(1f))
            Text("or use your email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.weight(1f))
        }

        if (tab == 0) {
            OutlinedTextField(
                siEmail, { siEmail = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                siPassword, { siPassword = it }, label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (siEmail.isBlank() || siPassword.isEmpty()) localError = "Enter your email and password."
                    else { localError = null; vm.signInWithEmail(siEmail, siPassword) }
                },
                enabled = !auth.busy, modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { BusyOr(auth.busy, "Sign in") }
            TextButton(onClick = { showForgot = true; vm.clearAuthMessage() }) { Text("Forgot password?") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(first, { first = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(last, { last = it }, label = { Text("Last name") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                phone, { phone = it }, label = { Text("Phone (optional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                rgEmail, { rgEmail = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                rgPassword, { rgPassword = it }, label = { Text("Password (min 6 characters)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth().clickable { rgConsent = !rgConsent }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(rgConsent, { rgConsent = it })
                Text("I accept the POPIA consent for my information to be processed by COSUP.", style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    localError = when {
                        first.isBlank() || last.isBlank() -> "Enter your first and last name."
                        !isValidOptionalPhone(phone) -> "Enter a valid phone number (digits only)."
                        !isValidEmail(rgEmail) -> "Enter a valid email address."
                        rgPassword.length < 6 -> "Password must be at least 6 characters."
                        !rgConsent -> "Please accept the POPIA consent to continue."
                        else -> null
                    }
                    if (localError == null) vm.register(first.trim(), last.trim(), phone.trim(), rgEmail, rgPassword)
                },
                enabled = !auth.busy, modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { BusyOr(auth.busy, "Create account") }
        }

        HorizontalDivider()
        OutlinedButton(
            onClick = { showGuest = true; localError = null; vm.clearAuthMessage() },
            enabled = !auth.busy, modifier = Modifier.fillMaxWidth()
        ) { Text("Continue as guest") }
        Text(
            "By continuing with Google, Yahoo or Microsoft you accept the POPIA consent.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    if (showForgot) ForgotDialog(
        initialEmail = siEmail, busy = auth.busy, info = auth.info,
        onSend = { vm.sendPasswordReset(it) }, onDismiss = { showForgot = false; vm.clearAuthMessage() }
    )
    if (showGuest) GuestDialog(
        busy = auth.busy, error = auth.error,
        onGo = { alias -> vm.signInAsGuest(alias) }, onDismiss = { showGuest = false; vm.clearAuthMessage() }
    )
}

@Composable
private fun BusyOr(busy: Boolean, label: String) {
    if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
    else Text(label)
}

@Composable
private fun ForgotDialog(initialEmail: String, busy: Boolean, info: String?, onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var err by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the email you registered with and we will send you a link to choose a new password.")
                OutlinedTextField(
                    email, { email = it }, label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
                )
                if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error)
                if (info != null) Text(info, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Signed up with Google, Yahoo or Microsoft? Close this and use that button to sign in instead.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                if (!isValidEmail(email)) err = "Enter a valid email address." else { err = null; onSend(email) }
            }) { Text(if (busy) "Sending..." else "Send reset link") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back to sign in") } }
    )
}

@Composable
private fun GuestDialog(busy: Boolean, error: String?, onGo: (String) -> Unit, onDismiss: () -> Unit) {
    var alias by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var err by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Continue as guest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No account needed. Choose a name to use in the app. We save your activity under that name, not your real name.")
                OutlinedTextField(
                    alias, { alias = it.take(30) }, label = { Text("Your guest name") },
                    placeholder = { Text("For example: Sunny") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth().clickable { consent = !consent }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(consent, { consent = it })
                    Text("I understand COSUP will save my activity under this name.", style = MaterialTheme.typography.bodySmall)
                }
                val shown = err ?: error
                if (shown != null) Text(shown, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                err = when {
                    !isValidAlias(alias) -> "Choose a name of 2 to 30 characters (letters, numbers, spaces, - _ .)."
                    !consent -> "Please tick the box so we can save your activity under this name."
                    else -> null
                }
                if (err == null) onGo(alias.trim().replace(Regex("\\s+"), " "))
            }) { Text(if (busy) "Starting..." else "Continue as guest") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back to sign in") } }
    )
}
