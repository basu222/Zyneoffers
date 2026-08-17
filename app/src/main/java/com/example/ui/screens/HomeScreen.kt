package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AppConstants
import com.example.data.local.AnnouncementEntity
import com.example.data.local.BannerEntity
import com.example.data.local.OfferEntity
import com.example.data.local.UserEntity
import com.example.ui.components.ZyneOfficialLogo
import com.example.ui.theme.ZyneAmber
import com.example.ui.theme.ZyneAmberBg
import com.example.ui.theme.ZyneAmberBorder
import com.example.ui.theme.ZyneAvatarGradient
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBlueBorder
import com.example.ui.theme.ZyneBlueDark
import com.example.ui.theme.ZyneBlueLight
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneBorderSubtle
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneGreen
import com.example.ui.theme.ZyneGreenBg
import com.example.ui.theme.ZyneGreenBorder
import com.example.ui.theme.ZyneHeroGradient
import com.example.ui.theme.ZynePurple
import com.example.ui.theme.ZynePurpleBg
import com.example.ui.theme.ZynePurpleBorder
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneRedBg
import com.example.ui.theme.ZyneRedBorder
import com.example.ui.theme.ZyneSurface
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle

@Composable
fun HomeScreen(
    user: UserEntity?,
    announcement: AnnouncementEntity?,
    banners: List<BannerEntity>,
    offers: List<OfferEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    unreadCount: Int,
    onNavigateToOfferDetails: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTracking: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val categories = listOf("All", "Finance", "Banking", "Shopping", "Gaming", "Surveys", "Crypto")

    val filteredOffers = remember(offers, selectedCategory, searchQuery) {
        offers.filter { offer ->
            val matchesCategory = if (selectedCategory == "All") true else offer.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                offer.name.contains(searchQuery, ignoreCase = true) ||
                offer.description.contains(searchQuery, ignoreCase = true) ||
                offer.category.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

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
            // 1. TOP HEADER: Zyne Logo + Notification + Profile
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZyneOfficialLogo(
                        markSize = 34.dp,
                        fontSize = 20,
                        textColor = ZyneTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Notification Bell Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZyneCard)
                                .border(1.dp, ZyneBorder, RoundedCornerShape(10.dp))
                                .clickable { onNavigateToNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ZyneTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(ZyneRed)
                                        .align(Alignment.TopEnd)
                                        .padding(1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ZyneAvatarGradient)
                                .clickable { onNavigateToProfile() },
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
                                val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "Z"
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // ANNOUNCEMENT TICKER (IF ACTIVE)
            // ==========================================
            if (announcement != null && announcement.isActive) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        color = ZyneBlueLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ZyneBlueBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = ZyneBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = announcement.content,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ZyneBlueDark,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // ==========================================
            // 2. HERO CARD: Join our Telegram Community
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ZyneHeroGradient)
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Join our Telegram Community",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Get exclusive offers, updates &\nearn more rewards!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.DEFAULT_TELEGRAM_URL))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = ZyneTextPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Join Now",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ZyneTextPrimary,
                                                fontSize = 13.sp
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = ZyneTextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 3D-styled Telegram Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Telegram",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. OFFERS WALL SECTION HEADER + VIEW ALL
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offers Wall",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 17.sp
                        )
                    )

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ZyneTextMuted,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.clickable {
                            // Reset filter to All to view everything
                            onSelectCategory("All")
                        }
                    )
                }
            }

            // ==========================================
            // 4. CATEGORY FILTER PILLS (HORIZONTAL SCROLL)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category.equals(selectedCategory, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onSelectCategory(category) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) ZyneBlue else ZyneCard,
                            border = if (isSelected) null else BorderStroke(1.dp, ZyneBorder)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else ZyneTextSecondary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ==========================================
            // 5. SEARCH INPUT BAR
            // ==========================================
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    placeholder = {
                        Text(
                            text = "Search offers, games, finance...",
                            color = ZyneTextSubtle,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ZyneTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = ZyneTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ZyneCard,
                        unfocusedContainerColor = ZyneCard,
                        focusedBorderColor = ZyneBlue,
                        unfocusedBorderColor = ZyneBorder,
                        focusedTextColor = ZyneTextPrimary,
                        unfocusedTextColor = ZyneTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ==========================================
            // 6. OFFER CARDS LIST
            // ==========================================
            if (filteredOffers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ZyneBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = ZyneBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = "No offers found",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextPrimary
                                )
                            )
                            Text(
                                text = "Try selecting a different category or search term.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ZyneTextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredOffers, key = { it.id }) { offer ->
                    OfferItemCard(
                        offer = offer,
                        onClick = { onNavigateToOfferDetails(offer.id) }
                    )
                }
            }
        }
    }
}

/**
 * Compact, visually distinct Offer Preview Card:
 * - Subtle light-blue/gray tinted surface (ZyneSurface) + thin border + subtle elevation
 * - Offer Logo / Image on left
 * - Offer Name + Category + Status Badge
 * - Reward Amount (₹) + Simple "Start →" button
 * - NO full description, NO coins, NO requirements shown here.
 */
@Composable
fun OfferItemCard(
    offer: OfferEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZyneSurface),
        border = BorderStroke(1.dp, ZyneBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Logo + (Title & Category) + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offer Logo / Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, ZyneBorderSubtle, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (offer.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = offer.imageUrl,
                            contentDescription = offer.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback monogram
                        Text(
                            text = offer.name.firstOrNull()?.uppercase() ?: "O",
                            color = ZyneBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = offer.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = offer.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ZyneTextMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }

                if (offer.badge.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OfferStatusBadge(badge = offer.badge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Reward (₹) on left + "Start ->" button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${offer.rewardAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = ZyneTextPrimary,
                        fontSize = 18.sp
                    )
                )

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZyneBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status Badge matching reference image styling:
 * HOT (Soft Red), BEST REWARD (Soft Green), INSTANT (Soft Blue), POPULAR (Soft Purple)
 */
@Composable
fun OfferStatusBadge(
    badge: String,
    modifier: Modifier = Modifier
) {
    val upper = badge.uppercase().trim()
    val (bgColor, textColor, borderColor) = when {
        upper.contains("HOT") || upper.contains("CRITICAL") -> Triple(ZyneRedBg, ZyneRed, ZyneRedBorder)
        upper.contains("BEST") || upper.contains("TOP") || upper.contains("COMPLETED") || upper.contains("ACTIVE") -> Triple(ZyneGreenBg, ZyneGreen, ZyneGreenBorder)
        upper.contains("INSTANT") || upper.contains("NEW") || upper.contains("VERIFIED") -> Triple(ZyneBlueLight, ZyneBlue, ZyneBlueBorder)
        upper.contains("POPULAR") || upper.contains("PRO") -> Triple(ZynePurpleBg, ZynePurple, ZynePurpleBorder)
        else -> Triple(ZyneAmberBg, ZyneAmber, ZyneAmberBorder)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = upper,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp
            )
        )
    }
}
