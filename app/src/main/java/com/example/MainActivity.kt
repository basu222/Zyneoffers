package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MaintenanceScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OfferDetailScreen
import com.example.ui.screens.OffersWallScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneOffersTheme
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneRedBg
import com.example.ui.theme.ZyneRedBorder
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZyneOffersTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeOffers by viewModel.activeOffers.collectAsState()
    val allOffers by viewModel.allOffers.collectAsState()
    val activeBanners by viewModel.activeBanners.collectAsState()
    val allBanners by viewModel.allBanners.collectAsState()
    val activeAnnouncement by viewModel.activeAnnouncement.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val userTransactions by viewModel.userTransactions.collectAsState()
    val userWithdrawals by viewModel.userWithdrawals.collectAsState()
    val userNotifications by viewModel.userNotifications.collectAsState()
    val userReferrals by viewModel.userReferrals.collectAsState()

    // Admin states
    val allUsers by viewModel.allUsers.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Offers.route,
        Screen.Tracking.route,
        Screen.Wallet.route,
        Screen.Profile.route
    )

    // Maintenance guard
    val isCurrentUserAdmin = currentUser?.email?.trim().equals(AppConstants.ADMIN_EMAIL, ignoreCase = true) || currentUser?.isAdmin == true

    if (appSettings.isMaintenanceMode && !isCurrentUserAdmin && currentRoute != Screen.Login.route) {
        MaintenanceScreen(
            settings = appSettings,
            onAdminBypass = { navController.navigate(Screen.AdminConsole.route) }
        )
        return
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null && currentRoute == Screen.Login.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ZyneBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = ZyneBackground,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ZyneBackground)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (currentUserId != null) Screen.Home.route else Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onLoginWithParams = { email, name, photo, deviceId, refCode, idToken ->
                            viewModel.loginWithGoogle(email, name, photo, deviceId, refCode, idToken)
                        },
                        errorMessage = viewModel.loginError.collectAsState().value,
                        isLoading = viewModel.isLoginLoading.collectAsState().value,
                        onClearError = { viewModel.clearLoginError() }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        user = currentUser,
                        announcement = activeAnnouncement,
                        banners = activeBanners,
                        offers = activeOffers,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.setSelectedCategory(it) },
                        unreadCount = unreadCount,
                        onNavigateToOfferDetails = { offerId ->
                            navController.navigate(Screen.OfferDetail.createRoute(offerId))
                        },
                        onNavigateToWallet = {
                            navController.navigate(Screen.Wallet.route)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onNavigateToTracking = {
                            navController.navigate(Screen.Tracking.route)
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                composable(Screen.Offers.route) {
                    OffersWallScreen(
                        user = currentUser,
                        offers = activeOffers,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.setSelectedCategory(it) },
                        onNavigateToOfferDetails = { offerId ->
                            navController.navigate(Screen.OfferDetail.createRoute(offerId))
                        }
                    )
                }

                composable(Screen.Tracking.route) {
                    TrackingScreen(
                        trackingUrl = appSettings.trackingUrl,
                        userId = currentUserId
                    )
                }

                composable(Screen.Wallet.route) {
                    WalletScreen(
                        user = currentUser,
                        transactions = userTransactions,
                        minWithdrawalAmount = appSettings.minWithdrawalAmount,
                        onNavigateToWithdraw = {
                            navController.navigate(Screen.Withdraw.route)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        user = currentUser,
                        settings = appSettings,
                        onNavigateToReferral = { navController.navigate(Screen.Referral.route) },
                        onNavigateToAdminConsole = { navController.navigate(Screen.AdminConsole.route) },
                        onAuthenticateAdmin = { pin -> viewModel.authenticateAdmin(pin) },
                        isAdminAuthenticated = isAdminAuthenticated,
                        onLogout = {
                            viewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.OfferDetail.route,
                    arguments = listOf(navArgument("offerId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
                    val offer = allOffers.firstOrNull { it.id == offerId }
                    OfferDetailScreen(
                        offer = offer,
                        user = currentUser,
                        onNavigateBack = { navController.popBackStack() },
                        onStartOffer = { offerToStart, affiliateUrl ->
                            viewModel.recordOfferClick(offerToStart, affiliateUrl)
                        }
                    )
                }

                composable(Screen.Withdraw.route) {
                    WithdrawScreen(
                        user = currentUser,
                        minWithdrawalAmount = appSettings.minWithdrawalAmount,
                        withdrawals = userWithdrawals,
                        onRequestWithdrawal = { amount, upiId -> viewModel.requestWithdrawal(amount, upiId) },
                        withdrawMessage = viewModel.withdrawMessage.collectAsState().value,
                        onClearMessage = { viewModel.clearWithdrawMessage() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Referral.route) {
                    ReferralScreen(
                        user = currentUser,
                        referrals = userReferrals,
                        referralBonusAmount = appSettings.referralBonusAmount,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        notifications = userNotifications,
                        onMarkAllRead = { viewModel.markNotificationsRead() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminConsole.route) {
                    val isAuthorizedAdmin = currentUser?.email?.trim().equals(AppConstants.ADMIN_EMAIL, ignoreCase = true)
                    if (!isAuthorizedAdmin) {
                        LaunchedEffect(Unit) {
                            Toast.makeText(context, "Access Denied: Administrator account required", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ZyneBackground)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, ZyneRedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(ZyneRedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Access Denied",
                                            tint = ZyneRed,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Admin Access Restricted",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ZyneTextPrimary,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Only the authorized administrator (${AppConstants.ADMIN_EMAIL}) can access this operator console.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ZyneTextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { navController.popBackStack() },
                                        colors = ButtonDefaults.buttonColors(containerColor = ZyneBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Return to App", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        AdminConsoleScreen(
                            users = allUsers,
                            offers = allOffers,
                            withdrawals = allWithdrawals,
                            banners = allBanners,
                            announcement = activeAnnouncement,
                            settings = appSettings,
                            auditLogs = auditLogs,
                            onCreditReward = { userId, amount, offerName, note ->
                                viewModel.adminCreditReward(userId, amount, offerName, note)
                            },
                            onRejectReward = { userId, offerName, reason ->
                                viewModel.adminRejectReward(userId, offerName, reason)
                            },
                            onApproveWithdrawal = { wdId -> viewModel.adminApproveWithdrawal(wdId) },
                            onMarkWithdrawalPaid = { wdId -> viewModel.adminMarkWithdrawalPaid(wdId) },
                            onRejectWithdrawal = { wdId, reason -> viewModel.adminRejectWithdrawal(wdId, reason) },
                            onSaveOffer = { offer -> viewModel.adminSaveOffer(offer) },
                            onDeleteOffer = { offerId -> viewModel.adminDeleteOffer(offerId) },
                            onSaveBanner = { banner -> viewModel.adminSaveBanner(banner) },
                            onDeleteBanner = { bannerId -> viewModel.adminDeleteBanner(bannerId) },
                            onSaveAnnouncement = { ann -> viewModel.adminSaveAnnouncement(ann) },
                            onUpdateSettings = { set -> viewModel.adminUpdateSettings(set) },
                            onToggleUserBan = { userId -> viewModel.adminToggleUserBan(userId) },
                            onResetDeviceBinding = { userId -> viewModel.adminResetDeviceBinding(userId) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean Professional Fintech Bottom Navigation Bar:
 * Home | Offers | Tracking | Wallet | Profile
 * Crisp white background with 1px zinc-200 border, Royal Blue active state, muted slate inactive state.
 * Preserves WindowInsets.navigationBars padding so it is fully visible above system gestures.
 */
@Composable
fun ZyneBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ZyneCard,
        border = BorderStroke(1.dp, ZyneBorder),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZyneNavItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) },
                    modifier = Modifier.weight(1f)
                )
                ZyneNavItem(
                    title = "Offers",
                    icon = Icons.Default.LocalOffer,
                    isSelected = currentRoute == Screen.Offers.route,
                    onClick = { onNavigate(Screen.Offers.route) },
                    modifier = Modifier.weight(1f)
                )
                ZyneNavItem(
                    title = "Tracking",
                    icon = Icons.Default.TrackChanges,
                    isSelected = currentRoute == Screen.Tracking.route,
                    onClick = { onNavigate(Screen.Tracking.route) },
                    modifier = Modifier.weight(1f)
                )
                ZyneNavItem(
                    title = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = currentRoute == Screen.Wallet.route,
                    onClick = { onNavigate(Screen.Wallet.route) },
                    modifier = Modifier.weight(1f)
                )
                ZyneNavItem(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    isSelected = currentRoute == Screen.Profile.route,
                    onClick = { onNavigate(Screen.Profile.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ZyneNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = ZyneBlue
    val inactiveColor = ZyneTextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeColor else inactiveColor,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
