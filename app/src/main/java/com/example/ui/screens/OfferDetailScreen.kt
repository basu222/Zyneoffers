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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AppConstants
import com.example.data.local.OfferEntity
import com.example.data.local.UserEntity
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
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneSurface
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offer: OfferEntity?,
    user: UserEntity?,
    onNavigateBack: () -> Unit,
    onStartOffer: ((OfferEntity, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (offer == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZyneBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Offer not found", color = ZyneTextMuted)
        }
        return
    }

    val uniqueTrackingUrl = remember(offer.id, user?.userId) {
        val baseUrl = offer.externalUrl.ifBlank { "https://zyneoffers.com/track/${offer.id}" }
        val separator = if (baseUrl.contains("?")) "&" else "?"
        val userIdParam = user?.userId ?: "ZY_GUEST"
        val clickId = "CLK_${System.currentTimeMillis()}_${(1000..9999).random()}"
        "$baseUrl${separator}sub1=$userIdParam&sub2=${offer.id}&click_id=$clickId"
    }

    val steps = remember(offer.requirements, offer.instructions) {
        if (offer.instructions.isNotBlank()) {
            val lines = offer.instructions.lines()
                .map { it.trim().removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("4.").removePrefix("5.").trim() }
                .filter { it.isNotBlank() }
            if (lines.isNotEmpty()) lines else listOf(
                "Click Claim Offer to open download page",
                "Install and register with your mobile number",
                "Complete basic profile verification",
                "Return to Zyne to receive reward in your wallet"
            )
        } else {
            listOf(
                "Click Claim Offer to open download page",
                "Install and register with your mobile number",
                "Complete basic profile verification",
                "Return to Zyne to receive reward in your wallet"
            )
        }
    }

    val tutorialUrl = if (offer.youtubeUrl.isNotBlank()) offer.youtubeUrl else AppConstants.DEFAULT_YOUTUBE_TUTORIAL_URL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Offer Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 17.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZyneTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uniqueTrackingUrl))
                            Toast.makeText(context, "Tracking link copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Copy Link",
                            tint = ZyneTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZyneBackground
                )
            )
        },
        bottomBar = {
            // Fixed Bottom CTA Bar matching UI Reference
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ZyneCard,
                border = BorderStroke(1.dp, ZyneBorder),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOU WILL EARN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextMuted,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "₹${offer.rewardAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ZyneTextPrimary,
                                fontSize = 20.sp
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onStartOffer?.invoke(offer, uniqueTrackingUrl)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uniqueTrackingUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening offer: $uniqueTrackingUrl", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZyneBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Start Offer",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = ZyneBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ==========================================
            // 1. APP HEADER: Logo + Verified Title + Category + Active Pill
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ZyneSurface)
                        .border(1.dp, ZyneBorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (offer.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = offer.imageUrl,
                            contentDescription = offer.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = offer.name.firstOrNull()?.uppercase() ?: "O",
                            color = ZyneBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = offer.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = ZyneBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = offer.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ZyneTextMuted,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Surface(
                    color = ZyneGreenBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ZyneGreenBorder)
                ) {
                    Text(
                        text = "ACTIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneGreen,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description Paragraph
            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ZyneTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. STATS CARDS ROW: REWARD & ESTIMATED TIME
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reward Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ZyneCard),
                    border = BorderStroke(1.dp, ZyneBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ZyneBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = ZyneBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "REWARD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextMuted,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "₹${offer.rewardAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextPrimary,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }

                // Estimated Time Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ZyneCard),
                    border = BorderStroke(1.dp, ZyneBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ZyneBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ZyneBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "TIME NEEDED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextMuted,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "${offer.estimatedMinutes} mins",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextPrimary,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 3. WATCH YOUTUBE TUTORIAL CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "YouTube Tutorial",
                                tint = ZyneRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Watch YouTube Tutorial",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneTextPrimary,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "Step-by-step guide to complete this offer",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ZyneTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tutorialUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening tutorial: $tutorialUrl", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            color = ZyneBlueLight,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ZyneBlueBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Watch",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneBlue,
                                        fontSize = 11.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = ZyneBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Thumbnail Card Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                                )
                            )
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tutorialUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening tutorial: $tutorialUrl", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = offer.name.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "HOW TO COMPLETE OFFER",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        // Big Play Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ZyneBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Duration Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${offer.estimatedMinutes}:00",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 4. OFFER REQUIREMENTS
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ZyneBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Offer Requirements",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextPrimary,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ZyneBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = offer.requirements.ifBlank { "Mobile number verification & basic profile setup." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ZyneTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 5. STEP-BY-STEP INSTRUCTIONS
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ZyneBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Step-by-Step Instructions",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextPrimary,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(ZyneBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ZyneTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 6. AFFILIATE TRACKING & POSTBACK
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = ZyneBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Affiliate Tracking & Postback",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextPrimary,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your affiliate click ID and user token are automatically embedded. Rewards are credited automatically after verification.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ZyneTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
