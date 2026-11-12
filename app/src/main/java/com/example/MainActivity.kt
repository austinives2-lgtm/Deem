package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.PanelMode
import com.example.ui.components.AppHeader
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.activities.ActivitiesScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.blood.BloodScreen
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.members.MembersScreen
import com.example.ui.screens.messages.ChatConversationScreen
import com.example.ui.screens.messages.ConversationsListScreen
import com.example.ui.screens.notices.NoticesScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.services.ServicesScreen
import com.example.ui.theme.SevaGreenPrimary
import com.example.ui.theme.SevaOrgTheme
import kotlinx.coroutines.flow.collectLatest

sealed class NavScreen {
    object Splash : NavScreen()
    object Login : NavScreen()
    object Register : NavScreen()
    object Home : NavScreen()
    object Services : NavScreen()
    object Activities : NavScreen()
    object Notices : NavScreen()
    object Profile : NavScreen()
    object Blood : NavScreen()
    object Finance : NavScreen()
    object Members : NavScreen()
    object Conversations : NavScreen()
    data class Chat(val conversationId: String, val recipientId: String, val recipientName: String) : NavScreen()
    object Notifications : NavScreen()
    object AdminDashboard : NavScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SevaOrgTheme {
                val viewModel: MainViewModel = viewModel()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                var currentScreen by remember { mutableStateOf<NavScreen>(NavScreen.Splash) }
                val currentUser by viewModel.currentUser.collectAsState()
                val panelMode by viewModel.panelMode.collectAsState()
                val notifications by viewModel.notifications.collectAsState()
                val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }

                // Listen to toasts
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // If admin panel mode is toggled, ensure we navigate appropriately
                LaunchedEffect(panelMode) {
                    if (panelMode == PanelMode.ADMIN && currentScreen !is NavScreen.AdminDashboard) {
                        currentScreen = NavScreen.AdminDashboard
                    } else if (panelMode == PanelMode.USER && currentScreen is NavScreen.AdminDashboard) {
                        currentScreen = NavScreen.Home
                    }
                }

