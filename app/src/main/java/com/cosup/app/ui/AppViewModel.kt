package com.cosup.app.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cosup.app.CosupApp
import com.cosup.app.auth.FirebaseAuthManager
import com.cosup.app.auth.toAuthMessage
import com.cosup.app.data.ActivityPage
import com.cosup.app.data.AdminDashboard
import com.cosup.app.data.AdminNewsRow
import com.cosup.app.data.AdminReferral
import com.cosup.app.data.AdminSite
import com.cosup.app.data.AuditRowDto
import com.cosup.app.data.HopelineRequest
import com.cosup.app.data.NewsCreateBody
import com.cosup.app.data.SiteEditBody
import com.cosup.app.data.StatusBody
import com.cosup.app.data.LoginRequest
import com.cosup.app.data.NewsDto
import com.cosup.app.data.ProfileDto
import com.cosup.app.data.ProfileUpdate
import com.cosup.app.data.ReferralRequest
import com.cosup.app.data.ServiceDto
import com.cosup.app.data.SessionInfo
import com.cosup.app.data.SiteDto
import com.cosup.app.data.parseError
import com.cosup.app.data.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ContentState(
    val loading: Boolean = true,
    val error: String? = null,
    val services: List<ServiceDto> = emptyList(),
    val news: List<NewsDto> = emptyList(),
    val sites: List<SiteDto> = emptyList()
)

data class AdminState(
    val loading: Boolean = false,
    val error: String? = null,
    val dashboard: AdminDashboard? = null,
    val referrals: List<AdminReferral>? = null,
    val news: List<AdminNewsRow>? = null,
    val sites: List<AdminSite>? = null,
    val audit: List<AuditRowDto>? = null,
    val activity: ActivityPage? = null
)

