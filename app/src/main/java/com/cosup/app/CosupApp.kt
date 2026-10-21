package com.cosup.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cosup.app.data.ApiClient
import com.cosup.app.data.SessionInfo
import com.cosup.app.data.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CosupApp : Application() {
    lateinit var store: SessionStore
        private set
    lateinit var client: ApiClient
        private set

    /** Read by the OkHttp interceptor on every request. */
    @Volatile
    var tokenCache: String? = null

    var initialSession: SessionInfo? = null
        private set

    override fun onCreate() {
        super.onCreate()
        store = SessionStore(this)
        // One quick local read so the first API call carries the saved token and the saved theme is applied up front.
        initialSession = runBlocking { store.session.first() }
        tokenCache = initialSession?.token
        AppCompatDelegate.setDefaultNightMode(nightModeFor(runBlocking { store.theme.first() }))
        client = ApiClient { tokenCache }
    }

    companion object {
        fun nightModeFor(theme: String) = when (theme) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }
}
