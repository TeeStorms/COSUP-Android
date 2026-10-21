package com.cosup.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cosup.app.databinding.ActivityMainBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.visible
import com.cosup.app.util.HOPELINE_NUMBER
import com.cosup.app.util.dial
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    val vm: AppViewModel by viewModels()

    private val topLevel = setOf(
        R.id.homeFragment, R.id.servicesFragment, R.id.sitesFragment, R.id.newsFragment, R.id.contactFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = host.navController
        appBarConfig = AppBarConfiguration(topLevel)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visible(destination.id in topLevel)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.theme.collect { theme ->
                        val mode = CosupApp.nightModeFor(theme)
                        if (AppCompatDelegate.getDefaultNightMode() != mode) AppCompatDelegate.setDefaultNightMode(mode)
                    }
                }
                launch { vm.messages.collect { showMessage(it) } }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    // ---------------------------------------------------------------- helpers for fragments
    /** Switches to one of the five bottom-bar tabs (R.id.homeFragment, R.id.sitesFragment, ...). */
    fun goTab(destinationId: Int) {
        binding.bottomNav.selectedItemId = destinationId
    }

    fun navigateTo(destinationId: Int) {
        navController.navigate(destinationId)
    }

    fun callHopeline() {
        vm.logHopeline("call")
        dial(HOPELINE_NUMBER)
    }

    private fun showMessage(text: String) {
        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        if (binding.bottomNav.visibility == View.VISIBLE) bar.anchorView = binding.bottomNav
        bar.show()
    }

    // ---------------------------------------------------------------- top bar menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_call -> { callHopeline(); true }
        R.id.action_theme -> {
            val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            vm.toggleTheme(dark)
            true
        }
        R.id.action_account -> { showAccountMenu(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showAccountMenu() {
        val anchor = findViewById<View>(R.id.action_account) ?: binding.toolbar
        val popup = PopupMenu(this, anchor)
        val session = vm.session.value
        val isGuest = session?.role == "guest"

        if (session == null) {
            popup.menu.add(0, MENU_SIGN_IN, 0, "Sign in / Register")
        } else {
            popup.menu.add(0, MENU_NAME, 0, if (isGuest) "${session.name} (Guest)" else session.name).isEnabled = false
            if (isGuest) popup.menu.add(0, MENU_SIGN_IN, 1, "Create a free account")
            else popup.menu.add(0, MENU_PROFILE, 1, "My profile")
            if (session.role == "admin") popup.menu.add(0, MENU_ADMIN, 2, "Admin panel")
        }
        popup.menu.add(0, MENU_ABOUT, 3, "About COSUP")
        if (session != null) popup.menu.add(0, MENU_SIGN_OUT, 4, if (isGuest) "End guest session" else "Sign out")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                MENU_SIGN_IN -> navigateTo(R.id.authFragment)
                MENU_PROFILE -> navigateTo(R.id.profileFragment)
                MENU_ADMIN -> navigateTo(R.id.adminFragment)
                MENU_ABOUT -> navigateTo(R.id.aboutFragment)
                MENU_SIGN_OUT -> vm.signOut()
            }
            true
        }
        popup.show()
    }

    private companion object {
        const val MENU_NAME = 1
        const val MENU_SIGN_IN = 2
        const val MENU_PROFILE = 3
        const val MENU_ADMIN = 4
        const val MENU_ABOUT = 5
        const val MENU_SIGN_OUT = 6
    }
}
