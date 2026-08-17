package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AppConstants
import com.example.data.local.SettingsEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.ZyneAvatarGradient
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBlueBorder
import com.example.ui.theme.ZyneBlueLight
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneGreen
import com.example.ui.theme.ZyneGreenBg
import com.example.ui.theme.ZyneGreenBorder
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneRedBg
import com.example.ui.theme.ZyneRedBorder
import com.example.ui.theme.ZyneSurface
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    user: UserEntity?,
    settings: SettingsEntity,
    onNavigateToReferral: () -> Unit,
    onNavigateToAdminConsole: () -> Unit,
    onAuthenticateAdmin: (pin: String) -> Boolean,
    isAdminAuthenticated: Boolean,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    var showAdminPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Strict Admin verification
    val isCurrentUserAdmin = user?.email?.trim().equals(AppConstants.ADMIN_EMAIL, ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZyneBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER: Profile & Settings
            // ==========================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            // ==========================================
            // 2. USER PROFILE HERO CARD
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZyneCard),
                    border = BorderStroke(1.dp, ZyneBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ZyneAvatarGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user?.photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = user?.photoUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = user?.displayName?.firstOrNull()?.uppercase() ?: "Z",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user?.displayName?.ifBlank { "Zyne Member" } ?: "Zyne Member",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneTextPrimary,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = user?.email?.ifBlank { "user@zyneoffers.com" } ?: "user@zyneoffers.com",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ZyneTextMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Surface(
                                color = ZyneGreenBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ZyneGreenBorder)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneGreen,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User ID & Referral Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZyneBlueLight)
                                .border(1.dp, ZyneBlueBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ZYNE USER ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ZyneTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = user?.userId ?: "ZY_GUEST",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneBlue,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(user?.userId ?: ""))
                                        Toast.makeText(context, "User ID copied", Toast.LENGTH_SHORT).show()
                                    },
                                color = ZyneBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Copy",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ==========================================
            // 3. EARNINGS & REWARDS PROGRAM
            // ==========================================
            item {
                Text(
                    text = "Rewards & Programs",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ZyneTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ZyneCard),
                    border = BorderStroke(1.dp, ZyneBorder)
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Default.Share,
                            title = "Refer & Earn Program",
                            subtitle = "Earn ₹10 for every invited friend",
                            onClick = onNavigateToReferral
                        )
                        ProfileMenuRow(
                            icon = Icons.Default.Send,
                            title = "Telegram Official Community",
                            subtitle = "Exclusive drop alerts and bonus codes",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.DEFAULT_TELEGRAM_URL))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ==========================================
            // 4. ACCOUNT & SECURITY
            // ==========================================
            item {
                Text(
                    text = "Account & App",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ZyneTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ZyneCard),
                    border = BorderStroke(1.dp, ZyneBorder)
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Default.Security,
                            title = "Device ID & Anti-Fraud Security",
                            subtitle = "Secured with device fingerprint",
                            onClick = {
                                Toast.makeText(context, "Device verification active", Toast.LENGTH_SHORT).show()
                            }
                        )
                        ProfileMenuRow(
                            icon = Icons.Default.HelpOutline,
                            title = "Help & Frequently Asked Questions",
                            subtitle = "Troubleshooting, payouts & tracking",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.DEFAULT_HELP_FAQ_URL))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        )
                        ProfileMenuRow(
                            icon = Icons.Default.Info,
                            title = "About Zyne Offers",
                            subtitle = "Version 1.0.0 (Production Release)",
                            onClick = { showAboutDialog = true }
                        )
                        ProfileMenuRow(
                            icon = Icons.Default.Logout,
                            title = "Sign Out",
                            subtitle = "Log out from your Google account",
                            isDestructive = true,
                            onClick = { showLogoutDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // 5. ADMIN CONSOLE (ONLY FOR ADMIN EMAIL)
            // ==========================================
            if (isCurrentUserAdmin) {
                item {
                    Text(
                        text = "Operator Management",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ZyneTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ZyneCard),
                        border = BorderStroke(1.dp, ZyneBorder)
                    ) {
                        ProfileMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Operator Console",
                            subtitle = "Manage offers, banners & withdrawals",
                            onClick = {
                                if (isAdminAuthenticated) {
                                    onNavigateToAdminConsole()
                                } else {
                                    showAdminPinDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // ADMIN PIN DIALOG
    // ==========================================
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPinDialog = false
                pinInput = ""
                pinError = false
            },
            title = {
                Text("Admin Console Authentication", fontWeight = FontWeight.Bold, color = ZyneTextPrimary, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter your 4-digit Administrator Security PIN to access management tools.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZyneTextSecondary, fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 6) pinInput = it
                            pinError = false
                        },
                        placeholder = { Text("Enter PIN (Default: 1234)", color = ZyneTextSubtle, fontSize = 13.sp) },
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ZyneBackground,
                            unfocusedContainerColor = ZyneBackground,
                            focusedBorderColor = ZyneBlue,
                            unfocusedBorderColor = ZyneBorder
                        )
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incorrect Admin PIN. Please try again.",
                            color = ZyneRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onAuthenticateAdmin(pinInput)
                        if (success) {
                            showAdminPinDialog = false
                            pinInput = ""
                            pinError = false
                            onNavigateToAdminConsole()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZyneBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Authenticate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminPinDialog = false
                        pinInput = ""
                        pinError = false
                    }
                ) {
                    Text("Cancel", color = ZyneTextMuted)
                }
            },
            containerColor = ZyneCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // LOGOUT CONFIRMATION DIALOG
    // ==========================================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Confirm Sign Out", fontWeight = FontWeight.Bold, color = ZyneTextPrimary, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to sign out of your account?",
                    style = MaterialTheme.typography.bodySmall.copy(color = ZyneTextSecondary, fontSize = 13.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZyneRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = ZyneTextMuted)
                }
            },
            containerColor = ZyneCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // ABOUT DIALOG
    // ==========================================
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("About Zyne Offers", fontWeight = FontWeight.Bold, color = ZyneTextPrimary, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Zyne Offers is a premier fintech rewards platform connecting users with verified high-paying partner offers and instant UPI payouts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZyneTextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                    )
                    Text(
                        text = "Version: 1.0.0 Production\nBuild: 2026.08\nAffiliate Engine: Direct Postback v2",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZyneTextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ZyneBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ZyneCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) ZyneRedBg else ZyneBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) ZyneRed else ZyneBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive) ZyneRed else ZyneTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ZyneTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ZyneTextSubtle,
            modifier = Modifier.size(18.dp)
        )
    }
}