                val showBottomBarAndHeader = currentScreen !in listOf(
                    NavScreen.Splash,
                    NavScreen.Login,
                    NavScreen.Register,
                    NavScreen.AdminDashboard
                ) && currentScreen !is NavScreen.Chat

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (showBottomBarAndHeader) {
                            AppHeader(
                                currentUser = currentUser,
                                panelMode = panelMode,
                                unreadNotificationsCount = unreadNotifications,
                                onPanelToggle = {
                                    val nextMode = if (panelMode == PanelMode.USER) PanelMode.ADMIN else PanelMode.USER
                                    viewModel.switchPanel(nextMode)
                                },
                                onNotificationsClick = { currentScreen = NavScreen.Notifications },
                                onMessagesClick = { currentScreen = NavScreen.Conversations },
                                onProfileClick = { currentScreen = NavScreen.Profile }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBarAndHeader) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is NavScreen.Home,
                                    onClick = { currentScreen = NavScreen.Home },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is NavScreen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "হোম"
                                        )
                                    },
                                    label = { Text("হোম", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SevaGreenPrimary,
                                        selectedTextColor = SevaGreenPrimary,
                                        indicatorColor = SevaGreenPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is NavScreen.Services,
                                    onClick = { currentScreen = NavScreen.Services },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is NavScreen.Services) Icons.Filled.VolunteerActivism else Icons.Outlined.VolunteerActivism,
                                            contentDescription = "সেবাসমূহ"
                                        )
                                    },
                                    label = { Text("সেবাসমূহ", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SevaGreenPrimary,
                                        selectedTextColor = SevaGreenPrimary,
                                        indicatorColor = SevaGreenPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_services")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is NavScreen.Activities,
                                    onClick = { currentScreen = NavScreen.Activities },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is NavScreen.Activities) Icons.Filled.Campaign else Icons.Outlined.Campaign,
                                            contentDescription = "কার্যক্রম"
                                        )
                                    },
                                    label = { Text("কার্যক্রম", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SevaGreenPrimary,
                                        selectedTextColor = SevaGreenPrimary,
                                        indicatorColor = SevaGreenPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_activities")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is NavScreen.Notices,
                                    onClick = { currentScreen = NavScreen.Notices },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is NavScreen.Notices) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                            contentDescription = "নোটিশ"
                                        )
                                    },
                                    label = { Text("নোটিশ", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SevaGreenPrimary,
                                        selectedTextColor = SevaGreenPrimary,
                                        indicatorColor = SevaGreenPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_notices")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is NavScreen.Profile,
                                    onClick = { currentScreen = NavScreen.Profile },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is NavScreen.Profile) Icons.Filled.Person else Icons.Outlined.Person,
                                            contentDescription = "প্রোফাইল"
                                        )
                                    },
                                    label = { Text("প্রোফাইল", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SevaGreenPrimary,
                                        selectedTextColor = SevaGreenPrimary,
                                        indicatorColor = SevaGreenPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_profile")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val screen = currentScreen) {
                            NavScreen.Splash -> {
                                SplashScreen(
                                    viewModel = viewModel,
                                    onNavigateNext = { isLoggedIn ->
                                        currentScreen = if (isLoggedIn) NavScreen.Home else NavScreen.Login
                                    }
                                )
                            }

                            NavScreen.Login -> {
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = { currentScreen = NavScreen.Home },
                                    onNavigateToRegister = { currentScreen = NavScreen.Register }
                                )
                            }

                            NavScreen.Register -> {
                                BackHandler { currentScreen = NavScreen.Login }
                                RegisterScreen(
                                    viewModel = viewModel,
                                    onRegisterSuccess = { currentScreen = NavScreen.Home },
                                    onNavigateToLogin = { currentScreen = NavScreen.Login }
                                )
                            }

                            NavScreen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToServices = { currentScreen = NavScreen.Services },
                                    onNavigateToActivities = { currentScreen = NavScreen.Activities },
                                    onNavigateToBlood = { currentScreen = NavScreen.Blood },
                                    onNavigateToFinance = { currentScreen = NavScreen.Finance },
                                    onNavigateToNotices = { currentScreen = NavScreen.Notices },
                                    onServiceSelected = { currentScreen = NavScreen.Services },
                                    onActivitySelected = { currentScreen = NavScreen.Activities },
                                    onRequestBloodClick = { currentScreen = NavScreen.Blood }
                                )
                            }

                            NavScreen.Services -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                ServicesScreen(viewModel = viewModel)
                            }

                            NavScreen.Activities -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                ActivitiesScreen(viewModel = viewModel)
                            }

                            NavScreen.Blood -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                BloodScreen(
                                    viewModel = viewModel,
                                    onNavigateToChatWithUser = { uid, name ->
                                        val convId = viewModel.firestoreRepo.getConversationId(currentUser?.uid ?: "", uid)
                                        currentScreen = NavScreen.Chat(convId, uid, name)
                                    }
                                )
                            }

                            NavScreen.Finance -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                FinanceScreen(viewModel = viewModel)
                            }

                            NavScreen.Notices -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                NoticesScreen(viewModel = viewModel)
                            }

                            NavScreen.Members -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                MembersScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { uid, name ->
                                        val convId = viewModel.firestoreRepo.getConversationId(currentUser?.uid ?: "", uid)
                                        currentScreen = NavScreen.Chat(convId, uid, name)
                                    }
                                )
                            }

                            NavScreen.Profile -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateToAdminDashboard = {
                                        viewModel.switchPanel(PanelMode.ADMIN)
                                        currentScreen = NavScreen.AdminDashboard
                                    },
                                    onLoggedOut = {
                                        currentScreen = NavScreen.Login
                                    }
                                )
                            }

                            NavScreen.Conversations -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                ConversationsListScreen(
                                    viewModel = viewModel,
                                    onSelectConversation = { convId, recId, recName ->
                                        currentScreen = NavScreen.Chat(convId, recId, recName)
                                    }
                                )
                            }

                            is NavScreen.Chat -> {
                                BackHandler { currentScreen = NavScreen.Conversations }
                                ChatConversationScreen(
                                    conversationId = screen.conversationId,
                                    recipientId = screen.recipientId,
                                    recipientName = screen.recipientName,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = NavScreen.Conversations }
                                )
                            }

                            NavScreen.Notifications -> {
                                BackHandler { currentScreen = NavScreen.Home }
                                NotificationsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = NavScreen.Home },
                                    onNavigateToBlood = { currentScreen = NavScreen.Blood },
                                    onNavigateToNotices = { currentScreen = NavScreen.Notices },
                                    onNavigateToMessages = { currentScreen = NavScreen.Conversations }
                                )
                            }

                            NavScreen.AdminDashboard -> {
                                BackHandler {
                                    viewModel.switchPanel(PanelMode.USER)
                                    currentScreen = NavScreen.Home
                                }
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onBackToUserPanel = {
                                        viewModel.switchPanel(PanelMode.USER)
                                        currentScreen = NavScreen.Home
                                    },
                                    onNavigateToServices = { currentScreen = NavScreen.Services },
                                    onNavigateToActivities = { currentScreen = NavScreen.Activities },
                                    onNavigateToBlood = { currentScreen = NavScreen.Blood },
                                    onNavigateToFinance = { currentScreen = NavScreen.Finance },
                                    onNavigateToNotices = { currentScreen = NavScreen.Notices },
                                    onNavigateToMembers = { currentScreen = NavScreen.Members }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
