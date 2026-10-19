package com.cosup.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosup.app.R
import com.cosup.app.ui.screens.AboutScreen
import com.cosup.app.ui.screens.AdminScreen
import com.cosup.app.ui.screens.AuthScreen
import com.cosup.app.ui.screens.ContactScreen
import com.cosup.app.ui.screens.FindSiteScreen
import com.cosup.app.ui.screens.HomeScreen
import com.cosup.app.ui.screens.NewsScreen
import com.cosup.app.ui.screens.ProfileScreen
import com.cosup.app.ui.screens.ServicesScreen
import com.cosup.app.ui.theme.CosupGreen
import com.cosup.app.ui.theme.CosupTheme
import com.cosup.app.util.HOPELINE_NUMBER
import com.cosup.app.util.dial

private object Routes {
    const val HOME = "home"
    const val SERVICES = "services"
    const val SITES = "sites"
    const val NEWS = "news"
    const val CONTACT = "contact"
    const val ABOUT = "about"
    const val AUTH = "auth"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Home", Icons.Filled.Home),
    Tab(Routes.SERVICES, "Services", Icons.Filled.Favorite),
    Tab(Routes.SITES, "Find a Site", Icons.Filled.Place),
    Tab(Routes.NEWS, "News", Icons.AutoMirrored.Filled.Article),
    Tab(Routes.CONTACT, "Contact", Icons.Filled.Email)
)

private fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun CosupRoot(vm: AppViewModel = viewModel()) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (theme) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
    CosupTheme(darkTheme = dark) { CosupScaffold(vm, dark) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CosupScaffold(vm: AppViewModel, dark: Boolean) {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val content by vm.content.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val isTab = TABS.any { it.route == route }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    fun callHopeline() {
        vm.logHopeline("call")
        ctx.dial(HOPELINE_NUMBER)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (!isTab && route != null) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                title = {
                    Image(painterResource(R.drawable.logo), contentDescription = "COSUP", modifier = Modifier.height(36.dp))
                },
                actions = {
                    IconButton(onClick = ::callHopeline) { Icon(Icons.Filled.Call, contentDescription = "Call the Hopeline") }
                    IconButton(onClick = { vm.toggleTheme(dark) }) {
                        Icon(if (dark) Icons.Filled.Brightness7 else Icons.Filled.Brightness4, contentDescription = "Toggle dark mode")
                    }
                    AccountMenu(
                        name = session?.name, role = session?.role,
                        onSignIn = { nav.navigate(Routes.AUTH) },
                        onProfile = { nav.navigate(Routes.PROFILE) },
                        onAbout = { nav.navigate(Routes.ABOUT) },
                        onAdmin = { nav.navigate(Routes.ADMIN) },
                        onSignOut = vm::signOut
                    )
                }
            )
        },
        bottomBar = {
            if (isTab) {
                NavigationBar {
                    TABS.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { nav.goTab(t.route) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label, fontSize = 10.sp, maxLines = 1) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding)) {
            composable(Routes.HOME) {
                HomeScreen(
                    content = content, onRetry = vm::refresh,
                    onReferral = { nav.goTab(Routes.CONTACT) },
                    onFindSite = { nav.goTab(Routes.SITES) },
                    onServices = { nav.goTab(Routes.SERVICES) },
                    onNews = { nav.goTab(Routes.NEWS) },
                    onAbout = { nav.navigate(Routes.ABOUT) },
                    onCall = ::callHopeline
                )
            }
            composable(Routes.SERVICES) { ServicesScreen(content, vm::refresh) { nav.goTab(Routes.SITES) } }
            composable(Routes.SITES) { FindSiteScreen(content, vm::refresh) { vm.logHopeline("call") } }
            composable(Routes.NEWS) { NewsScreen(content, vm::refresh) }
            composable(Routes.CONTACT) {
                ContactScreen(
                    content = content, session = session, busy = busy,
                    onSubmit = { req, done -> vm.submitReferral(req, done) },
                    onFindSite = { nav.goTab(Routes.SITES) },
                    onCall = ::callHopeline
                )
            }
            composable(Routes.ABOUT) { AboutScreen() }
            composable(Routes.ADMIN) { AdminScreen(vm) }
            composable(Routes.AUTH) { AuthScreen(vm) { nav.popBackStack() } }
            composable(Routes.PROFILE) {
                ProfileScreen(vm, onSignIn = { nav.navigate(Routes.AUTH) }, onAdmin = { nav.navigate(Routes.ADMIN) }, onSignOut = vm::signOut)
            }
        }
    }
}

@Composable
private fun AccountMenu(
    name: String?,
    role: String?,
    onSignIn: () -> Unit,
    onProfile: () -> Unit,
    onAbout: () -> Unit,
    onAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val isGuest = role == "guest"
    Box {
        IconButton(onClick = { open = true }) {
            if (name.isNullOrBlank()) {
                Icon(Icons.Filled.Person, contentDescription = "Account")
            } else {
                val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
                Box(Modifier.size(32.dp).background(CosupGreen, CircleShape), contentAlignment = Alignment.Center) {
                    Text(initials.ifBlank { "U" }, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (name.isNullOrBlank()) {
                DropdownMenuItem(text = { Text("Sign in / Register") }, onClick = { open = false; onSignIn() })
            } else {
                DropdownMenuItem(
                    text = { Text(if (isGuest) "$name (Guest)" else name, fontWeight = FontWeight.Bold) },
                    onClick = { open = false }, enabled = false
                )
                if (isGuest) DropdownMenuItem(text = { Text("Create a free account") }, onClick = { open = false; onSignIn() })
                else DropdownMenuItem(text = { Text("My profile") }, onClick = { open = false; onProfile() })
            }
            if (role == "admin") DropdownMenuItem(text = { Text("Admin panel") }, onClick = { open = false; onAdmin() })
            DropdownMenuItem(text = { Text("About COSUP") }, onClick = { open = false; onAbout() })
            if (!name.isNullOrBlank()) {
                DropdownMenuItem(
                    text = { Text(if (isGuest) "End guest session" else "Sign out") },
                    onClick = { open = false; onSignOut() }
                )
            }
        }
    }
}
