package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AnnouncementEntity
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.local.BannerEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OfferActivityEntity
import com.example.data.local.OfferEntity
import com.example.data.local.ReferralEntity
import com.example.data.local.SettingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class ZyneRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val offerDao = db.offerDao()
    private val offerActivityDao = db.offerActivityDao()
    private val transactionDao = db.transactionDao()
    private val withdrawalDao = db.withdrawalDao()
    private val notificationDao = db.notificationDao()
    private val bannerDao = db.bannerDao()
    private val announcementDao = db.announcementDao()
    private val settingsDao = db.settingsDao()
    private val referralDao = db.referralDao()
    private val auditLogDao = db.auditLogDao()

    val firebaseManager = FirebaseManager()

    private val prefs = context.getSharedPreferences("zyne_prefs", Context.MODE_PRIVATE)
    private val _currentUserId = MutableStateFlow<String?>(prefs.getString("logged_in_user_id", null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            seedInitialDataIfNeeded()

            val savedUserId = prefs.getString("logged_in_user_id", null)
            if (savedUserId != null) {
                _currentUserId.value = savedUserId
                attachUserListeners(savedUserId)
            } else {
                // Check if Firebase Auth has an active session
                val firebaseUid = firebaseManager.getCurrentFirebaseUserUid()
                val firebaseEmail = firebaseManager.getCurrentFirebaseUserEmail()
                if (firebaseUid != null) {
                    val existingUser = userDao.getUserByIdDirect(firebaseUid)
                        ?: (if (firebaseEmail != null) userDao.getUserByEmail(firebaseEmail) else null)
                        ?: firebaseManager.fetchUserById(firebaseUid)
                        ?: (if (firebaseEmail != null) firebaseManager.fetchUserByEmail(firebaseEmail) else null)

                    if (existingUser != null) {
                        userDao.insertOrUpdateUser(existingUser)
                        _currentUserId.value = existingUser.userId
                        prefs.edit().putString("logged_in_user_id", existingUser.userId).apply()
                        attachUserListeners(existingUser.userId)
                    }
                }
            }

            // Start Firebase Firestore real-time synchronization for all collections
            firebaseManager.startRealtimeSync(
                scope = coroutineScope,
                onOffersUpdated = { offers ->
                    if (offers.isNotEmpty()) {
                        offerDao.deleteAllOffers()
                        offerDao.insertOrUpdateOffers(offers)
                    }
                },
                onBannersUpdated = { banners ->
                    if (banners.isNotEmpty()) {
                        bannerDao.deleteAllBanners()
                        banners.forEach { bannerDao.insertOrUpdateBanner(it) }
                    }
                },
                onAnnouncementsUpdated = { announcements ->
                    if (announcements.isNotEmpty()) {
                        announcementDao.deleteAllAnnouncements()
                        announcements.forEach { announcementDao.insertOrUpdateAnnouncement(it) }
                    }
                },
                onSettingsUpdated = { settings ->
                    settingsDao.updateSettings(settings)
                },
                onWithdrawalsUpdated = { withdrawals ->
                    withdrawals.forEach { withdrawalDao.insertOrUpdateWithdrawal(it) }
                },
                onUsersUpdated = { users ->
                    users.forEach { userDao.insertOrUpdateUser(it) }
                },
                onTransactionsUpdated = { transactions ->
                    transactions.forEach { transactionDao.insertTransaction(it) }
                },
                onReferralsUpdated = { referrals ->
                    referralDao.insertOrUpdateReferrals(referrals)
                }
            )
        }
    }

    private fun attachUserListeners(userId: String) {
        firebaseManager.listenToUser(userId, coroutineScope) { updatedUser ->
            if (updatedUser != null) {
                userDao.insertOrUpdateUser(updatedUser)
            }
        }
        firebaseManager.listenToUserTransactions(userId, coroutineScope) { transactions ->
            transactions.forEach { transactionDao.insertTransaction(it) }
        }
        firebaseManager.listenToUserNotifications(userId, coroutineScope) { notifications ->
            notifications.forEach { notificationDao.insertNotification(it) }
        }
        firebaseManager.listenToUserReferrals(userId, coroutineScope) { referrals ->
            referralDao.insertOrUpdateReferrals(referrals)
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingSettings = settingsDao.getSettingsDirect()
        if (existingSettings == null) {
            val initialSettings = SettingsEntity(
                id = 1,
                trackingUrl = "https://tracking.zyneoffers.com",
                minWithdrawalAmount = 50.0,
                referralBonusAmount = 10.0,
                isMaintenanceMode = false,
                telegramUrl = "https://t.me/zyneoffers_community",
                supportEmail = "support@zyneoffers.com",
                supportPhone = "+91 98765 43210",
                privacyPolicyUrl = "https://zyneoffers.com/privacy",
                termsOfServiceUrl = "https://zyneoffers.com/terms",
                aboutText = "Zyne Offers is a leading brand rewards platform helping users earn real cash by completing brand tasks and offers with instant UPI payouts.",
                helpFaqUrl = "https://zyneoffers.com/help"
            )
            settingsDao.updateSettings(initialSettings)
        }
    }

    // --- AUTHENTICATION & DEVICE BINDING ---
    suspend fun loginWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String,
        deviceId: String,
        inputReferralCode: String?
    ): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            val isAuthorizedAdmin = normalizedEmail == "buddepubasu123@gmail.com"

            // Check Firestore first for authoritative user state
            val firestoreUser = firebaseManager.fetchUserByEmail(normalizedEmail)
            val localUser = userDao.getUserByEmail(normalizedEmail)
            val existingUser = firestoreUser ?: localUser

            val existingUserByDevice = userDao.getUserByDeviceId(deviceId)
            if (existingUserByDevice != null && !existingUserByDevice.email.equals(normalizedEmail, ignoreCase = true)) {
                return@withContext Result.failure(
                    Exception("Security Restriction: This device is already registered to another account (${existingUserByDevice.email}). Only 1 account per device is permitted.")
                )
            }

            val userToReturn: UserEntity = if (existingUser != null) {
                if (existingUser.isBanned) {
                    return@withContext Result.failure(
                        Exception("Your account has been suspended. Please contact support.")
                    )
                }
                val updatedUser = existingUser.copy(
                    deviceId = deviceId,
                    displayName = displayName.ifBlank { existingUser.displayName },
                    photoUrl = photoUrl.ifBlank { existingUser.photoUrl },
                    isAdmin = isAuthorizedAdmin || existingUser.isAdmin
                )
                userDao.insertOrUpdateUser(updatedUser)
                firebaseManager.saveUser(updatedUser)
                updatedUser
            } else {
                // First-time Registration in Firebase
                val newUserId = generateUniqueUserId()
                var referredBy = ""
                if (!inputReferralCode.isNullOrBlank()) {
                    val code = inputReferralCode.trim().uppercase()
                    val referrer = userDao.getUserByIdDirect(code) ?: firebaseManager.fetchUserById(code)
                    if (referrer != null && referrer.userId != newUserId) {
                        referredBy = referrer.userId
                    }
                }

                val newUser = UserEntity(
                    userId = newUserId,
                    email = normalizedEmail,
                    displayName = displayName.ifBlank { "Zyne User" },
                    photoUrl = photoUrl,
                    deviceId = deviceId,
                    registeredAt = System.currentTimeMillis(),
                    referralCode = newUserId,
                    referredByCode = referredBy,
                    availableBalance = 0.0,
                    pendingBalance = 0.0,
                    lifetimeEarnings = 0.0,
                    totalWithdrawals = 0.0,
                    isAdmin = isAuthorizedAdmin
                )
                userDao.insertOrUpdateUser(newUser)
                firebaseManager.saveUser(newUser)

                if (referredBy.isNotEmpty()) {
                    val refId = "REF_${referredBy}_${newUserId}"
                    val refEntity = ReferralEntity(
                        id = refId,
                        referrerUserId = referredBy,
                        friendUserId = newUserId,
                        friendDisplayName = newUser.displayName,
                        referralCode = referredBy,
                        offerId = "",
                        bonusAmount = 10.0,
                        status = "JOINED_PENDING",
                        isQualified = false,
                        dateReferred = System.currentTimeMillis(),
                        completedAt = 0L
                    )
                    referralDao.insertOrUpdateReferral(refEntity)
                    firebaseManager.saveReferral(refEntity)
                }

                // Welcome notification
                val welcomeNotif = NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = newUserId,
                    title = "Welcome to Zyne Offers!",
                    message = "Your User ID is $newUserId. Complete brand offers to earn real cash rewards withdrawable via UPI!",
                    type = "WELCOME"
                )
                notificationDao.insertNotification(welcomeNotif)
                firebaseManager.saveNotification(welcomeNotif)

                newUser
            }

            _currentUserId.value = userToReturn.userId
            prefs.edit().putString("logged_in_user_id", userToReturn.userId).apply()
            attachUserListeners(userToReturn.userId)
            Result.success(userToReturn)
        }
    }

    private suspend fun generateUniqueUserId(): String {
        var id: String
        do {
            val randomDigits = Random.nextInt(100000, 999999)
            id = "ZY$randomDigits"
        } while (userDao.getUserByIdDirect(id) != null || firebaseManager.fetchUserById(id) != null)
        return id
    }

    fun logout() {
        firebaseManager.signOut()
        prefs.edit().remove("logged_in_user_id").apply()
        _currentUserId.value = null
    }

    // --- READ FLOWS ---
    fun getCurrentUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)

    val activeOffersFlow: Flow<List<OfferEntity>> = offerDao.getActiveOffers()
    val allOffersFlow: Flow<List<OfferEntity>> = offerDao.getAllOffers()
    fun getOfferFlow(offerId: String): Flow<OfferEntity?> = offerDao.getOfferById(offerId)

    val activeBannersFlow: Flow<List<BannerEntity>> = bannerDao.getActiveBanners()
    val allBannersFlow: Flow<List<BannerEntity>> = bannerDao.getAllBanners()

    val activeAnnouncementFlow: Flow<AnnouncementEntity?> = announcementDao.getActiveAnnouncement()
    val allAnnouncementsFlow: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()

    val settingsFlow: Flow<SettingsEntity?> = settingsDao.getSettings()

    fun getUserTransactionsFlow(userId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByUserId(userId)
    fun getUserWithdrawalsFlow(userId: String): Flow<List<WithdrawalEntity>> = withdrawalDao.getWithdrawalsByUserId(userId)
    fun getUserNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsByUserId(userId)
    fun getUnreadCountFlow(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)
    fun getUserReferralsFlow(userId: String): Flow<List<ReferralEntity>> = referralDao.getReferralsByReferrer(userId)

    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allWithdrawalsFlow: Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    // --- USER ACTIONS ---
    suspend fun markAllNotificationsRead(userId: String) {
        withContext(Dispatchers.IO) {
            notificationDao.markAllAsRead(userId)
        }
    }

    /**
     * Submit withdrawal request using atomic Firestore transaction
     */
    suspend fun requestWithdrawal(userId: String, amount: Double, upiId: String): Result<WithdrawalEntity> {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByIdDirect(userId) ?: firebaseManager.fetchUserById(userId)
                ?: return@withContext Result.failure(Exception("User not found"))

            val settings = settingsDao.getSettingsDirect() ?: SettingsEntity()
            if (amount < settings.minWithdrawalAmount) {
                return@withContext Result.failure(Exception("Minimum withdrawal amount is ₹${settings.minWithdrawalAmount.toInt()}"))
            }

            if (amount > user.availableBalance) {
                return@withContext Result.failure(Exception("Amount exceeds available balance ₹${user.availableBalance.toInt()}"))
            }

            val cleanedUpi = upiId.trim()
            if (!cleanedUpi.contains("@") || cleanedUpi.length < 5) {
                return@withContext Result.failure(Exception("Invalid UPI ID format. Example: user@upi or mobile@paytm"))
            }

            val pending = withdrawalDao.getPendingWithdrawalByUserId(userId)
            if (pending != null) {
                return@withContext Result.failure(Exception("You already have a pending withdrawal request of ₹${pending.amount.toInt()}. Please wait until it is processed."))
            }

            // Call atomic server transaction
            val result = firebaseManager.requestWithdrawalAtomic(
                userId = userId,
                amount = amount,
                upiId = cleanedUpi,
                userName = user.displayName,
                minWithdrawalAmount = settings.minWithdrawalAmount
            )

            if (result.isSuccess) {
                val withdrawal = result.getOrThrow()
                withdrawalDao.insertOrUpdateWithdrawal(withdrawal)

                // Update local cached balance
                val updatedUser = user.copy(
                    availableBalance = user.availableBalance - amount,
                    pendingBalance = user.pendingBalance + amount
                )
                userDao.insertOrUpdateUser(updatedUser)

                val tx = TransactionEntity(
                    id = "TX_WD_${withdrawal.id}",
                    userId = userId,
                    type = "Withdrawal Requested",
                    amount = amount,
                    status = "Pending",
                    note = "UPI: $cleanedUpi"
                )
                transactionDao.insertTransaction(tx)
            }

            result
        }
    }

    // --- OPERATOR / ADMIN ACTIONS ---
    suspend fun recordOfferClick(
        userId: String,
        offerId: String,
        offerName: String,
        affiliateUrl: String,
        reward: Double
    ): OfferActivityEntity {
        return withContext(Dispatchers.IO) {
            val trackingId = "TRK_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
            val activity = OfferActivityEntity(
                id = trackingId,
                userId = userId,
                offerId = offerId,
                offerName = offerName,
                trackingId = trackingId,
                affiliateUrl = affiliateUrl,
                reward = reward,
                status = "Started",
                startedAt = System.currentTimeMillis()
            )
            offerActivityDao.insertOrUpdateActivity(activity)
            firebaseManager.saveOfferActivity(activity)
            activity
        }
    }

    suspend fun adminCreditReward(
        userId: String,
        amount: Double,
        offerName: String,
        adminEmail: String,
        note: String,
        completionReference: String = "",
        offerId: String = ""
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val safeOfferId = if (offerId.isNotBlank()) offerId else offerName
            val safeCompRef = if (completionReference.isNotBlank()) completionReference else "COMP_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"

            // 1. Execute atomic Firestore transaction
            val fbResult = firebaseManager.adminCreditRewardAtomic(
                userId = userId,
                offerId = safeOfferId,
                offerName = offerName,
                rewardAmount = amount,
                completionReference = safeCompRef,
                adminEmail = adminEmail,
                note = note
            )

            if (fbResult.isFailure) {
                return@withContext Result.failure(fbResult.exceptionOrNull() ?: Exception("Failed to credit reward"))
            }

            // 2. Update local Room Cache
            val user = userDao.getUserByIdDirect(userId) ?: firebaseManager.fetchUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    availableBalance = user.availableBalance + amount,
                    lifetimeEarnings = user.lifetimeEarnings + amount
                )
                userDao.insertOrUpdateUser(updatedUser)

                val tx = TransactionEntity(
                    id = "TX_REW_$safeCompRef",
                    userId = userId,
                    type = "Reward Credited",
                    amount = amount,
                    status = "Approved",
                    note = "$offerName (Ref: $safeCompRef) - $note"
                )
                transactionDao.insertTransaction(tx)
            }

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "CREDIT_REWARD",
                details = "Credited ₹$amount to user $userId for offer '$offerName' [Ref: $safeCompRef]. Note: $note"
            )
            auditLogDao.insertAuditLog(auditLog)

            Result.success("Reward of ₹${amount.toInt()} credited successfully to $userId")
        }
    }

    suspend fun adminRejectReward(userId: String, offerName: String, reason: String, adminEmail: String) {
        withContext(Dispatchers.IO) {
            val tx = TransactionEntity(
                id = "TX_${System.currentTimeMillis()}",
                userId = userId,
                type = "Reward Rejected",
                amount = 0.0,
                status = "Rejected",
                note = "Offer: $offerName. Reason: $reason"
            )
            transactionDao.insertTransaction(tx)
            firebaseManager.saveTransaction(tx)

            val notif = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Reward Verification Failed",
                message = "Your submission for '$offerName' was rejected. Reason: $reason",
                type = "REWARD"
            )
            notificationDao.insertNotification(notif)
            firebaseManager.saveNotification(notif)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "REJECT_REWARD",
                details = "Rejected reward for user $userId ($offerName). Reason: $reason"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminApproveWithdrawal(withdrawalId: String, adminEmail: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = firebaseManager.adminApproveWithdrawalAtomic(withdrawalId, adminEmail)
            if (result.isSuccess) {
                val allWd = withdrawalDao.getAllWithdrawals().firstOrNull() ?: emptyList()
                val withdrawal = allWd.find { it.id == withdrawalId }
                if (withdrawal != null) {
                    val updated = withdrawal.copy(
                        status = "Approved",
                        approvedAt = System.currentTimeMillis()
                    )
                    withdrawalDao.insertOrUpdateWithdrawal(updated)
                }
            }
            result
        }
    }

    suspend fun adminMarkWithdrawalPaid(withdrawalId: String, adminEmail: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = firebaseManager.adminMarkWithdrawalPaidAtomic(withdrawalId, adminEmail)
            if (result.isSuccess) {
                val allWd = withdrawalDao.getAllWithdrawals().firstOrNull() ?: emptyList()
                val withdrawal = allWd.find { it.id == withdrawalId }
                if (withdrawal != null && withdrawal.status != "Paid") {
                    val updatedWd = withdrawal.copy(
                        status = "Paid",
                        paidAt = System.currentTimeMillis()
                    )
                    withdrawalDao.insertOrUpdateWithdrawal(updatedWd)

                    val user = userDao.getUserByIdDirect(withdrawal.userId)
                    if (user != null) {
                        val updatedUser = user.copy(
                            pendingBalance = kotlin.math.max(0.0, user.pendingBalance - withdrawal.amount),
                            totalWithdrawals = user.totalWithdrawals + withdrawal.amount
                        )
                        userDao.insertOrUpdateUser(updatedUser)
                    }

                    val tx = TransactionEntity(
                        id = "TX_PAID_$withdrawalId",
                        userId = withdrawal.userId,
                        type = "Withdrawal Paid",
                        amount = withdrawal.amount,
                        status = "Paid",
                        note = "Paid out to UPI: ${withdrawal.upiId}"
                    )
                    transactionDao.insertTransaction(tx)
                }
            }
            result
        }
    }

    suspend fun adminRejectWithdrawal(withdrawalId: String, reason: String, adminEmail: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = firebaseManager.adminRejectWithdrawalAtomic(withdrawalId, reason, adminEmail)
            if (result.isSuccess) {
                val allWd = withdrawalDao.getAllWithdrawals().firstOrNull() ?: emptyList()
                val withdrawal = allWd.find { it.id == withdrawalId }
                if (withdrawal != null && withdrawal.status != "Rejected") {
                    val updatedWd = withdrawal.copy(
                        status = "Rejected",
                        rejectionReason = reason
                    )
                    withdrawalDao.insertOrUpdateWithdrawal(updatedWd)

                    val user = userDao.getUserByIdDirect(withdrawal.userId)
                    if (user != null) {
                        val updatedUser = user.copy(
                            availableBalance = user.availableBalance + withdrawal.amount,
                            pendingBalance = kotlin.math.max(0.0, user.pendingBalance - withdrawal.amount)
                        )
                        userDao.insertOrUpdateUser(updatedUser)
                    }

                    val tx = TransactionEntity(
                        id = "TX_REFUND_$withdrawalId",
                        userId = withdrawal.userId,
                        type = "Withdrawal Refund",
                        amount = withdrawal.amount,
                        status = "Approved",
                        note = "Refund for rejected withdrawal: $reason"
                    )
                    transactionDao.insertTransaction(tx)
                }
            }
            result
        }
    }

    suspend fun adminToggleUserBan(userId: String, adminEmail: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val result = firebaseManager.toggleUserBanAtomic(userId, adminEmail)
            if (result.isSuccess) {
                val newBanState = result.getOrThrow()
                val user = userDao.getUserByIdDirect(userId)
                if (user != null) {
                    val updated = user.copy(isBanned = newBanState)
                    userDao.insertOrUpdateUser(updated)
                }
            }
            result
        }
    }

    suspend fun adminResetDeviceBinding(userId: String, adminEmail: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = firebaseManager.resetDeviceBindingAtomic(userId, adminEmail)
            if (result.isSuccess) {
                val user = userDao.getUserByIdDirect(userId)
                if (user != null) {
                    val updated = user.copy(deviceId = "RESET_${System.currentTimeMillis()}")
                    userDao.insertOrUpdateUser(updated)
                }
            }
            result
        }
    }

    // --- ADMIN CRUD FOR OFFERS, BANNERS, ANNOUNCEMENTS, SETTINGS ---
    suspend fun adminSaveOffer(offer: OfferEntity, adminEmail: String) {
        withContext(Dispatchers.IO) {
            offerDao.insertOrUpdateOffer(offer)
            firebaseManager.saveOffer(offer)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "SAVE_OFFER",
                details = "Created/Updated offer: ${offer.name} (${offer.id})"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminDeleteOffer(offerId: String, adminEmail: String) {
        withContext(Dispatchers.IO) {
            offerDao.deleteOffer(offerId)
            firebaseManager.deleteOffer(offerId)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "DELETE_OFFER",
                details = "Deleted offer: $offerId"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminSaveBanner(banner: BannerEntity, adminEmail: String) {
        withContext(Dispatchers.IO) {
            bannerDao.insertOrUpdateBanner(banner)
            firebaseManager.saveBanner(banner)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "SAVE_BANNER",
                details = "Saved banner: ${banner.title} (${banner.id})"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminDeleteBanner(bannerId: String, adminEmail: String) {
        withContext(Dispatchers.IO) {
            bannerDao.deleteBanner(bannerId)
            firebaseManager.deleteBanner(bannerId)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "DELETE_BANNER",
                details = "Deleted banner: $bannerId"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminSaveAnnouncement(announcement: AnnouncementEntity, adminEmail: String) {
        withContext(Dispatchers.IO) {
            announcementDao.insertOrUpdateAnnouncement(announcement)
            firebaseManager.saveAnnouncement(announcement)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "SAVE_ANNOUNCEMENT",
                details = "Saved announcement: ${announcement.content.take(40)}..."
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminDeleteAnnouncement(announcementId: String, adminEmail: String) {
        withContext(Dispatchers.IO) {
            announcementDao.deleteAnnouncement(announcementId)
            firebaseManager.deleteAnnouncement(announcementId)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "DELETE_ANNOUNCEMENT",
                details = "Deleted announcement: $announcementId"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }

    suspend fun adminUpdateSettings(settings: SettingsEntity, adminEmail: String) {
        withContext(Dispatchers.IO) {
            settingsDao.updateSettings(settings)
            firebaseManager.saveSettings(settings)

            val auditLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = adminEmail,
                action = "UPDATE_SETTINGS",
                details = "Updated platform settings (Min WD: ₹${settings.minWithdrawalAmount}, Referral: ₹${settings.referralBonusAmount})"
            )
            auditLogDao.insertAuditLog(auditLog)
            firebaseManager.saveAuditLog(auditLog)
        }
    }
}
