package com.cosup.app.auth

import android.app.Activity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

/** Same Firebase project as the website. Every method returns a Firebase ID token for the API to verify. */
class FirebaseAuthManager {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    suspend fun signInWithEmail(email: String, password: String): String {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Sign-in failed.")
        return idToken(user)
    }

    suspend fun register(email: String, password: String, first: String, last: String): String {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Could not create the account.")
        user.updateProfile(userProfileChangeRequest { displayName = "$first $last".trim() }).await()
        return idToken(user)
    }

    /** providerId: "google.com", "yahoo.com" or "microsoft.com". */
    suspend fun signInWithProvider(activity: Activity, providerId: String): String {
        val provider = OAuthProvider.newBuilder(providerId)
        if (providerId != "yahoo.com") provider.addCustomParameter("prompt", "select_account")
        val pending = auth.pendingAuthResult
        val result = (pending ?: auth.startActivityForSignInWithProvider(activity, provider.build())).await()
        val user = result.user ?: error("Sign-in failed.")
        return idToken(user)
    }

    suspend fun signInAsGuest(alias: String): String {
        val user = auth.signInAnonymously().await().user ?: error("Could not start a guest session.")
        user.updateProfile(userProfileChangeRequest { displayName = alias }).await()
        return idToken(user)
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun signOut() = auth.signOut()

    private suspend fun idToken(user: FirebaseUser): String =
        user.getIdToken(true).await().token ?: error("Could not read the sign-in token.")
}

/** Friendly versions of the same messages the website shows. */
fun Throwable.toAuthMessage(): String {
    if (this is FirebaseNetworkException) return "Network problem. Check your connection."
    if (this is FirebaseAuthException) {
        return when (errorCode) {
            "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> "Incorrect email or password."
            "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already registered. Try signing in."
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "This email is already registered with a different sign-in method."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a moment and try again."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in option has not been switched on yet."
            else -> "Sign-in failed. Please try again."
        }
    }
    return message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
