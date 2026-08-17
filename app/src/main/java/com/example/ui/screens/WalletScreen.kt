package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBlueBorder
import com.example.ui.theme.ZyneBlueDark
import com.example.ui.theme.ZyneBlueDeep
import com.example.ui.theme.ZyneBlueLight
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneGreen
import com.example.ui.theme.ZyneGreenBg
import com.example.ui.theme.ZyneGreenBorder
import com.example.ui.theme.ZyneHeroGradient
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneRedBg
import com.example.ui.theme.ZyneRedBorder
import com.example.ui.theme.ZyneSurface
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle
import com.example.ui.theme.ZyneWalletGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    user: UserEntity?,
    transactions: List<TransactionEntity>,
    minWithdrawalAmount: Double,
    onNavigateToWithdraw: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

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
            // 1. TOP HEADER: Wallet Icon + My Wallet
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZyneBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = ZyneBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "My Wallet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            // ==========================================
            // 2. HERO AVAILABLE BALANCE CARD (BLUE GRADIENT)
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ZyneWalletGradient)
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "₹${"%.2f".format(user?.availableBalance ?: 0.0)}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 34.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Minimum withdrawal: ₹${minWithdrawalAmount.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Withdraw to UPI Button
                            Button(
                                onClick = onNavigateToWithdraw,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = ZyneBlueDark
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = ZyneBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Withdraw to UPI",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ZyneBlueDark,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ==========================================
            // 3. STATS CARDS ROW: Pending, Lifetime, Paid Out
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pending Box
                    StatSummaryCard(
                        title = "Pending",
                        amount = "₹${"%.2f".format(user?.pendingBalance ?: 0.0)}",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )

                    // Lifetime Box
                    StatSummaryCard(
                        title = "Lifetime",
                        amount = "₹${"%.2f".format(user?.lifetimeEarnings ?: 0.0)}",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )

                    // Paid Out Box
                    StatSummaryCard(
                        title = "Paid Out",
                        amount = "₹${"%.2f".format(user?.totalWithdrawals ?: 0.0)}",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ==========================================
            // 4. RECENT TRANSACTIONS HEADER
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ZyneTextMuted,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // ==========================================
            // 5. TRANSACTIONS LIST / CLEAN EMPTY STATE
            // ==========================================
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ZyneBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = ZyneBlue,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = "No transactions yet",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextPrimary,
                                    fontSize = 15.sp
                                )
                            )

                            Text(
                                text = "Complete offers to earn rewards.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ZyneTextMuted,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            } else {
                items(transactions.take(15), key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        formattedDate = dateFormat.format(Date(tx.dateTimestamp))
                    )
                }
            }
        }
    }
}

@Composable
private fun StatSummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZyneCard),
        border = BorderStroke(1.dp, ZyneBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ZyneBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ZyneBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ZyneTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ZyneTextPrimary,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionEntity,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    val isCredit = transaction.type.equals("CREDIT", ignoreCase = true) || transaction.type.equals("REWARD", ignoreCase = true) || transaction.type.equals("REFERRAL", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZyneCard),
        border = BorderStroke(1.dp, ZyneBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isCredit) ZyneGreenBg else ZyneRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) ZyneGreen else ZyneRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.note.ifBlank { transaction.type },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ZyneTextPrimary,
                            fontSize = 14.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ZyneTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${"%.2f".format(transaction.amount)}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) ZyneGreen else ZyneRed,
                    fontSize = 15.sp
                )
            )
        }
    }
}
