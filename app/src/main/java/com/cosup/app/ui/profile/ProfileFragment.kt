package com.cosup.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cosup.app.MainActivity
import com.cosup.app.R
import com.cosup.app.data.ProfileDto
import com.cosup.app.data.ProfileUpdate
import com.cosup.app.databinding.FragmentProfileBinding
import com.cosup.app.databinding.ItemReferralRowBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.bindOptions
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.str
import com.cosup.app.ui.common.visible
import com.cosup.app.util.LANGUAGES
import com.cosup.app.util.formatDate
import com.cosup.app.util.isValidOptionalPhone

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    private var lang = "en"
    private var filledFor: ProfileDto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        b.btnSignInOut.setOnClickListener { main.navigateTo(R.id.authFragment) }
        b.btnCreateAccount.setOnClickListener { main.navigateTo(R.id.authFragment) }
        b.btnEndGuest.setOnClickListener { vm.signOut() }
        b.btnSignOut.setOnClickListener { vm.signOut() }
        b.btnAdmin.setOnClickListener { main.navigateTo(R.id.adminFragment) }
        b.btnRetry.setOnClickListener { vm.loadProfile() }
        b.btnSave.setOnClickListener { save() }

        collectFlow(vm.session) { s ->
            if (s != null && s.role != "guest") vm.loadProfile()
            render()
        }
        collectFlow(vm.profile) { render() }
        collectFlow(vm.profileError) { render() }
        collectFlow(vm.busy) { busy -> b.btnSave.isEnabled = !busy; b.btnSave.text = if (busy) "Saving..." else "Save changes" }
    }

    private fun render() {
        val s = vm.session.value
        val guest = s?.role == "guest"
        val p = vm.profile.value
        val err = vm.profileError.value
        val registered = s != null && !guest

        b.boxSignedOut.visible(s == null)
        b.boxGuest.visible(guest)
        b.guestTitle.text = "Browsing as ${s?.name.orEmpty()}"
        b.loading.visible(registered && p == null && err == null)
        b.errorBox.visible(registered && p == null && err != null)
        b.errorText.text = err
        b.boxUser.visible(registered && p != null)
        b.btnAdmin.visible(s?.role == "admin")

        if (registered && p != null && filledFor !== p) {
            filledFor = p
            fill(p)
        }
    }

    private fun fill(p: ProfileDto) {
        b.profEmail.text = p.email
        b.etFirst.setText(p.firstName)
        b.etLast.setText(p.lastName)
        b.etPhone.setText(p.phone.orEmpty())
        lang = p.langPref
        b.ddLang.bindOptions(LANGUAGES, lang) { lang = it }

        b.refContainer.removeAllViews()
        b.refEmpty.visible(p.referrals.isEmpty())
        p.referrals.forEach { r ->
            val row = ItemReferralRowBinding.inflate(layoutInflater, b.refContainer, false)
            row.refService.text = r.serviceNeeded ?: "General referral"
            row.refStatus.text = r.status.replaceFirstChar { it.uppercase() }
            row.refMeta.text = listOfNotNull(formatDate(r.submittedAt).ifBlank { null }, r.preferredArea).joinToString(" \u00B7 ")
            b.refContainer.addView(row.root)
        }
    }

    private fun save() {
        val first = b.etFirst.str.trim()
        val last = b.etLast.str.trim()
        val phone = b.etPhone.str.trim()
        b.tilFirst.error = if (first.isEmpty()) "First name is required." else null
        b.tilLast.error = if (last.isEmpty()) "Last name is required." else null
        b.tilPhone.error = if (!isValidOptionalPhone(phone)) "Enter a valid phone number (digits only)." else null
        if (b.tilFirst.error == null && b.tilLast.error == null && b.tilPhone.error == null) {
            vm.saveProfile(ProfileUpdate(first, last, phone.ifEmpty { null }, lang))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
        filledFor = null
    }
}
