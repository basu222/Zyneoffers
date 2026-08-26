package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String, // e.g. ZY000001
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val deviceId: String,
    val registeredAt: Long = System.currentTimeMillis(),
    val referralCode: String,
    val referredByCode: String = "",
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val lifetimeEarnings: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val isBanned: Boolean = false,
    val isAdmin: Boolean = false
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rewardAmount: Double,
    val imageUrl: String,
    val category: String,
    val estimatedMinutes: Int,
    val badge: String, // New, Hot, Limited, Verified, Popular, Exclusive, or empty
    val description: String,
    val instructions: String,
    val requirements: String,
    val terms: String,
    val externalUrl: String,
    val youtubeUrl: String,
    val isPinned: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // Reward Credited, Reward Rejected, Withdrawal Requested, Withdrawal Approved, Withdrawal Paid, Referral Bonus, Manual Adjustment
    val amount: Double,
    val status: String, // Pending, Approved, Paid, Rejected
    val note: String = "",
    val dateTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val amount: Double,
    val upiId: String,
    val status: String, // Pending, Approved, Paid, Rejected
    val rejectionReason: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long = 0L,
    val paidAt: Long = 0L
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "INFO"
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String,
    val targetUrl: String,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val content: String,
    val isActive: Boolean = true,
    val publishedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val trackingUrl: String = "https://tracking.zyneoffers.com",
    val minWithdrawalAmount: Double = 50.0,
    val referralBonusAmount: Double = 10.0,
    val isMaintenanceMode: Boolean = false,
    val telegramUrl: String = "https://t.me/zyneoffers_community",
    val supportEmail: String = "support@zyneoffers.com",
    val supportPhone: String = "+91 98765 43210",
    val privacyPolicyUrl: String = "https://zyneoffers.com/privacy",
    val termsOfServiceUrl: String = "https://zyneoffers.com/terms",
    val aboutText: String = "Zyne Offers is a leading brand rewards platform helping users earn real cash by completing brand tasks and offers with instant UPI payouts.",
    val helpFaqUrl: String = "https://zyneoffers.com/help"
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String, // referralId (e.g. REF_referrerId_friendId)
    val referrerUserId: String,
    val friendUserId: String,
    val friendDisplayName: String,
    val referralCode: String = "",
    val offerId: String = "",
    val bonusAmount: Double = 10.0,
    val status: String = "JOINED_PENDING", // "JOINED_PENDING" (Joined — Waiting for first offer) or "COMPLETED_REWARDED" (Offer Completed — ₹10 Rewarded)
    val isQualified: Boolean = false,
    val dateReferred: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val adminEmail: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offer_activities")
data class OfferActivityEntity(
    @PrimaryKey val id: String, // e.g. TRK_123456789
    val userId: String,
    val offerId: String,
    val offerName: String,
    val trackingId: String,
    val affiliateUrl: String = "",
    val reward: Double = 0.0,
    val status: String = "Started", // "Started", "Pending", "Completed", "Rejected"
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val creditedAt: Long = 0L,
    val adminId: String = "",
    val completionReference: String = "",
    val adminNote: String = ""
)
