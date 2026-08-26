package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfferEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBlueLight
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle

@Composable
fun OffersWallScreen(
    user: UserEntity?,
    offers: List<OfferEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onNavigateToOfferDetails: (String) -> Unit
) {
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
            // Header Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Offers Wall",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZyneTextPrimary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Discover & complete verified brand tasks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ZyneTextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Surface(
                        color = ZyneBlueLight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ZyneBorder)
                    ) {
                        Text(
                            text = "${filteredOffers.size} Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ZyneBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search by name, app or category...",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ZyneCard,
                        unfocusedContainerColor = ZyneCard,
                        focusedBorderColor = ZyneBlue,
                        unfocusedBorderColor = ZyneBorder,
                        focusedTextColor = ZyneTextPrimary,
                        unfocusedTextColor = ZyneTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Horizontal Category Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Offers List
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
                                text = "No offers found matching '$searchQuery'",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZyneTextPrimary
                                )
                            )
                            Text(
                                text = "Try adjusting your search keywords or filter category.",
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
