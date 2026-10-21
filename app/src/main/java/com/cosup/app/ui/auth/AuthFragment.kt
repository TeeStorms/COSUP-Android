package com.cosup.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cosup.app.databinding.DialogForgotBinding
import com.cosup.app.databinding.DialogGuestBinding
import com.cosup.app.databinding.FragmentAuthBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.AuthUi
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.str
import com.cosup.app.ui.common.visible
import com.cosup.app.util.isValidAlias
import com.cosup.app.util.isValidEmail
import com.cosup.app.util.isValidOptionalPhone
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class AuthFragment : Fragment() {
    private var _b: FragmentAuthBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    private var localError: String? = null
    private var forgotBinding: DialogForgotBinding? = null
    private var guestBinding: DialogGuestBinding? = null
    private var openDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAuthBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.clearAuthMessage()

        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                b.signinForm.visible(tab.position == 0)
                b.registerForm.visible(tab.position == 1)
                localError = null
                renderError(vm.auth.value)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val activity = requireActivity()
        b.btnGoogle.setOnClickListener { vm.signInWithProvider(activity, "google.com") }
        b.btnYahoo.setOnClickListener { vm.signInWithProvider(activity, "yahoo.com") }
        b.btnMicrosoft.setOnClickListener { vm.signInWithProvider(activity, "microsoft.com") }

        b.btnSignIn.setOnClickListener {
            val email = b.etSiEmail.str.trim()
            val pw = b.etSiPassword.str
            if (email.isEmpty() || pw.isEmpty()) setLocalError("Enter your email and password.")
            else { setLocalError(null); vm.signInWithEmail(email, pw) }
        }
        b.btnForgot.setOnClickListener { showForgot() }
        b.btnRegister.setOnClickListener { register() }
        b.btnGuest.setOnClickListener { showGuest() }

        collectFlow(vm.auth) { render(it) }
        collectFlow(vm.authDone) { openDialog?.dismiss(); findNavController().popBackStack() }
    }

    private fun register() {
        val first = b.etRgFirst.str.trim()
        val last = b.etRgLast.str.trim()
        val phone = b.etRgPhone.str.trim()
        val email = b.etRgEmail.str.trim()
        val pw = b.etRgPassword.str
        val error = when {
            first.isEmpty() || last.isEmpty() -> "Enter your first and last name."
            !isValidOptionalPhone(phone) -> "Enter a valid phone number (digits only)."
            !isValidEmail(email) -> "Enter a valid email address."
            pw.length < 6 -> "Password must be at least 6 characters."
            !b.cbRgConsent.isChecked -> "Please accept the POPIA consent to continue."
            else -> null
        }
        setLocalError(error)
        if (error == null) vm.register(first, last, phone, email, pw)
    }

    private fun setLocalError(msg: String?) {
        localError = msg
        renderError(vm.auth.value)
    }

    private fun renderError(auth: AuthUi) {
        val msg = localError ?: auth.error
        _b?.authError?.let { it.text = msg; it.visible(!msg.isNullOrBlank()) }
    }

    private fun render(auth: AuthUi) {
        val enabled = !auth.busy
        listOf(b.btnGoogle, b.btnYahoo, b.btnMicrosoft, b.btnSignIn, b.btnRegister, b.btnGuest, b.btnForgot)
            .forEach { it.isEnabled = enabled }
        b.btnSignIn.text = if (auth.busy) "Please wait..." else "Sign in"
        b.btnRegister.text = if (auth.busy) "Please wait..." else "Create account"
        renderError(auth)

        forgotBinding?.let { d ->
            d.fpMsg.text = auth.info
            d.fpMsg.visible(!auth.info.isNullOrBlank())
        }
        guestBinding?.let { d ->
            d.gsError.text = auth.error
            d.gsError.visible(!auth.error.isNullOrBlank())
        }
    }

    // ------------------------------------------------------------ dialogs
    private fun showForgot() {
        vm.clearAuthMessage()
        val d = DialogForgotBinding.inflate(layoutInflater)
        d.etFpEmail.setText(b.etSiEmail.str.trim())
        forgotBinding = d
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset your password")
            .setView(d.root)
            .setPositiveButton("Send reset link", null)
            .setNegativeButton("Back to sign in", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val email = d.etFpEmail.str.trim()
                if (!isValidEmail(email)) d.tilFpEmail.error = "Enter a valid email address."
                else { d.tilFpEmail.error = null; vm.sendPasswordReset(email) }
            }
        }
        dialog.setOnDismissListener { forgotBinding = null; openDialog = null; vm.clearAuthMessage() }
        openDialog = dialog
        dialog.show()
    }

    private fun showGuest() {
        vm.clearAuthMessage()
        val d = DialogGuestBinding.inflate(layoutInflater)
        guestBinding = d
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Continue as guest")
            .setView(d.root)
            .setPositiveButton("Continue as guest", null)
            .setNegativeButton("Back to sign in", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val alias = d.etGsName.str.trim().replace(Regex("\\s+"), " ")
                when {
                    !isValidAlias(alias) -> d.tilGsName.error = "Choose a name of 2 to 30 characters (letters, numbers, spaces, - _ .)."
                    !d.cbGsConsent.isChecked -> {
                        d.tilGsName.error = null
                        d.gsError.text = "Please tick the box so we can save your activity under this name."
                        d.gsError.visible(true)
                    }
                    else -> { d.tilGsName.error = null; d.gsError.visible(false); vm.signInAsGuest(alias) }
                }
            }
        }
        dialog.setOnDismissListener { guestBinding = null; openDialog = null; vm.clearAuthMessage() }
        openDialog = dialog
        dialog.show()
    }

    override fun onDestroyView() {
        openDialog?.dismiss()
        super.onDestroyView()
        _b = null
        forgotBinding = null
        guestBinding = null
    }
}
