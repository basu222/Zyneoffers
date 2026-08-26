package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AnnouncementEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.BannerEntity
import com.example.data.local.OfferEntity
import com.example.data.local.SettingsEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderLight
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSky
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    users: List<UserEntity>,
    offers: List<OfferEntity>,
    withdrawals: List<WithdrawalEntity>,
    banners: List<BannerEntity>,
    announcement: AnnouncementEntity?,
    settings: SettingsEntity,
    auditLogs: List<AuditLogEntity>,
    onCreditReward: (userId: String, amount: Double, offerName: String, note: String) -> Unit,
    onRejectReward: (userId: String, offerName: String, reason: String) -> Unit,
    onApproveWithdrawal: (withdrawalId: String) -> Unit,
    onMarkWithdrawalPaid: (withdrawalId: String) -> Unit,
    onRejectWithdrawal: (withdrawalId: String, reason: String) -> Unit,
    onSaveOffer: (OfferEntity) -> Unit,
    onDeleteOffer: (offerId: String) -> Unit,
    onSaveBanner: (BannerEntity) -> Unit,
    onDeleteBanner: (bannerId: String) -> Unit,
    onSaveAnnouncement: (AnnouncementEntity) -> Unit,
    onUpdateSettings: (SettingsEntity) -> Unit,
    onToggleUserBan: (userId: String) -> Unit,
    onResetDeviceBinding: (userId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Withdrawals", "Offers", "Users", "Banners", "Settings", "Audit Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = RoyalBlueAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Admin Operator Console",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimaryDark,
                    navigationIconContentColor = TextPrimaryDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = DarkSurface,
                contentColor = RoyalBlueAccent,
                divider = { Divider(color = DarkBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) RoyalBlueAccent else TextSecondaryDark
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AdminDashboardTab(users, offers, withdrawals)
                    1 -> AdminWithdrawalsTab(withdrawals, onApproveWithdrawal, onMarkWithdrawalPaid, onRejectWithdrawal, dateFormat)
                    2 -> AdminOffersTab(offers, onSaveOffer, onDeleteOffer)
                    3 -> AdminUsersTab(users, offers, onCreditReward, onRejectReward, onToggleUserBan, onResetDeviceBinding)
                    4 -> AdminBannersTab(banners, announcement, onSaveBanner, onDeleteBanner, onSaveAnnouncement)
                    5 -> AdminSettingsTab(settings, onUpdateSettings)
                    6 -> AdminAuditLogsTab(auditLogs, dateFormat)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardTab(
    users: List<UserEntity>,
    offers: List<OfferEntity>,
    withdrawals: List<WithdrawalEntity>
) {
    val pendingWithdrawals = withdrawals.count { it.status == "Pending" }
    val totalPaidOut = withdrawals.filter { it.status == "Paid" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Platform Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Total Users",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Offers",
                    value = "${offers.count { it.isActive }}",
                    icon = Icons.Default.LocalOffer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Pending WDs",
                    value = "$pendingWithdrawals",
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Paid Out",
                    value = "₹${totalPaidOut.toInt()}",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RoyalBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoyalBlueAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
            )
        }
    }
}

