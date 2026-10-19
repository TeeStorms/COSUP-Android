package com.cosup.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cosup_session")

/** Keeps the API session token, role, name and theme choice on the device. */
class SessionStore(private val context: Context) {
    private val kToken = stringPreferencesKey("token")
    private val kRole = stringPreferencesKey("role")
    private val kName = stringPreferencesKey("name")
    private val kTheme = stringPreferencesKey("theme")

    val session: Flow<SessionInfo?> = context.dataStore.data.map { p -> p.toSession() }
    val theme: Flow<String> = context.dataStore.data.map { it[kTheme] ?: "system" }

    private fun Preferences.toSession(): SessionInfo? {
        val t = this[kToken] ?: return null
        return SessionInfo(t, this[kRole] ?: "user", this[kName] ?: "")
    }

    suspend fun save(info: SessionInfo) {
        context.dataStore.edit {
            it[kToken] = info.token
            it[kRole] = info.role
            it[kName] = info.name
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(kToken)
            it.remove(kRole)
            it.remove(kName)
        }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[kTheme] = value }
    }
}
