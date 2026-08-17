package com.example.data.firebase

import android.util.Log
import com.example.data.local.AnnouncementEntity
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseManager {

    companion object {
        private const val TAG = "FirebaseManager"
        const val PROJECT_ID = "zyne-6a559"
        const val STORAGE_BUCKET = "zyne-6a559.firebasestorage.app"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_OFFERS = "offers"
        const val COLLECTION_WITHDRAWALS = "withdrawals"
        const val COLLECTION_TRANSACTIONS = "transactions"
        const val COLLECTION_NOTIFICATIONS = "notifications"
        const val COLLECTION_BANNERS = "banners"
        const val COLLECTION_ANNOUNCEMENTS = "announcements"
        const val COLLECTION_SETTINGS = "settings"
        const val COLLECTION_REFERRALS = "referrals"
        const val COLLECTION_AUDIT_LOGS = "audit_logs"
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization error: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth initialization error: ${e.message}")
            null
        }
    }

    private val storage: FirebaseStorage? by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseStorage initialization error: ${e.message}")
            null
        }
    }

    fun isFirebaseAvailable(): Boolean {
        return firestore != null
    }

    fun getFirebaseAuth(): FirebaseAuth? = auth
    fun getFirebaseFirestore(): FirebaseFirestore? = firestore
    fun getFirebaseStorage(): FirebaseStorage? = storage

    fun getCurrentFirebaseUserUid(): String? = auth?.currentUser?.uid
    fun getCurrentFirebaseUserEmail(): String? = auth?.currentUser?.email

    suspend fun signInWithGoogleCredential(idToken: String): Result<String> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(Exception("FirebaseAuth not initialized"))
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = a.signInWithCredential(credential).await()
            val uid = authResult.user?.uid ?: ""
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Error in Firebase Auth signInWithGoogle: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error signing out of Firebase Auth: ${e.message}")
        }
    }

    suspend fun uploadFile(bytes: ByteArray, path: String): Result<String> = withContext(Dispatchers.IO) {
        val st = storage ?: return@withContext Result.failure(Exception("Firebase Storage not available"))
        try {
            val storageRef = st.reference.child(path)
            storageRef.putBytes(bytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file to Firebase Storage: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Set up real-time Firestore listeners to synchronize Cloud Database with Local Room Cache
     */
    fun startRealtimeSync(
        scope: CoroutineScope,
        onOffersUpdated: suspend (List<OfferEntity>) -> Unit,
        onBannersUpdated: suspend (List<BannerEntity>) -> Unit,
        onAnnouncementsUpdated: suspend (List<AnnouncementEntity>) -> Unit,
        onSettingsUpdated: suspend (SettingsEntity) -> Unit,
        onWithdrawalsUpdated: (suspend (List<WithdrawalEntity>) -> Unit)? = null
    ) {
        val db = firestore ?: return

        // 1. Listen for Offers collection updates
        try {
            db.collection(COLLECTION_OFFERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Offers snapshot listener failed", error)
                        return@addSnapshotListener
                    }
                    val offers = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            OfferEntity(
                                id = doc.getString("id") ?: doc.id,
                                name = doc.getString("name") ?: "",
                                rewardAmount = doc.getDouble("rewardAmount") ?: 0.0,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                category = doc.getString("category") ?: "Apps",
                                estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 5,
                                badge = doc.getString("badge") ?: "",
                                description = doc.getString("description") ?: "",
                                instructions = doc.getString("instructions") ?: "",
                                requirements = doc.getString("requirements") ?: "",
                                terms = doc.getString("terms") ?: "",
                                externalUrl = doc.getString("externalUrl") ?: "",
                                youtubeUrl = doc.getString("youtubeUrl") ?: "",
                                isPinned = doc.getBoolean("isPinned") ?: false,
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onOffersUpdated(offers)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting offers sync: ${e.message}")
        }

        // 2. Listen for Banners collection updates
        try {
            db.collection(COLLECTION_BANNERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Banners snapshot listener failed", error)
                        return@addSnapshotListener
                    }
                    val banners = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            BannerEntity(
                                id = doc.getString("id") ?: doc.id,
                                title = doc.getString("title") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                targetUrl = doc.getString("targetUrl") ?: "",
                                displayOrder = doc.getLong("displayOrder")?.toInt() ?: 0,
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onBannersUpdated(banners)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting banners sync: ${e.message}")
        }

        // 3. Listen for Announcements updates
        try {
            db.collection(COLLECTION_ANNOUNCEMENTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Announcements snapshot listener failed", error)
                        return@addSnapshotListener
                    }
                    val announcements = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            AnnouncementEntity(
                                id = doc.getString("id") ?: doc.id,
                                content = doc.getString("content") ?: "",
                                isActive = doc.getBoolean("isActive") ?: true,
                                publishedAt = doc.getLong("publishedAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onAnnouncementsUpdated(announcements)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting announcements sync: ${e.message}")
        }

        // 4. Listen for Platform Settings updates
        try {
            db.collection(COLLECTION_SETTINGS).document("global_config")
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.w(TAG, "Settings snapshot listener failed", error)
                        return@addSnapshotListener
                    }
                    if (doc != null && doc.exists()) {
                        try {
                            val settings = SettingsEntity(
                                id = 1,
                                trackingUrl = doc.getString("trackingUrl") ?: "https://tracking.zyneoffers.com",
                                minWithdrawalAmount = doc.getDouble("minWithdrawalAmount") ?: 50.0,
                                referralBonusAmount = doc.getDouble("referralBonusAmount") ?: 10.0,
                                isMaintenanceMode = doc.getBoolean("isMaintenanceMode") ?: false,
                                telegramUrl = doc.getString("telegramUrl") ?: "https://t.me/zyneoffers_community",
                                supportEmail = doc.getString("supportEmail") ?: "support@zyneoffers.com",
                                supportPhone = doc.getString("supportPhone") ?: "+91 98765 43210",
                                privacyPolicyUrl = doc.getString("privacyPolicyUrl") ?: "https://zyneoffers.com/privacy",
                                termsOfServiceUrl = doc.getString("termsOfServiceUrl") ?: "https://zyneoffers.com/terms",
                                aboutText = doc.getString("aboutText") ?: "Zyne Offers is a leading brand rewards platform helping users earn real cash by completing brand tasks and offers with instant UPI payouts.",
                                helpFaqUrl = doc.getString("helpFaqUrl") ?: "https://zyneoffers.com/help"
                            )
                            scope.launch(Dispatchers.IO) {
                                onSettingsUpdated(settings)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing settings document", e)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting settings sync: ${e.message}")
        }

        // 5. Listen for Withdrawals updates (for real-time status changes)
        if (onWithdrawalsUpdated != null) {
            try {
                db.collection(COLLECTION_WITHDRAWALS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Withdrawals snapshot listener failed", error)
                            return@addSnapshotListener
                        }
                        val withdrawals = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                WithdrawalEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    userName = doc.getString("userName") ?: "",
                                    amount = doc.getDouble("amount") ?: 0.0,
                                    upiId = doc.getString("upiId") ?: "",
                                    status = doc.getString("status") ?: "Pending",
                                    rejectionReason = doc.getString("rejectionReason") ?: "",
                                    requestedAt = doc.getLong("requestedAt") ?: System.currentTimeMillis(),
                                    approvedAt = doc.getLong("approvedAt") ?: 0L,
                                    paidAt = doc.getLong("paidAt") ?: 0L
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()
                        scope.launch(Dispatchers.IO) {
                            onWithdrawalsUpdated(withdrawals)
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error starting withdrawals sync: ${e.message}")
            }
        }
    }

    /**
     * Real-time listener for current user's profile
     */
    fun listenToUser(
        userId: String,
        scope: CoroutineScope,
        onUserUpdated: suspend (UserEntity?) -> Unit
    ) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_USERS).document(userId)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.w(TAG, "User snapshot listener failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (doc != null && doc.exists()) {
                        val user = try {
                            val uid = doc.getString("uid") ?: doc.getString("userId") ?: doc.getString("zyneUserId") ?: doc.id
                            val email = doc.getString("email") ?: ""
                            val name = doc.getString("displayName") ?: "Zyne User"
                            val photo = doc.getString("photoUrl") ?: doc.getString("photoURL") ?: ""
                            val devId = doc.getString("deviceId") ?: ""
                            val regAt = doc.getLong("registeredAt") ?: doc.getLong("joinedAt") ?: System.currentTimeMillis()
                            val refCode = doc.getString("referralCode") ?: doc.getString("zyneUserId") ?: uid
                            val refBy = doc.getString("referredByCode") ?: doc.getString("referredBy") ?: ""
                            val availBal = doc.getDouble("availableBalance") ?: doc.getDouble("walletBalance") ?: 0.0
                            val pendBal = doc.getDouble("pendingBalance") ?: 0.0
                            val lifetime = doc.getDouble("lifetimeEarnings") ?: 0.0
                            val totalWith = doc.getDouble("totalWithdrawals") ?: doc.getDouble("paidOut") ?: 0.0
                            val isBanned = (doc.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (doc.getBoolean("isBanned") ?: false)
                            val isAdmin = doc.getBoolean("isAdmin") ?: email.trim().equals("buddepubasu123@gmail.com", ignoreCase = true)

                            UserEntity(
                                userId = uid,
                                email = email,
                                displayName = name,
                                photoUrl = photo,
                                deviceId = devId,
                                registeredAt = regAt,
                                referralCode = refCode,
                                referredByCode = refBy,
                                availableBalance = availBal,
                                pendingBalance = pendBal,
                                lifetimeEarnings = lifetime,
                                totalWithdrawals = totalWith,
                                isBanned = isBanned,
                                isAdmin = isAdmin
                            )
                        } catch (e: Exception) {
                            null
                        }
                        scope.launch(Dispatchers.IO) {
                            onUserUpdated(user)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to user: ${e.message}")
        }
    }

    /**
     * Real-time listener for current user's transactions
     */
    fun listenToUserTransactions(
        userId: String,
        scope: CoroutineScope,
        onTransactionsUpdated: suspend (List<TransactionEntity>) -> Unit
    ) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Transactions snapshot listener failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    val txList = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            TransactionEntity(
                                id = doc.getString("id") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                type = doc.getString("type") ?: "Transaction",
                                amount = doc.getDouble("amount") ?: 0.0,
                                status = doc.getString("status") ?: "Approved",
                                note = doc.getString("note") ?: "",
                                dateTimestamp = doc.getLong("dateTimestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onTransactionsUpdated(txList)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to transactions: ${e.message}")
        }
    }

    /**
     * Real-time listener for current user's notifications
     */
    fun listenToUserNotifications(
        userId: String,
        scope: CoroutineScope,
        onNotificationsUpdated: suspend (List<NotificationEntity>) -> Unit
    ) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Notifications snapshot listener failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    val notifList = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            NotificationEntity(
                                id = doc.getString("id") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                dateTimestamp = doc.getLong("dateTimestamp") ?: System.currentTimeMillis(),
                                isRead = doc.getBoolean("isRead") ?: false,
                                type = doc.getString("type") ?: "GENERAL"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onNotificationsUpdated(notifList)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to notifications: ${e.message}")
        }
    }

    /**
     * Real-time listener for current user's referrals (where user is the referrer)
     */
    fun listenToUserReferrals(
        referrerUserId: String,
        scope: CoroutineScope,
        onReferralsUpdated: suspend (List<ReferralEntity>) -> Unit
    ) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_REFERRALS)
                .whereEqualTo("referrerUserId", referrerUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Referrals snapshot listener failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    val refList = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ReferralEntity(
                                id = doc.getString("id") ?: doc.id,
                                referrerUserId = doc.getString("referrerUserId") ?: "",
                                friendUserId = doc.getString("friendUserId") ?: "",
                                friendDisplayName = doc.getString("friendDisplayName") ?: "Invited User",
                                referralCode = doc.getString("referralCode") ?: doc.getString("referrerUserId") ?: "",
                                offerId = doc.getString("offerId") ?: "",
                                bonusAmount = doc.getDouble("bonusAmount") ?: 10.0,
                                status = doc.getString("status") ?: if (doc.getBoolean("isQualified") == true) "COMPLETED_REWARDED" else "JOINED_PENDING",
                                isQualified = doc.getBoolean("isQualified") ?: false,
                                dateReferred = doc.getLong("dateReferred") ?: System.currentTimeMillis(),
                                completedAt = doc.getLong("completedAt") ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onReferralsUpdated(refList)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to user referrals: ${e.message}")
        }
    }

    /**
     * Real-time listener for all referrals (Admin console)
     */
    fun listenToAllReferrals(
        scope: CoroutineScope,
        onReferralsUpdated: suspend (List<ReferralEntity>) -> Unit
    ) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_REFERRALS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "All referrals snapshot listener failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    val refList = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ReferralEntity(
                                id = doc.getString("id") ?: doc.id,
                                referrerUserId = doc.getString("referrerUserId") ?: "",
                                friendUserId = doc.getString("friendUserId") ?: "",
                                friendDisplayName = doc.getString("friendDisplayName") ?: "Invited User",
                                referralCode = doc.getString("referralCode") ?: doc.getString("referrerUserId") ?: "",
                                offerId = doc.getString("offerId") ?: "",
                                bonusAmount = doc.getDouble("bonusAmount") ?: 10.0,
                                status = doc.getString("status") ?: if (doc.getBoolean("isQualified") == true) "COMPLETED_REWARDED" else "JOINED_PENDING",
                                isQualified = doc.getBoolean("isQualified") ?: false,
                                dateReferred = doc.getLong("dateReferred") ?: System.currentTimeMillis(),
                                completedAt = doc.getLong("completedAt") ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    scope.launch(Dispatchers.IO) {
                        onReferralsUpdated(refList)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to all referrals: ${e.message}")
        }
    }

    suspend fun fetchUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val doc = db.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                val uid = doc.getString("uid") ?: doc.getString("userId") ?: doc.getString("zyneUserId") ?: doc.id
                val email = doc.getString("email") ?: ""
                val name = doc.getString("displayName") ?: "Zyne User"
                val photo = doc.getString("photoUrl") ?: doc.getString("photoURL") ?: ""
                val devId = doc.getString("deviceId") ?: ""
                val regAt = doc.getLong("registeredAt") ?: doc.getLong("joinedAt") ?: System.currentTimeMillis()
                val refCode = doc.getString("referralCode") ?: doc.getString("zyneUserId") ?: uid
                val refBy = doc.getString("referredByCode") ?: doc.getString("referredBy") ?: ""
                val availBal = doc.getDouble("availableBalance") ?: doc.getDouble("walletBalance") ?: 0.0
                val pendBal = doc.getDouble("pendingBalance") ?: 0.0
                val lifetime = doc.getDouble("lifetimeEarnings") ?: 0.0
                val totalWith = doc.getDouble("totalWithdrawals") ?: doc.getDouble("paidOut") ?: 0.0
                val isBanned = (doc.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (doc.getBoolean("isBanned") ?: false)
                val isAdmin = doc.getBoolean("isAdmin") ?: email.trim().equals("buddepubasu123@gmail.com", ignoreCase = true)

                UserEntity(
                    userId = uid,
                    email = email,
                    displayName = name,
                    photoUrl = photo,
                    deviceId = devId,
                    registeredAt = regAt,
                    referralCode = refCode,
                    referredByCode = refBy,
                    availableBalance = availBal,
                    pendingBalance = pendBal,
                    lifetimeEarnings = lifetime,
                    totalWithdrawals = totalWith,
                    isBanned = isBanned,
                    isAdmin = isAdmin
                )
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user $userId from Firestore: ${e.message}")
            null
        }
    }

    suspend fun fetchUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val snapshot = db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                val uid = doc.getString("uid") ?: doc.getString("userId") ?: doc.getString("zyneUserId") ?: doc.id
                val userEmail = doc.getString("email") ?: email
                val name = doc.getString("displayName") ?: "Zyne User"
                val photo = doc.getString("photoUrl") ?: doc.getString("photoURL") ?: ""
                val devId = doc.getString("deviceId") ?: ""
                val regAt = doc.getLong("registeredAt") ?: doc.getLong("joinedAt") ?: System.currentTimeMillis()
                val refCode = doc.getString("referralCode") ?: doc.getString("zyneUserId") ?: uid
                val refBy = doc.getString("referredByCode") ?: doc.getString("referredBy") ?: ""
                val availBal = doc.getDouble("availableBalance") ?: doc.getDouble("walletBalance") ?: 0.0
                val pendBal = doc.getDouble("pendingBalance") ?: 0.0
                val lifetime = doc.getDouble("lifetimeEarnings") ?: 0.0
                val totalWith = doc.getDouble("totalWithdrawals") ?: doc.getDouble("paidOut") ?: 0.0
                val isBanned = (doc.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (doc.getBoolean("isBanned") ?: false)
                val isAdmin = doc.getBoolean("isAdmin") ?: userEmail.trim().equals("buddepubasu123@gmail.com", ignoreCase = true)

                UserEntity(
                    userId = uid,
                    email = userEmail,
                    displayName = name,
                    photoUrl = photo,
                    deviceId = devId,
                    registeredAt = regAt,
                    referralCode = refCode,
                    referredByCode = refBy,
                    availableBalance = availBal,
                    pendingBalance = pendBal,
                    lifetimeEarnings = lifetime,
                    totalWithdrawals = totalWith,
                    isBanned = isBanned,
                    isAdmin = isAdmin
                )
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user by email from Firestore: ${e.message}")
            null
        }
    }

    suspend fun fetchAllOffers(): List<OfferEntity> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        try {
            val snapshot = db.collection(COLLECTION_OFFERS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    OfferEntity(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        rewardAmount = doc.getDouble("rewardAmount") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        category = doc.getString("category") ?: "Apps",
                        estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 5,
                        badge = doc.getString("badge") ?: "",
                        description = doc.getString("description") ?: "",
                        instructions = doc.getString("instructions") ?: "",
                        requirements = doc.getString("requirements") ?: "",
                        terms = doc.getString("terms") ?: "",
                        externalUrl = doc.getString("externalUrl") ?: "",
                        youtubeUrl = doc.getString("youtubeUrl") ?: "",
                        isPinned = doc.getBoolean("isPinned") ?: false,
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching offers from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        try {
            val snapshot = db.collection(COLLECTION_USERS).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val uid = doc.getString("uid") ?: doc.getString("userId") ?: doc.getString("zyneUserId") ?: doc.id
                    val email = doc.getString("email") ?: ""
                    val name = doc.getString("displayName") ?: "Zyne User"
                    val photo = doc.getString("photoUrl") ?: doc.getString("photoURL") ?: ""
                    val devId = doc.getString("deviceId") ?: ""
                    val regAt = doc.getLong("registeredAt") ?: doc.getLong("joinedAt") ?: System.currentTimeMillis()
                    val refCode = doc.getString("referralCode") ?: doc.getString("zyneUserId") ?: uid
                    val refBy = doc.getString("referredByCode") ?: doc.getString("referredBy") ?: ""
                    val availBal = doc.getDouble("availableBalance") ?: doc.getDouble("walletBalance") ?: 0.0
                    val pendBal = doc.getDouble("pendingBalance") ?: 0.0
                    val lifetime = doc.getDouble("lifetimeEarnings") ?: 0.0
                    val totalWith = doc.getDouble("totalWithdrawals") ?: doc.getDouble("paidOut") ?: 0.0
                    val isBanned = (doc.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (doc.getBoolean("isBanned") ?: false)
                    val isAdmin = doc.getBoolean("isAdmin") ?: email.trim().equals("buddepubasu123@gmail.com", ignoreCase = true)

                    UserEntity(
                        userId = uid,
                        email = email,
                        displayName = name,
                        photoUrl = photo,
                        deviceId = devId,
                        registeredAt = regAt,
                        referralCode = refCode,
                        referredByCode = refBy,
                        availableBalance = availBal,
                        pendingBalance = pendBal,
                        lifetimeEarnings = lifetime,
                        totalWithdrawals = totalWith,
                        isBanned = isBanned,
                        isAdmin = isAdmin
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching users from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val userMap = hashMapOf(
                "uid" to user.userId,
                "userId" to user.userId,
                "zyneUserId" to user.referralCode,
                "email" to user.email.trim().lowercase(),
                "displayName" to user.displayName,
                "photoUrl" to user.photoUrl,
                "photoURL" to user.photoUrl,
                "deviceId" to user.deviceId,
                "registeredAt" to user.registeredAt,
                "joinedAt" to user.registeredAt,
                "referralCode" to user.referralCode,
                "referredBy" to user.referredByCode,
                "referredByCode" to user.referredByCode,
                "availableBalance" to user.availableBalance,
                "walletBalance" to user.availableBalance,
                "pendingBalance" to user.pendingBalance,
                "lifetimeEarnings" to user.lifetimeEarnings,
                "paidOut" to user.totalWithdrawals,
                "totalWithdrawals" to user.totalWithdrawals,
                "accountStatus" to if (user.isBanned) "banned" else "active",
                "isBanned" to user.isBanned,
                "isAdmin" to user.email.trim().equals("buddepubasu123@gmail.com", ignoreCase = true),
                "lastLoginAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS).document(user.userId)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveOffer(offer: OfferEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val offerMap = hashMapOf(
                "id" to offer.id,
                "name" to offer.name,
                "rewardAmount" to offer.rewardAmount,
                "imageUrl" to offer.imageUrl,
                "category" to offer.category,
                "estimatedMinutes" to offer.estimatedMinutes,
                "badge" to offer.badge,
                "description" to offer.description,
                "instructions" to offer.instructions,
                "requirements" to offer.requirements,
                "terms" to offer.terms,
                "externalUrl" to offer.externalUrl,
                "youtubeUrl" to offer.youtubeUrl,
                "isPinned" to offer.isPinned,
                "isActive" to offer.isActive,
                "createdAt" to offer.createdAt
            )
            db.collection(COLLECTION_OFFERS).document(offer.id)
                .set(offerMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving offer to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteOffer(offerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            db.collection(COLLECTION_OFFERS).document(offerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting offer from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveWithdrawal(withdrawal: WithdrawalEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val withdrawalMap = hashMapOf(
                "id" to withdrawal.id,
                "userId" to withdrawal.userId,
                "userName" to withdrawal.userName,
                "amount" to withdrawal.amount,
                "upiId" to withdrawal.upiId,
                "status" to withdrawal.status,
                "rejectionReason" to withdrawal.rejectionReason,
                "requestedAt" to withdrawal.requestedAt,
                "approvedAt" to withdrawal.approvedAt,
                "paidAt" to withdrawal.paidAt
            )
            db.collection(COLLECTION_WITHDRAWALS).document(withdrawal.id)
                .set(withdrawalMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving withdrawal to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveTransaction(transaction: TransactionEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val txMap = hashMapOf(
                "id" to transaction.id,
                "userId" to transaction.userId,
                "type" to transaction.type,
                "amount" to transaction.amount,
                "status" to transaction.status,
                "note" to transaction.note,
                "dateTimestamp" to transaction.dateTimestamp
            )
            db.collection(COLLECTION_TRANSACTIONS).document(transaction.id)
                .set(txMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveNotification(notification: NotificationEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val notifMap = hashMapOf(
                "id" to notification.id,
                "userId" to notification.userId,
                "title" to notification.title,
                "message" to notification.message,
                "dateTimestamp" to notification.dateTimestamp,
                "isRead" to notification.isRead,
                "type" to notification.type
            )
            db.collection(COLLECTION_NOTIFICATIONS).document(notification.id)
                .set(notifMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveSettings(settings: SettingsEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val settingsMap = hashMapOf(
                "trackingUrl" to settings.trackingUrl,
                "minWithdrawalAmount" to settings.minWithdrawalAmount,
                "referralBonusAmount" to settings.referralBonusAmount,
                "isMaintenanceMode" to settings.isMaintenanceMode,
                "telegramUrl" to settings.telegramUrl,
                "supportEmail" to settings.supportEmail,
                "supportPhone" to settings.supportPhone,
                "privacyPolicyUrl" to settings.privacyPolicyUrl,
                "termsOfServiceUrl" to settings.termsOfServiceUrl,
                "aboutText" to settings.aboutText,
                "helpFaqUrl" to settings.helpFaqUrl
            )
            db.collection(COLLECTION_SETTINGS).document("global_config")
                .set(settingsMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving settings to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveBanner(banner: BannerEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val bannerMap = hashMapOf(
                "id" to banner.id,
                "title" to banner.title,
                "imageUrl" to banner.imageUrl,
                "targetUrl" to banner.targetUrl,
                "displayOrder" to banner.displayOrder,
                "isActive" to banner.isActive
            )
            db.collection(COLLECTION_BANNERS).document(banner.id)
                .set(bannerMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving banner to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBanner(bannerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            db.collection(COLLECTION_BANNERS).document(bannerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting banner from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveAnnouncement(announcement: AnnouncementEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val announcementMap = hashMapOf(
                "id" to announcement.id,
                "content" to announcement.content,
                "isActive" to announcement.isActive,
                "publishedAt" to announcement.publishedAt
            )
            db.collection(COLLECTION_ANNOUNCEMENTS).document(announcement.id)
                .set(announcementMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving announcement to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            db.collection(COLLECTION_ANNOUNCEMENTS).document(announcementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting announcement from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveReferral(referral: ReferralEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val refMap = hashMapOf(
                "id" to referral.id,
                "referrerUserId" to referral.referrerUserId,
                "friendUserId" to referral.friendUserId,
                "friendDisplayName" to referral.friendDisplayName,
                "referralCode" to referral.referralCode,
                "offerId" to referral.offerId,
                "bonusAmount" to referral.bonusAmount,
                "status" to referral.status,
                "isQualified" to referral.isQualified,
                "dateReferred" to referral.dateReferred,
                "completedAt" to referral.completedAt
            )
            db.collection(COLLECTION_REFERRALS).document(referral.id)
                .set(refMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving referral to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atomically process referral reward in Firestore when a referred user (friend) completes their first eligible offer.
     * Rules enforced:
     * 1. Checks if friend was referred by a valid referrer (referrerUserId).
     * 2. Strict anti-duplicate rule: Checks if referral has ALREADY been rewarded (isQualified == true or status == "COMPLETED_REWARDED").
     *    If already rewarded, ABORTS with no duplicate reward.
     * 3. Only on confirmed successful first offer completion:
     *    - Updates referral document to isQualified = true, status = "COMPLETED_REWARDED", offerId = offerId, completedAt = now.
     *    - Atomically increments referrer's availableBalance by ₹10.0 and lifetimeEarnings by ₹10.0.
     *    - Writes referral transaction record to collection "transactions" for referrer.
     *    - Writes notification to collection "notifications" for referrer.
     */
    suspend fun processReferralRewardOnOfferCompletion(
        friendUserId: String,
        offerId: String,
        offerName: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val referralQuery = db.collection(COLLECTION_REFERRALS)
                .whereEqualTo("friendUserId", friendUserId)
                .limit(1)
                .get()
                .await()

            if (referralQuery.isEmpty) {
                // Friend was not referred by anyone, no referral reward needed
                return@withContext Result.success(false)
            }

            val referralDoc = referralQuery.documents.first()
            val referralRef = referralDoc.reference
            val referrerUserId = referralDoc.getString("referrerUserId") ?: return@withContext Result.success(false)

            if (referrerUserId.isBlank() || referrerUserId == friendUserId) {
                return@withContext Result.success(false)
            }

            val referrerUserRef = db.collection(COLLECTION_USERS).document(referrerUserId)
            val timestamp = System.currentTimeMillis()
            val txId = "TX_REF_${timestamp}_${friendUserId}"
            val notifId = UUID.randomUUID().toString()
            val rewardAmount = 10.0

            var rewardCredited = false

            db.runTransaction { transaction ->
                val freshRefSnap = transaction.get(referralRef)
                val alreadyQualified = freshRefSnap.getBoolean("isQualified") ?: false
                val currentStatus = freshRefSnap.getString("status") ?: ""

                // Strict anti-duplicate check: User A receives ₹10 only ONCE per referred user.
                // Repeated offer completions by User B must NEVER generate another referral reward.
                if (alreadyQualified || currentStatus == "COMPLETED_REWARDED") {
                    rewardCredited = false
                    return@runTransaction
                }

                val referrerSnap = transaction.get(referrerUserRef)
                if (!referrerSnap.exists()) {
                    rewardCredited = false
                    return@runTransaction
                }

                val currentBalance = referrerSnap.getDouble("availableBalance") ?: 0.0
                val currentLifetime = referrerSnap.getDouble("lifetimeEarnings") ?: 0.0
                val friendName = freshRefSnap.getString("friendDisplayName") ?: "Friend ($friendUserId)"
                val referralCode = freshRefSnap.getString("referralCode") ?: referrerUserId

                // 1. Update Referrer User Wallet atomically
                transaction.update(referrerUserRef, mapOf(
                    "availableBalance" to (currentBalance + rewardAmount),
                    "lifetimeEarnings" to (currentLifetime + rewardAmount)
                ))

                // 2. Update Referral record with full history fields
                transaction.update(referralRef, mapOf(
                    "isQualified" to true,
                    "status" to "COMPLETED_REWARDED",
                    "offerId" to offerId,
                    "bonusAmount" to rewardAmount,
                    "completedAt" to timestamp
                ))

                // 3. Create Transaction record for Referrer
                val txRef = db.collection(COLLECTION_TRANSACTIONS).document(txId)
                transaction.set(txRef, mapOf(
                    "id" to txId,
                    "userId" to referrerUserId,
                    "type" to "Referral Bonus",
                    "amount" to rewardAmount,
                    "status" to "Approved",
                    "note" to "Referral reward: ₹10 for friend $friendName ($friendUserId) completing first offer ($offerName)",
                    "dateTimestamp" to timestamp
                ))

                // 4. Create Notification for Referrer
                val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(notifId)
                transaction.set(notifRef, mapOf(
                    "id" to notifId,
                    "userId" to referrerUserId,
                    "title" to "Referral Bonus Credited! 🎁",
                    "message" to "₹10 credited to your wallet! Your referred friend $friendName completed their first offer ($offerName).",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "REFERRAL"
                ))

                rewardCredited = true
            }.await()

            Result.success(rewardCredited)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing referral reward transaction: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveOfferActivity(activity: OfferActivityEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val activityMap = hashMapOf(
                "id" to activity.id,
                "userId" to activity.userId,
                "offerId" to activity.offerId,
                "offerName" to activity.offerName,
                "trackingId" to activity.trackingId,
                "affiliateUrl" to activity.affiliateUrl,
                "reward" to activity.reward,
                "status" to activity.status,
                "startedAt" to activity.startedAt,
                "completedAt" to activity.completedAt,
                "creditedAt" to activity.creditedAt,
                "adminId" to activity.adminId,
                "completionReference" to activity.completionReference,
                "adminNote" to activity.adminNote
            )
            db.collection("offer_activities").document(activity.id)
                .set(activityMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving offer activity to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Executes an atomic Firestore transaction to credit an offer reward to a user.
     * 1. Validates user exists in Firestore.
     * 2. Checks completionReference / idempotency key to prevent double credits.
     * 3. Atomically increases user's availableBalance & lifetimeEarnings.
     * 4. Records completion record in `completions` collection.
     * 5. Records reward transaction in `transactions` collection.
     * 6. Records notification in `notifications` collection.
     * 7. If user was referred and this is their first qualifying completion:
     *    Atomically credits ₹10 to the referrer, creates referrer transaction, updates referral status, and logs notification.
     */
    suspend fun adminCreditRewardAtomic(
        userId: String,
        offerId: String,
        offerName: String,
        rewardAmount: Double,
        completionReference: String,
        adminEmail: String,
        note: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val safeRef = completionReference.ifBlank { "COMP_${System.currentTimeMillis()}_${(1000..9999).random()}" }
            val completionDocRef = db.collection("completions").document(safeRef)
            val userDocRef = db.collection(COLLECTION_USERS).document(userId)

            val timestamp = System.currentTimeMillis()
            val txId = "TX_REW_${timestamp}_${userId.takeLast(4)}"
            val notifId = UUID.randomUUID().toString()

            db.runTransaction { transaction ->
                // 1. Idempotency Check: Prevent duplicate credits
                val existingComp = transaction.get(completionDocRef)
                if (existingComp.exists()) {
                    val status = existingComp.getString("status") ?: ""
                    if (status.equals("Completed", ignoreCase = true) || status.equals("CREDITED", ignoreCase = true)) {
                        throw IllegalStateException("Reward with completion reference '$safeRef' has already been credited!")
                    }
                }

                val userSnap = transaction.get(userDocRef)
                if (!userSnap.exists()) {
                    throw IllegalArgumentException("User with ID '$userId' does not exist in Firestore!")
                }

                val currentBalance = userSnap.getDouble("availableBalance") ?: userSnap.getDouble("walletBalance") ?: 0.0
                val currentLifetime = userSnap.getDouble("lifetimeEarnings") ?: 0.0

                val newBalance = currentBalance + rewardAmount
                val newLifetime = currentLifetime + rewardAmount

                // 2. Update user wallet balance & lifetime earnings atomically
                transaction.update(userDocRef, mapOf(
                    "availableBalance" to newBalance,
                    "walletBalance" to newBalance,
                    "lifetimeEarnings" to newLifetime
                ))

                // 3. Set completion record
                transaction.set(completionDocRef, mapOf(
                    "id" to safeRef,
                    "userId" to userId,
                    "offerId" to offerId,
                    "offerName" to offerName,
                    "rewardAmount" to rewardAmount,
                    "status" to "Completed",
                    "completionReference" to safeRef,
                    "adminEmail" to adminEmail,
                    "adminNote" to note,
                    "creditedAt" to timestamp
                ), SetOptions.merge())

                // 4. Set transaction record for user
                val txRef = db.collection(COLLECTION_TRANSACTIONS).document(txId)
                transaction.set(txRef, mapOf(
                    "id" to txId,
                    "userId" to userId,
                    "type" to "Reward Credited",
                    "amount" to rewardAmount,
                    "status" to "Approved",
                    "note" to "$offerName (Ref: $safeRef) - $note",
                    "dateTimestamp" to timestamp
                ))

                // 5. Set notification for user
                val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(notifId)
                transaction.set(notifRef, mapOf(
                    "id" to notifId,
                    "userId" to userId,
                    "title" to "Reward Credited! 🎉",
                    "message" to "₹${rewardAmount.toInt()} has been credited to your wallet for completing $offerName.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "OFFER_REWARD"
                ))
            }.await()

            // Safe referral trigger check
            processReferralRewardOnOfferCompletion(userId, offerId, offerName)

            Result.success("Reward of ₹${rewardAmount.toInt()} successfully credited to $userId!")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing adminCreditRewardAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveAuditLog(auditLog: AuditLogEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val logMap = hashMapOf(
                "id" to auditLog.id,
                "adminEmail" to auditLog.adminEmail,
                "action" to auditLog.action,
                "details" to auditLog.details,
                "timestamp" to auditLog.timestamp
            )
            db.collection(COLLECTION_AUDIT_LOGS).document(auditLog.id)
                .set(logMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audit log to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