@Composable
private fun AdminWithdrawalsTab(
    withdrawals: List<WithdrawalEntity>,
    onApprove: (String) -> Unit,
    onMarkPaid: (String) -> Unit,
    onReject: (String, String) -> Unit,
    dateFormat: SimpleDateFormat
) {
    var filterStatus by remember { mutableStateOf("Pending") }
    val statuses = listOf("Pending", "Approved", "Paid", "Rejected", "All")

    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedWdId by remember { mutableStateOf("") }
    var rejectReasonInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = { filterStatus = status },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBluePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurface,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterStatus == status,
                        borderColor = DarkBorder,
                        selectedBorderColor = RoyalBluePrimary
                    )
                )
            }
        }

        val filteredList = withdrawals.filter {
            filterStatus == "All" || it.status.equals(filterStatus, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { wd ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${wd.userName} (${wd.userId})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = "UPI ID: ${wd.upiId}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlueSky)
                                )
                                Text(
                                    text = dateFormat.format(Date(wd.requestedAt)),
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
                                )
                            }
                            Text(
                                text = "₹${wd.amount.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlueAccent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (wd.status == "Pending") {
                                Button(
                                    onClick = { onApprove(wd.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Approve", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = {
                                        selectedWdId = wd.id
                                        rejectReasonInput = "Invalid UPI ID / Policy Violation"
                                        showRejectDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DarkBorderLight)
                                ) {
                                    Text("Reject", color = TextSecondaryDark)
                                }
                            } else if (wd.status == "Approved") {
                                Button(
                                    onClick = { onMarkPaid(wd.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Mark Paid (Send UPI Payout)", color = Color.White)
                                }
                            } else {
                                Surface(
                                    color = DarkSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Status: ${wd.status}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (wd.status == "Paid") RoyalBlueAccent else TextMutedDark
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Reject Withdrawal", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = rejectReasonInput,
                    onValueChange = { rejectReasonInput = it },
                    label = { Text("Reason for Rejection", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(selectedWdId, rejectReasonInput)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Confirm Reject", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun AdminOffersTab(
    offers: List<OfferEntity>,
    onSaveOffer: (OfferEntity) -> Unit,
    onDeleteOffer: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingOffer by remember { mutableStateOf<OfferEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Brand Offers (${offers.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
            Button(
                onClick = {
                    editingOffer = OfferEntity(
                        id = "OFFER_${System.currentTimeMillis()}",
                        name = "",
                        rewardAmount = 100.0,
                        imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500",
                        category = "Finance",
                        estimatedMinutes = 5,
                        badge = "Hot",
                        description = "Register and complete simple brand task to get rewarded.",
                        instructions = "1. Click Start Offer link.\n2. Sign up with mobile number & email.\n3. Complete KYC/first action.",
                        requirements = "New users only • Complete KYC verification",
                        terms = "Reward will be verified within 24 hours of task completion.",
                        externalUrl = "https://zyneoffers.com/task",
                        youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        isPinned = false,
                        isActive = true
                    )
                    showEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Offer", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(offers) { offer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (offer.isPinned) RoyalBluePrimary else DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Offer Image Preview
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            AsyncImage(
                                model = offer.imageUrl,
                                contentDescription = offer.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = offer.name.ifBlank { "Untitled Offer" },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (offer.badge.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = GoldSecondary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = offer.badge.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "₹${offer.rewardAmount.toInt()} • ${offer.category} • ${offer.estimatedMinutes}m",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RoyalBlueAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            // YouTube Guidance Badge indicator
                            if (offer.youtubeUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Video Guide",
                                        tint = Color(0xFFFF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "YouTube Guide Attached",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFF8888),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                onSaveOffer(offer.copy(isPinned = !offer.isPinned))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (offer.isPinned) RoyalBlueAccent else TextMutedDark
                                )
                            }
                            IconButton(onClick = {
                                editingOffer = offer
                                showEditDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextPrimaryDark
                                )
                            }
                            IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && editingOffer != null) {
        val o = editingOffer!!
        var name by remember { mutableStateOf(o.name) }
        var reward by remember { mutableStateOf(o.rewardAmount.toString()) }
        var category by remember { mutableStateOf(o.category) }
        var estimatedMins by remember { mutableStateOf(o.estimatedMinutes.toString()) }
        var badge by remember { mutableStateOf(o.badge) }
        var imageUrl by remember { mutableStateOf(o.imageUrl) }
        var youtubeUrl by remember { mutableStateOf(o.youtubeUrl) }
        var desc by remember { mutableStateOf(o.description) }
        var instructions by remember { mutableStateOf(o.instructions) }
        var requirements by remember { mutableStateOf(o.requirements) }
        var terms by remember { mutableStateOf(o.terms) }
        var extUrl by remember { mutableStateOf(o.externalUrl) }
        var isPinned by remember { mutableStateOf(o.isPinned) }
        var isActive by remember { mutableStateOf(o.isActive) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = if (o.name.isBlank()) "Add New Brand Offer" else "Edit Offer Details",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Image URL Section with live preview
                    Text(
                        text = "Offer Image / Logo URL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlueSky
                        )
                    )
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL (HTTPS)", color = TextMutedDark) },
                        placeholder = { Text("https://example.com/logo.png", color = TextMutedDark) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = RoyalBlueAccent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    // Image Preview Box
                    if (imageUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Image Preview OK",
                                style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlueAccent)
                            )
                        }
                    }

                    // YouTube Guidance Link Section
                    Text(
                        text = "YouTube Video Guidance Link",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8888)
                        )
                    )
                    OutlinedTextField(
                        value = youtubeUrl,
                        onValueChange = { youtubeUrl = it },
                        label = { Text("YouTube Video Guide Link", color = TextMutedDark) },
                        placeholder = { Text("https://www.youtube.com/watch?v=...", color = TextMutedDark) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFF4444))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

                    // Basic Details
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Offer Name (e.g. AngelOne, Kotak 811)", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = reward,
                            onValueChange = { reward = it },
                            label = { Text("Reward (₹)", color = TextMutedDark) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = RoyalBluePrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                        OutlinedTextField(
                            value = estimatedMins,
                            onValueChange = { estimatedMins = it },
                            label = { Text("Mins", color = TextMutedDark) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = RoyalBluePrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category (Finance, etc)", color = TextMutedDark) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = RoyalBluePrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                        OutlinedTextField(
                            value = badge,
                            onValueChange = { badge = it },
                            label = { Text("Badge (Hot, New)", color = TextMutedDark) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = RoyalBluePrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                    }

                    OutlinedTextField(
                        value = extUrl,
                        onValueChange = { extUrl = it },
                        label = { Text("Task / Tracking URL", color = TextMutedDark) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = RoyalBlueAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Step-by-Step Instructions", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    OutlinedTextField(
                        value = requirements,
                        onValueChange = { requirements = it },
                        label = { Text("Requirements & Conditions", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Terms & Verification Window", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                colors = CheckboxDefaults.colors(checkedColor = RoyalBluePrimary)
                            )
                            Text("Pin to Top", color = TextPrimaryDark, style = MaterialTheme.typography.bodySmall)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active:", color = TextPrimaryDark, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlueAccent, checkedTrackColor = RoyalBluePrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveOffer(
                            o.copy(
                                name = name.ifBlank { "Brand Offer" },
                                rewardAmount = reward.toDoubleOrNull() ?: o.rewardAmount,
                                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500" },
                                youtubeUrl = youtubeUrl,
                                category = category.ifBlank { "Finance" },
                                estimatedMinutes = estimatedMins.toIntOrNull() ?: 5,
                                badge = badge,
                                description = desc,
                                instructions = instructions,
                                requirements = requirements,
                                terms = terms,
                                externalUrl = extUrl.ifBlank { "https://zyneoffers.com" },
                                isPinned = isPinned,
                                isActive = isActive
                            )
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save Offer", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserEntity>,
    offers: List<OfferEntity>,
    onCreditReward: (userId: String, amount: Double, offerName: String, note: String) -> Unit,
    onRejectReward: (userId: String, offerName: String, reason: String) -> Unit,
    onToggleUserBan: (userId: String) -> Unit,
    onResetDeviceBinding: (userId: String) -> Unit
) {
    var userQuery by remember { mutableStateOf("") }
    var selectedUserForAction by remember { mutableStateOf<UserEntity?>(null) }
    var showCreditModal by remember { mutableStateOf(false) }

    var creditAmountInput by remember { mutableStateOf("100") }
    var offerNameInput by remember { mutableStateOf(offers.firstOrNull()?.name ?: "AngelOne Trading Account") }
    var noteInput by remember { mutableStateOf("Manual Verification Passed") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = userQuery,
            onValueChange = { userQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search User ID or Email...", color = TextMutedDark) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RoyalBlueAccent) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark,
                focusedBorderColor = RoyalBluePrimary,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(14.dp)
        )

        val filtered = users.filter {
            it.userId.contains(userQuery, ignoreCase = true) || it.email.contains(userQuery, ignoreCase = true) || it.displayName.contains(userQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (u.isAdmin) RoyalBluePrimary else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${u.displayName} (${u.userId})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    )
                                    if (u.isAdmin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = RoyalBluePrimary, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                "ADMIN",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = u.email, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                                Text(
                                    text = "Balance: ₹${u.availableBalance.toInt()} • Device: ${u.deviceId.take(14)}...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlueSky)
                                )
                            }
                            if (u.isBanned) {
                                Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(6.dp)) {
                                    Text("BANNED", style = MaterialTheme.typography.labelSmall.copy(color = Color.White), modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    selectedUserForAction = u
                                    showCreditModal = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Credit Reward", fontSize = 11.sp, color = Color.White) }

                            OutlinedButton(
                                onClick = { onToggleUserBan(u.userId) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DarkBorderLight)
                            ) { Text(if (u.isBanned) "Unban" else "Ban User", fontSize = 11.sp, color = TextSecondaryDark) }

                            OutlinedButton(
                                onClick = { onResetDeviceBinding(u.userId) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DarkBorderLight)
                            ) { Text("Reset Device", fontSize = 11.sp, color = TextSecondaryDark) }
                        }
                    }
                }
            }
        }
    }

    if (showCreditModal && selectedUserForAction != null) {
        val targetUser = selectedUserForAction!!
        AlertDialog(
            onDismissRequest = { showCreditModal = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Credit Reward to ${targetUser.displayName} (${targetUser.userId})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = offerNameInput,
                        onValueChange = { offerNameInput = it },
                        label = { Text("Offer Name", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    OutlinedTextField(
                        value = creditAmountInput,
                        onValueChange = { creditAmountInput = it },
                        label = { Text("Amount (₹)", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Verification Note", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = creditAmountInput.toDoubleOrNull() ?: 0.0
                        onCreditReward(targetUser.userId, amt, offerNameInput, noteInput)
                        showCreditModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Confirm Credit", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreditModal = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

@Composable
private fun AdminBannersTab(
    banners: List<BannerEntity>,
    announcement: AnnouncementEntity?,
    onSaveBanner: (BannerEntity) -> Unit,
    onDeleteBanner: (String) -> Unit,
    onSaveAnnouncement: (AnnouncementEntity) -> Unit
) {
    var annText by remember { mutableStateOf(announcement?.content ?: "") }
    var annActive by remember { mutableStateOf(announcement?.isActive ?: true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live System Announcement Banner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = annText,
                        onValueChange = { annText = it },
                        label = { Text("Announcement Message", color = TextMutedDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalBluePrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Banner:", color = TextPrimaryDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = annActive,
                                onCheckedChange = { annActive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlueAccent, checkedTrackColor = RoyalBluePrimary)
                            )
                        }
                        Button(
                            onClick = {
                                onSaveAnnouncement(AnnouncementEntity(id = "ANN_01", content = annText, isActive = annActive))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Update", color = Color.White) }
                    }
                }
            }
        }

        item {
            Text(
                text = "Promo Carousel Banners (${banners.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            )
        }

        items(banners) { b ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        AsyncImage(
                            model = b.imageUrl,
                            contentDescription = b.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = b.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                        Text(text = b.targetUrl, style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark))
                    }
                    IconButton(onClick = { onDeleteBanner(b.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsTab(
    settings: SettingsEntity,
    onUpdateSettings: (SettingsEntity) -> Unit
) {
    var trackingUrl by remember { mutableStateOf(settings.trackingUrl) }
    var minWd by remember { mutableStateOf(settings.minWithdrawalAmount.toString()) }
    var refBonus by remember { mutableStateOf(settings.referralBonusAmount.toString()) }
    var isMaintenance by remember { mutableStateOf(settings.isMaintenanceMode) }
    var telegram by remember { mutableStateOf(settings.telegramUrl) }
    var supportEmail by remember { mutableStateOf(settings.supportEmail) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Global Platform Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Maintenance Mode:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark))
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isMaintenance,
                        onCheckedChange = { isMaintenance = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlueAccent, checkedTrackColor = RoyalBluePrimary)
                    )
                }

                OutlinedTextField(
                    value = trackingUrl,
                    onValueChange = { trackingUrl = it },
                    label = { Text("Tracking Website URL", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
                OutlinedTextField(
                    value = minWd,
                    onValueChange = { minWd = it },
                    label = { Text("Minimum Withdrawal (₹)", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
                OutlinedTextField(
                    value = refBonus,
                    onValueChange = { refBonus = it },
                    label = { Text("Referral Bonus Amount (₹)", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("Telegram Community Link", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
                OutlinedTextField(
                    value = supportEmail,
                    onValueChange = { supportEmail = it },
                    label = { Text("Support Email", color = TextMutedDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalBluePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onUpdateSettings(
                            settings.copy(
                                trackingUrl = trackingUrl,
                                minWithdrawalAmount = minWd.toDoubleOrNull() ?: settings.minWithdrawalAmount,
                                referralBonusAmount = refBonus.toDoubleOrNull() ?: settings.referralBonusAmount,
                                isMaintenanceMode = isMaintenance,
                                telegramUrl = telegram,
                                supportEmail = supportEmail
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Global Settings", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminAuditLogsTab(
    auditLogs: List<AuditLogEntity>,
    dateFormat: SimpleDateFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${log.action} by ${log.adminEmail}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlueAccent)
                    )
                    Text(text = log.details, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark))
                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
                    )
                }
            }
        }
    }
}