data class AuthUi(val busy: Boolean = false, val error: String? = null, val info: String? = null)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val cosup = app as CosupApp
    private val api get() = cosup.client.api
    private val store = cosup.store
    private val firebase = FirebaseAuthManager()

    // ---- public content (home, services, sites, news) ----
    private val _content = MutableStateFlow(ContentState())
    val content: StateFlow<ContentState> = _content.asStateFlow()

    // ---- session + theme ----
    val session: StateFlow<SessionInfo?> =
        store.session.stateIn(viewModelScope, SharingStarted.Eagerly, cosup.initialSession)
    val theme: StateFlow<String> = store.theme.stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    // ---- sign-in ----
    private val _auth = MutableStateFlow(AuthUi())
    val auth: StateFlow<AuthUi> = _auth.asStateFlow()
    private val _authDone = Channel<Unit>(Channel.BUFFERED)
    val authDone = _authDone.receiveAsFlow()

    // ---- profile ----
    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()
    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    // ---- admin ----
    private val _admin = MutableStateFlow(AdminState())
    val admin: StateFlow<AdminState> = _admin.asStateFlow()

    // ---- one-off snackbar messages ----
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch { session.collect { cosup.tokenCache = it?.token } }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _content.update { it.copy(loading = true, error = null) }
            try {
                coroutineScope {
                    val services = async { api.services() }
                    val news = async { api.news() }
                    val sites = async { api.sites() }
                    _content.value = ContentState(false, null, services.await(), news.await(), sites.await())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _content.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }

    // -------------------------------------------------- auth
    fun clearAuthMessage() { _auth.value = AuthUi() }

    private fun runAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _auth.value = AuthUi(busy = true)
            try {
                block()
                _auth.value = AuthUi()
                _authDone.send(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException, is IOException -> e.toUserMessage()
                    else -> e.toAuthMessage()
                }
                _auth.value = AuthUi(error = msg)
            }
        }
    }

    private suspend fun completeLogin(idToken: String, first: String? = null, last: String? = null, phone: String? = null) {
        val r = api.login(LoginRequest(idToken, first, last, phone))
        cosup.tokenCache = r.token
        store.save(SessionInfo(r.token, r.role, r.name.ifBlank { "Guest" }))
        _profile.value = null
        refresh()
    }

    fun signInWithEmail(email: String, password: String) = runAuth {
        completeLogin(firebase.signInWithEmail(email, password))
    }

    fun register(first: String, last: String, phone: String, email: String, password: String) = runAuth {
        completeLogin(firebase.register(email, password, first, last), first, last, phone.ifBlank { null })
    }

    fun signInWithProvider(activity: Activity, providerId: String) = runAuth {
        completeLogin(firebase.signInWithProvider(activity, providerId))
    }

    fun signInAsGuest(alias: String) = runAuth {
        completeLogin(firebase.signInAsGuest(alias), first = alias)
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _auth.value = AuthUi(busy = true)
            try {
                firebase.sendPasswordReset(email)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Same wording as the website: never reveal whether the email exists.
            }
            _auth.value = AuthUi(info = "If an account exists for that email, a reset link is on its way. Check your inbox and your spam folder.")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try { api.logout() } catch (e: Exception) { /* offline is fine, we still sign out locally */ }
            firebase.signOut()
            cosup.tokenCache = null
            store.clear()
            _profile.value = null
            _messages.send("You have been signed out.")
        }
    }

    // -------------------------------------------------- profile
    fun loadProfile() {
        viewModelScope.launch {
            _profileError.value = null
            try {
                _profile.value = api.profile()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _profileError.value = e.toUserMessage()
            }
        }
    }

    fun saveProfile(update: ProfileUpdate) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val r = api.updateProfile(update)
                if (r.isSuccessful) {
                    _messages.send("Your details were saved.")
                    loadProfile()
                } else {
                    _messages.send(parseError(r.errorBody()?.string()) ?: "Could not save your details.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.toUserMessage())
            } finally {
                _busy.value = false
            }
        }
    }

    // -------------------------------------------------- referral + hopeline + theme
    fun submitReferral(req: ReferralRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val r = api.submitReferral(req)
                if (r.isSuccessful) {
                    _messages.send("Your referral has been submitted. A COSUP team member will contact you within 24 hours.")
                    onSuccess()
                } else {
                    _messages.send(parseError(r.errorBody()?.string()) ?: "Could not submit your referral. Please try again.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.toUserMessage())
            } finally {
                _busy.value = false
            }
        }
    }

    fun logHopeline(channel: String = "call") {
        viewModelScope.launch {
            try { api.logHopeline(HopelineRequest(channel)) } catch (e: Exception) { /* logging must never block a call */ }
        }
    }

    fun toggleTheme(isDarkNow: Boolean) {
        viewModelScope.launch { store.setTheme(if (isDarkNow) "light" else "dark") }
    }

    // -------------------------------------------------- admin
    /** section: dashboard | referrals | news | sites | audit | activity */
    fun loadAdmin(section: String) {
        viewModelScope.launch {
            _admin.update { it.copy(loading = true, error = null) }
            try {
                when (section) {
                    "dashboard" -> { val v = api.adminDashboard(); _admin.update { it.copy(dashboard = v) } }
                    "referrals" -> { val v = api.adminReferrals(); _admin.update { it.copy(referrals = v) } }
                    "news" -> { val v = api.adminNews(); _admin.update { it.copy(news = v) } }
                    "sites" -> { val v = api.adminSites(); _admin.update { it.copy(sites = v) } }
                    "audit" -> { val v = api.adminAudit(); _admin.update { it.copy(audit = v) } }
                    "activity" -> { val v = api.adminActivity(); _admin.update { it.copy(activity = v) } }
                }
                _admin.update { it.copy(loading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _admin.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }

    private fun adminAction(okMessage: String, reload: String, call: suspend () -> retrofit2.Response<Unit>) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val r = call()
                if (r.isSuccessful) {
                    _messages.send(okMessage)
                    loadAdmin(reload)
                    refresh() // public sites / news lists may have changed
                } else {
                    _messages.send(parseError(r.errorBody()?.string()) ?: "That did not work. Please try again.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.toUserMessage())
            } finally {
                _busy.value = false
            }
        }
    }

    fun adminUpdateReferral(id: Int, status: String) =
        adminAction("Referral #$id updated to $status.", "referrals") { api.adminUpdateReferral(id, StatusBody(status)) }

    fun adminCreateNews(title: String, body: String, category: String?) =
        adminAction("News item published.", "news") { api.adminCreateNews(NewsCreateBody(title, body, category)) }

    fun adminToggleNews(id: Int) =
        adminAction("News item updated.", "news") { api.adminToggleNews(id) }

    fun adminEditSite(id: Int, body: SiteEditBody) =
        adminAction("Site updated.", "sites") { api.adminEditSite(id, body) }
}
