package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Offers : Screen("offers", "Offers")
    object Tracking : Screen("tracking", "Track")
    object Wallet : Screen("wallet", "Wallet")
    object Profile : Screen("profile", "Profile")

    object OfferDetail : Screen("offer_detail/{offerId}", "Offer Details") {
        fun createRoute(offerId: String) = "offer_detail/$offerId"
    }

    object Withdraw : Screen("withdraw", "Withdraw")
    object Referral : Screen("referral", "Referral & Earn")
    object Notifications : Screen("notifications", "Notifications")
    object AdminConsole : Screen("admin_console", "Operator Console")
}
