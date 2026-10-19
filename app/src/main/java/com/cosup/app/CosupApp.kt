package com.cosup.app

import android.app.Application
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
        // One quick local read so the very first API call already carries the saved token.
        initialSession = runBlocking { store.session.first() }
        tokenCache = initialSession?.token
        client = ApiClient { tokenCache }
    }
}
