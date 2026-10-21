package com.cosup.app.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cosup.app.MainActivity
import com.cosup.app.R
import com.cosup.app.data.ReferralRequest
import com.cosup.app.databinding.FragmentContactBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.bindOptions
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.str
import com.cosup.app.ui.common.visible
import com.cosup.app.util.isValidEmail
import com.cosup.app.util.isValidPhone

class ContactFragment : Fragment() {
    private var _b: FragmentContactBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    private var area = ""
    private var service = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentContactBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        b.cardClinic.setOnClickListener { main.goTab(R.id.sitesFragment) }
        b.cardHope.setOnClickListener { main.callHopeline() }

        // Prefill the name the way the website does (signed-in or guest name).
        vm.session.value?.name?.takeIf { it.isNotBlank() && b.etFullName.str.isEmpty() }?.let { b.etFullName.setText(it) }

        collectFlow(vm.content) { c ->
            b.ddArea.bindOptions(listOf("" to "No preference") + c.sites.map { it.siteName to it.siteName }, area) { area = it }
            b.ddService.bindOptions(listOf("" to "Not sure") + c.services.map { it.serviceName to it.serviceName }, service) { service = it }
        }
        collectFlow(vm.busy) { busy ->
            b.btnSubmit.isEnabled = !busy
            b.btnSubmit.text = if (busy) "Submitting..." else "Submit referral"
        }
        b.btnSubmit.setOnClickListener { submit() }
    }

    private fun submit() {
        val name = b.etFullName.str.trim()
        val phone = b.etPhone.str.trim()
        val email = b.etEmail.str.trim()
        val message = b.etMessage.str.trim()
        val consent = b.cbConsent.isChecked

        b.tilFullName.error = if (name.isEmpty() || name.length > 160) "Please enter your full name." else null
        b.tilPhone.error = if (!isValidPhone(phone)) "Enter a valid phone number (digits only)." else null
        b.tilEmail.error = if (email.isNotEmpty() && !isValidEmail(email)) "Enter a valid email address." else null
        b.tilMessage.error = if (message.length > 1000) "Keep your message under 1000 characters." else null
        b.consentError.visible(!consent)

        val ok = b.tilFullName.error == null && b.tilPhone.error == null && b.tilEmail.error == null &&
            b.tilMessage.error == null && consent
        if (!ok) return

        vm.submitReferral(
            ReferralRequest(
                fullName = name, contactNumber = phone, email = email.ifEmpty { null },
                preferredArea = area.ifEmpty { null }, serviceNeeded = service.ifEmpty { null },
                message = message.ifEmpty { null }, consent = true
            )
        ) {
            _b?.let {
                it.etPhone.text = null; it.etEmail.text = null; it.etMessage.text = null
                it.cbConsent.isChecked = false
                area = ""; service = ""
                it.ddArea.setText("No preference", false)
                it.ddService.setText("Not sure", false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
