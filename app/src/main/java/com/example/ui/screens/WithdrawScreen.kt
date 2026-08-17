package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.theme.ZyneAmber
import com.example.ui.theme.ZyneAmberBg
import com.example.ui.theme.ZyneAmberBorder
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
import com.example.ui.theme.ZyneWalletGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    user: UserEntity?,
    minWithdrawalAmount: Double,
    withdrawals: List<WithdrawalEntity>,
    onRequestWithdrawal: (amount: Double, upiId: String) -> Unit,
    withdrawMessage: String?,
    onClearMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    var amountInput by remember { mutableStateOf("") }
    var upiIdInput by remember { mutableStateOf("") }

    val userBalance = user?.availableBalance ?: 0.0
    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isValidUpi = upiIdInput.contains("@") && upiIdInput.length >= 5
    val isValidAmount = parsedAmount >= minWithdrawalAmount && parsedAmount <= userBalance
    val canSubmit = isValidAmount && isValidUpi

    val quickAmounts = listOf(50, 100, 200, 500)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Withdraw to UPI",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZyneBackground
                )
            )
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
            // Status/Error Message if present
            AnimatedVisibility(visible = withdrawMessage != null) {
                if (withdrawMessage != null) {
                    val isSuccess = withdrawMessage.contains("success", ignoreCase = true) || withdrawMessage.contains("submitted", ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSuccess) ZyneGreenBg else ZyneRedBg,
                        border = BorderStroke(1.dp, if (isSuccess) ZyneGreenBorder else ZyneRedBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isSuccess) ZyneGreen else ZyneRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = withdrawMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSuccess) ZyneGreen else ZyneRed,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            IconButton(
                                onClick = onClearMessage,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = ZyneTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 1. BALANCE SUMMARY CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZyneWalletGradient)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Withdrawable Balance",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${"%.2f".format(userBalance)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 28.sp
                                )
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Min: ₹${minWithdrawalAmount.toInt()}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. AMOUNT INPUT WITH QUICK CHIPS
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Withdrawal Amount (₹)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        placeholder = { Text("Enter amount (e.g. 100)", color = ZyneTextSubtle, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ZyneBackground,
                            unfocusedContainerColor = ZyneBackground,
                            focusedBorderColor = ZyneBlue,
                            unfocusedBorderColor = ZyneBorder,
                            focusedTextColor = ZyneTextPrimary,
                            unfocusedTextColor = ZyneTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick select chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { amountInput = amt.toString() },
                                color = if (amountInput == amt.toString()) ZyneBlueLight else ZyneSurface,
                                border = BorderStroke(1.dp, if (amountInput == amt.toString()) ZyneBlueBorder else ZyneBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₹$amt",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (amountInput == amt.toString()) ZyneBlue else ZyneTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 3. UPI ID INPUT
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UPI ID / VPA",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZyneTextPrimary,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it.trim() },
                        placeholder = { Text("e.g. mobile@paytm or name@okaxis", color = ZyneTextSubtle, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = ZyneTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ZyneBackground,
                            unfocusedContainerColor = ZyneBackground,
                            focusedBorderColor = ZyneBlue,
                            unfocusedBorderColor = ZyneBorder,
                            focusedTextColor = ZyneTextPrimary,
                            unfocusedTextColor = ZyneTextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    if (canSubmit) {
                        onRequestWithdrawal(parsedAmount, upiIdInput)
                        amountInput = ""
                        upiIdInput = ""
                    }
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZyneBlue,
                    contentColor = Color.White,
                    disabledContainerColor = ZyneBorder,
                    disabledContentColor = ZyneTextSubtle
                ),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = if (canSubmit) Color.White else ZyneTextSubtle,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Submit Withdrawal Request",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (canSubmit) Color.White else ZyneTextSubtle,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. WITHDRAWAL HISTORY
            // ==========================================
            if (withdrawals.isNotEmpty()) {
                Text(
                    text = "Withdrawal Requests",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZyneTextPrimary,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                withdrawals.forEach { wd ->
                    val isPending = wd.status.equals("Pending", ignoreCase = true)
                    val isPaid = wd.status.equals("Paid", ignoreCase = true) || wd.status.equals("Completed", ignoreCase = true)
                    val isApproved = wd.status.equals("Approved", ignoreCase = true)

                    val (statusBg, statusColor, statusBorder) = when {
                        isPaid -> Triple(ZyneGreenBg, ZyneGreen, ZyneGreenBorder)
                        isApproved -> Triple(ZyneBlueLight, ZyneBlue, ZyneBlueBorder)
                        isPending -> Triple(ZyneAmberBg, ZyneAmber, ZyneAmberBorder)
                        else -> Triple(ZyneRedBg, ZyneRed, ZyneRedBorder)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            Column {
                                Text(
                                    text = "₹${"%.2f".format(wd.amount)} to ${wd.upiId}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZyneTextPrimary,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = dateFormat.format(Date(wd.requestedAt)),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ZyneTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, statusBorder)
                            ) {
                                Text(
                                    text = wd.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
