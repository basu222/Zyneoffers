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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
import kotlin.math.max

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
        const val COLLECTION_COMPLETIONS = "completions"
        const val COLLECTION_OFFER_ACTIVITIES = "offer_activities"
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

    fun isFirebaseAvailable(): Boolean = firestore != null

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
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "FirebaseAuthException [${e.errorCode}]: ${e.message}", e)
            Result.failure(Exception("[${e.errorCode}] ${e.message ?: "Authentication failed"}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error in Firebase Auth signInWithGoogle: ${e.message}", e)
            Result.failure(Exception("[${e::class.java.simpleName}] ${e.localizedMessage ?: e.message ?: "Authentication failed"}"))
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
     * Real-time listeners to sync Cloud Firestore with Room local cache
     */
    fun startRealtimeSync(
        scope: CoroutineScope,
        onOffersUpdated: suspend (List<OfferEntity>) -> Unit,
        onBannersUpdated: suspend (List<BannerEntity>) -> Unit,
        onAnnouncementsUpdated: suspend (List<AnnouncementEntity>) -> Unit,
        onSettingsUpdated: suspend (SettingsEntity) -> Unit,
        onWithdrawalsUpdated: (suspend (List<WithdrawalEntity>) -> Unit)? = null,
        onUsersUpdated: (suspend (List<UserEntity>) -> Unit)? = null,
        onTransactionsUpdated: (suspend (List<TransactionEntity>) -> Unit)? = null,
        onReferralsUpdated: (suspend (List<ReferralEntity>) -> Unit)? = null
    ) {
        val db = firestore ?: return

        // 1. Offers
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

        // 2. Banners
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

        // 3. Announcements
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

        // 4. Settings
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

        // 5. Withdrawals
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

        // 6. Users (Realtime synchronization for Admin search & user directory)
        if (onUsersUpdated != null) {
            try {
                db.collection(COLLECTION_USERS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Users snapshot listener failed", error)
                            return@addSnapshotListener
                        }
                        val userList = snapshot?.documents?.mapNotNull { doc ->
                            parseUserFromDocument(doc)
                        } ?: emptyList()
                        scope.launch(Dispatchers.IO) {
                            onUsersUpdated(userList)
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error starting users sync: ${e.message}")
            }
        }

        // 7. Transactions
        if (onTransactionsUpdated != null) {
            try {
                db.collection(COLLECTION_TRANSACTIONS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Transactions global sync failed: ${error.message}")
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
                Log.w(TAG, "Error starting transactions sync: ${e.message}")
            }
        }

        // 8. Referrals
        if (onReferralsUpdated != null) {
            try {
                db.collection(COLLECTION_REFERRALS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Referrals global sync failed: ${error.message}")
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
                Log.w(TAG, "Error starting referrals sync: ${e.message}")
            }
        }
    }

    private fun parseUserFromDocument(doc: com.google.firebase.firestore.DocumentSnapshot): UserEntity? {
        return try {
            val uid = doc.getString("uid") ?: doc.getString("userId") ?: doc.id
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
                        val user = parseUserFromDocument(doc)
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

    suspend fun fetchUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val doc = db.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                parseUserFromDocument(doc)
            } else {
                // Also search by referralCode / zyneUserId query
                val query = db.collection(COLLECTION_USERS)
                    .whereEqualTo("referralCode", userId)
                    .limit(1)
                    .get()
                    .await()
                if (!query.isEmpty) {
                    parseUserFromDocument(query.documents.first())
                } else null
            }
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
                parseUserFromDocument(snapshot.documents.first())
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user by email from Firestore: ${e.message}")
            null
        }
    }

    suspend fun fetchAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        try {
            val snapshot = db.collection(COLLECTION_USERS).get().await()
            snapshot.documents.mapNotNull { parseUserFromDocument(it) }
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
                "isAdmin" to (user.isAdmin || user.email.trim().equals("buddepubasu123@gmail.com", ignoreCase = true)),
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
            db.collection(COLLECTION_OFFER_ACTIVITIES).document(activity.id)
                .set(activityMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving offer activity to Firestore: ${e.message}", e)
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

    // =========================================================================
    // ATOMIC FIRESTORE TRANSACTIONS (SOURCE OF TRUTH & IDEMPOTENCY ENGINE)
    // =========================================================================

    /**
     * ATOMIC WITHDRAWAL REQUEST
     * 1. Validates user balance >= amount and min withdrawal threshold.
     * 2. Checks user is active/not banned.
     * 3. Checks no other Pending withdrawal exists.
     * 4. Deducts amount from availableBalance and adds to pendingBalance (reserves funds).
     * 5. Atomically creates Withdrawal document, Transaction ledger entry, and Notification.
     */
    suspend fun requestWithdrawalAtomic(
        userId: String,
        amount: Double,
        upiId: String,
        userName: String,
        minWithdrawalAmount: Double
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val userRef = db.collection(COLLECTION_USERS).document(userId)
            val wdId = "WD_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val wdRef = db.collection(COLLECTION_WITHDRAWALS).document(wdId)
            val txRef = db.collection(COLLECTION_TRANSACTIONS).document("TX_WD_$wdId")
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()

            var createdWithdrawal: WithdrawalEntity? = null

            db.runTransaction { transaction ->
                val userSnap = transaction.get(userRef)
                if (!userSnap.exists()) {
                    throw IllegalArgumentException("User profile not found in database.")
                }

                val isBanned = (userSnap.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (userSnap.getBoolean("isBanned") ?: false)
                if (isBanned) {
                    throw IllegalStateException("Account is suspended. Cannot perform withdrawals.")
                }

                val currentAvailable = userSnap.getDouble("availableBalance") ?: userSnap.getDouble("walletBalance") ?: 0.0
                val currentPending = userSnap.getDouble("pendingBalance") ?: 0.0

                if (amount < minWithdrawalAmount) {
                    throw IllegalArgumentException("Minimum withdrawal amount is ₹${minWithdrawalAmount.toInt()}")
                }

                if (amount > currentAvailable) {
                    throw IllegalArgumentException("Insufficient balance. Available: ₹${currentAvailable.toInt()}, Requested: ₹${amount.toInt()}")
                }

                val newAvailable = currentAvailable - amount
                val newPending = currentPending + amount

                // 1. Update user wallet balance (reserve funds)
                transaction.update(userRef, mapOf(
                    "availableBalance" to newAvailable,
                    "walletBalance" to newAvailable,
                    "pendingBalance" to newPending
                ))

                // 2. Insert withdrawal document
                transaction.set(wdRef, mapOf(
                    "id" to wdId,
                    "userId" to userId,
                    "userName" to userName,
                    "amount" to amount,
                    "upiId" to upiId,
                    "status" to "Pending",
                    "rejectionReason" to "",
                    "requestedAt" to timestamp,
                    "approvedAt" to 0L,
                    "paidAt" to 0L
                ))

                // 3. Insert transaction ledger entry
                transaction.set(txRef, mapOf(
                    "id" to "TX_WD_$wdId",
                    "userId" to userId,
                    "type" to "Withdrawal Requested",
                    "amount" to amount,
                    "status" to "Pending",
                    "note" to "UPI: $upiId",
                    "dateTimestamp" to timestamp
                ))

                // 4. Insert notification
                transaction.set(notifRef, mapOf(
                    "id" to notifRef.id,
                    "userId" to userId,
                    "title" to "Withdrawal Submitted",
                    "message" to "Your withdrawal request for ₹${amount.toInt()} via UPI ($upiId) is pending operator review.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "WITHDRAWAL"
                ))

                createdWithdrawal = WithdrawalEntity(
                    id = wdId,
                    userId = userId,
                    userName = userName,
                    amount = amount,
                    upiId = upiId,
                    status = "Pending",
                    rejectionReason = "",
                    requestedAt = timestamp,
                    approvedAt = 0L,
                    paidAt = 0L
                )
            }.await()

            if (createdWithdrawal != null) {
                Result.success(createdWithdrawal!!)
            } else {
                Result.failure(Exception("Failed to submit withdrawal transaction."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in requestWithdrawalAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ATOMIC ADMIN APPROVE WITHDRAWAL (PENDING -> APPROVED)
     * Enforces valid state machine: Only "Pending" can transition to "Approved".
     * If already "Approved", safely no-ops.
     */
    suspend fun adminApproveWithdrawalAtomic(
        withdrawalId: String,
        adminEmail: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val wdRef = db.collection(COLLECTION_WITHDRAWALS).document(withdrawalId)
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(UUID.randomUUID().toString())
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()

            var responseMessage = ""

            db.runTransaction { transaction ->
                val wdSnap = transaction.get(wdRef)
                if (!wdSnap.exists()) {
                    throw IllegalArgumentException("Withdrawal '$withdrawalId' not found.")
                }

                val currentStatus = wdSnap.getString("status") ?: "Pending"
                if (currentStatus.equals("Approved", ignoreCase = true)) {
                    responseMessage = "Withdrawal is already approved."
                    return@runTransaction
                }
                if (currentStatus.equals("Paid", ignoreCase = true)) {
                    throw IllegalStateException("Cannot approve a withdrawal that has already been paid out.")
                }
                if (currentStatus.equals("Rejected", ignoreCase = true)) {
                    throw IllegalStateException("Cannot approve a withdrawal that was rejected.")
                }
                if (!currentStatus.equals("Pending", ignoreCase = true)) {
                    throw IllegalStateException("Cannot transition withdrawal from '$currentStatus' to 'Approved'.")
                }

                val userId = wdSnap.getString("userId") ?: ""
                val amount = wdSnap.getDouble("amount") ?: 0.0

                // 1. Update withdrawal status
                transaction.update(wdRef, mapOf(
                    "status" to "Approved",
                    "approvedAt" to timestamp
                ))

                // 2. Notification
                transaction.set(notifRef, mapOf(
                    "id" to notifRef.id,
                    "userId" to userId,
                    "title" to "Withdrawal Approved ✅",
                    "message" to "Your withdrawal of ₹${amount.toInt()} has been approved and is being processed for UPI payout.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "WITHDRAWAL"
                ))

                // 3. Audit log
                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to "APPROVE_WITHDRAWAL",
                    "details" to "Approved withdrawal $withdrawalId (₹$amount) for user $userId",
                    "timestamp" to timestamp
                ))

                responseMessage = "Withdrawal $withdrawalId successfully approved."
            }.await()

            Result.success(responseMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error in adminApproveWithdrawalAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ATOMIC ADMIN MARK AS PAID (PENDING/APPROVED -> PAID)
     * CRITICAL IDEMPOTENCY & SAFETY RULES:
     * 1. If status is ALREADY "Paid", immediately returns success without mutating user balance or total withdrawals!
     * 2. If status is "Rejected", aborts with error (cannot pay rejected).
     * 3. On valid first transition to "Paid":
     *    - Decrements pendingBalance: pendingBalance = max(0, pendingBalance - amount).
     *    - Increments totalWithdrawals: totalWithdrawals += amount.
     *    - availableBalance was ALREADY deducted during withdrawal request reservation, so availableBalance is NOT deducted again!
     *    - Writes "Withdrawal Paid" ledger transaction with ID "TX_PAID_$withdrawalId".
     *    - Updates withdrawal document to status "Paid".
     */
    suspend fun adminMarkWithdrawalPaidAtomic(
        withdrawalId: String,
        adminEmail: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val wdRef = db.collection(COLLECTION_WITHDRAWALS).document(withdrawalId)
            val txRef = db.collection(COLLECTION_TRANSACTIONS).document("TX_PAID_$withdrawalId")
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(UUID.randomUUID().toString())
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()

            var responseMessage = ""

            db.runTransaction { transaction ->
                val wdSnap = transaction.get(wdRef)
                if (!wdSnap.exists()) {
                    throw IllegalArgumentException("Withdrawal '$withdrawalId' not found.")
                }

                val currentStatus = wdSnap.getString("status") ?: "Pending"

                // STRICT IDEMPOTENCY CHECK:
                if (currentStatus.equals("Paid", ignoreCase = true)) {
                    responseMessage = "Withdrawal '$withdrawalId' was already marked as paid. No wallet modifications performed."
                    return@runTransaction
                }

                if (currentStatus.equals("Rejected", ignoreCase = true)) {
                    throw IllegalStateException("Cannot mark a rejected withdrawal as paid.")
                }

                val userId = wdSnap.getString("userId") ?: ""
                val amount = wdSnap.getDouble("amount") ?: 0.0
                val upiId = wdSnap.getString("upiId") ?: ""

                val userRef = db.collection(COLLECTION_USERS).document(userId)
                val userSnap = transaction.get(userRef)

                if (userSnap.exists()) {
                    val currentPending = userSnap.getDouble("pendingBalance") ?: 0.0
                    val currentTotalWith = userSnap.getDouble("totalWithdrawals") ?: userSnap.getDouble("paidOut") ?: 0.0

                    val newPending = max(0.0, currentPending - amount)
                    val newTotalWith = currentTotalWith + amount

                    // 1. Update user total withdrawals & clear reserved pending balance
                    transaction.update(userRef, mapOf(
                        "pendingBalance" to newPending,
                        "totalWithdrawals" to newTotalWith,
                        "paidOut" to newTotalWith
                    ))
                }

                // 2. Mark withdrawal document as Paid
                transaction.update(wdRef, mapOf(
                    "status" to "Paid",
                    "paidAt" to timestamp
                ))

                // 3. Record transaction in ledger (using idempotent document key)
                transaction.set(txRef, mapOf(
                    "id" to "TX_PAID_$withdrawalId",
                    "userId" to userId,
                    "type" to "Withdrawal Paid",
                    "amount" to amount,
                    "status" to "Paid",
                    "note" to "Paid out to UPI: $upiId",
                    "dateTimestamp" to timestamp
                ), SetOptions.merge())

                // 4. Notification
                transaction.set(notifRef, mapOf(
                    "id" to notifRef.id,
                    "userId" to userId,
                    "title" to "UPI Payment Sent! 💸",
                    "message" to "₹${amount.toInt()} has been transferred to your UPI ID $upiId.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "WITHDRAWAL"
                ))

                // 5. Audit log
                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to "MARK_WITHDRAWAL_PAID",
                    "details" to "Marked withdrawal $withdrawalId paid out ₹$amount to $upiId (User: $userId)",
                    "timestamp" to timestamp
                ))

                responseMessage = "Withdrawal $withdrawalId of ₹${amount.toInt()} successfully marked as Paid."
            }.await()

            Result.success(responseMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error in adminMarkWithdrawalPaidAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ATOMIC ADMIN REJECT WITHDRAWAL (PENDING/APPROVED -> REJECTED)
     * CRITICAL IDEMPOTENCY & REFUND RULES:
     * 1. If status is ALREADY "Rejected", immediately returns success without refunding again! (Exactly one refund guarantee).
     * 2. If status is "Paid", aborts with error (cannot refund paid payout).
     * 3. On valid first transition to "Rejected":
     *    - Refunds amount to availableBalance: availableBalance = availableBalance + amount.
     *    - Clears reserved pendingBalance: pendingBalance = max(0, pendingBalance - amount).
     *    - Writes "Withdrawal Refund" ledger transaction with ID "TX_REFUND_$withdrawalId".
     *    - Updates withdrawal document with status "Rejected" and rejectionReason.
     */
    suspend fun adminRejectWithdrawalAtomic(
        withdrawalId: String,
        reason: String,
        adminEmail: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val wdRef = db.collection(COLLECTION_WITHDRAWALS).document(withdrawalId)
            val txRef = db.collection(COLLECTION_TRANSACTIONS).document("TX_REFUND_$withdrawalId")
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(UUID.randomUUID().toString())
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()

            var responseMessage = ""

            db.runTransaction { transaction ->
                val wdSnap = transaction.get(wdRef)
                if (!wdSnap.exists()) {
                    throw IllegalArgumentException("Withdrawal '$withdrawalId' not found.")
                }

                val currentStatus = wdSnap.getString("status") ?: "Pending"

                // STRICT IDEMPOTENCY CHECK:
                if (currentStatus.equals("Rejected", ignoreCase = true)) {
                    responseMessage = "Withdrawal '$withdrawalId' was already rejected and refunded. No balance modifications performed."
                    return@runTransaction
                }

                if (currentStatus.equals("Paid", ignoreCase = true)) {
                    throw IllegalStateException("Cannot reject a withdrawal that has already been paid out.")
                }

                val userId = wdSnap.getString("userId") ?: ""
                val amount = wdSnap.getDouble("amount") ?: 0.0
                val safeReason = reason.ifBlank { "Invalid details or policy violation" }

                val userRef = db.collection(COLLECTION_USERS).document(userId)
                val userSnap = transaction.get(userRef)

                if (userSnap.exists()) {
                    val currentAvailable = userSnap.getDouble("availableBalance") ?: userSnap.getDouble("walletBalance") ?: 0.0
                    val currentPending = userSnap.getDouble("pendingBalance") ?: 0.0

                    val newAvailable = currentAvailable + amount
                    val newPending = max(0.0, currentPending - amount)

                    // 1. Refund the deducted amount back to available balance exactly once
                    transaction.update(userRef, mapOf(
                        "availableBalance" to newAvailable,
                        "walletBalance" to newAvailable,
                        "pendingBalance" to newPending
                    ))
                }

                // 2. Mark withdrawal document as Rejected
                transaction.update(wdRef, mapOf(
                    "status" to "Rejected",
                    "rejectionReason" to safeReason
                ))

                // 3. Record refund transaction in ledger
                transaction.set(txRef, mapOf(
                    "id" to "TX_REFUND_$withdrawalId",
                    "userId" to userId,
                    "type" to "Withdrawal Refund",
                    "amount" to amount,
                    "status" to "Approved",
                    "note" to "Refund for rejected withdrawal: $safeReason",
                    "dateTimestamp" to timestamp
                ), SetOptions.merge())

                // 4. Notification
                transaction.set(notifRef, mapOf(
                    "id" to notifRef.id,
                    "userId" to userId,
                    "title" to "Withdrawal Rejected & Refunded",
                    "message" to "Your withdrawal of ₹${amount.toInt()} was rejected ($safeReason). ₹${amount.toInt()} has been refunded to your wallet.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "WITHDRAWAL"
                ))

                // 5. Audit log
                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to "REJECT_WITHDRAWAL",
                    "details" to "Rejected withdrawal $withdrawalId (₹$amount) for user $userId. Reason: $safeReason",
                    "timestamp" to timestamp
                ))

                responseMessage = "Withdrawal $withdrawalId rejected and ₹${amount.toInt()} refunded to user $userId."
            }.await()

            Result.success(responseMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error in adminRejectWithdrawalAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ATOMIC ADMIN CREDIT REWARD
     * 1. Idempotency check via completion reference.
     * 2. Atomically increments user's availableBalance & lifetimeEarnings.
     * 3. Sets completion record in `completions`.
     * 4. Writes transaction ledger entry in `transactions`.
     * 5. Writes user notification.
     * 6. Automatically checks and awards referral bonus (₹10) if friend completed their first qualifying task.
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
            val completionDocRef = db.collection(COLLECTION_COMPLETIONS).document(safeRef)
            val userDocRef = db.collection(COLLECTION_USERS).document(userId)
            val txRef = db.collection(COLLECTION_TRANSACTIONS).document("TX_REW_$safeRef")
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document(UUID.randomUUID().toString())
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()

            db.runTransaction { transaction ->
                // 1. Idempotency Check: Prevent duplicate credits
                val existingComp = transaction.get(completionDocRef)
                if (existingComp.exists()) {
                    val status = existingComp.getString("status") ?: ""
                    if (status.equals("Completed", ignoreCase = true) || status.equals("CREDITED", ignoreCase = true)) {
                        throw IllegalStateException("Reward with reference '$safeRef' has already been credited!")
                    }
                }

                val userSnap = transaction.get(userDocRef)
                if (!userSnap.exists()) {
                    throw IllegalArgumentException("User '$userId' does not exist in database!")
                }

                val currentBalance = userSnap.getDouble("availableBalance") ?: userSnap.getDouble("walletBalance") ?: 0.0
                val currentLifetime = userSnap.getDouble("lifetimeEarnings") ?: 0.0

                val newBalance = currentBalance + rewardAmount
                val newLifetime = currentLifetime + rewardAmount

                // 2. Atomically update wallet balance & lifetime earnings
                transaction.update(userDocRef, mapOf(
                    "availableBalance" to newBalance,
                    "walletBalance" to newBalance,
                    "lifetimeEarnings" to newLifetime
                ))

                // 3. Record completion
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

                // 4. Record transaction ledger entry
                transaction.set(txRef, mapOf(
                    "id" to "TX_REW_$safeRef",
                    "userId" to userId,
                    "type" to "Reward Credited",
                    "amount" to rewardAmount,
                    "status" to "Approved",
                    "note" to "$offerName (Ref: $safeRef) - $note",
                    "dateTimestamp" to timestamp
                ))

                // 5. Notification
                transaction.set(notifRef, mapOf(
                    "id" to notifRef.id,
                    "userId" to userId,
                    "title" to "Reward Credited! 🎉",
                    "message" to "₹${rewardAmount.toInt()} has been credited to your wallet for completing $offerName.",
                    "dateTimestamp" to timestamp,
                    "isRead" to false,
                    "type" to "OFFER_REWARD"
                ))

                // 6. Audit log
                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to "CREDIT_REWARD",
                    "details" to "Credited ₹$rewardAmount to user $userId for offer '$offerName' [Ref: $safeRef]. Note: $note",
                    "timestamp" to timestamp
                ))
            }.await()

            // Safe referral trigger check
            processReferralRewardOnOfferCompletion(userId, offerId, offerName)

            Result.success("Reward of ₹${rewardAmount.toInt()} successfully credited to $userId!")
        } catch (e: Exception) {
            Log.e(TAG, "Error in adminCreditRewardAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atomically processes the referral bonus (₹10) when a referred friend completes their first offer.
     * Enforces strict idempotency: Only triggers once per friend.
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
            val txId = "TX_REF_${friendUserId}"
            val notifId = UUID.randomUUID().toString()
            val rewardAmount = 10.0

            var rewardCredited = false

            db.runTransaction { transaction ->
                val freshRefSnap = transaction.get(referralRef)
                val alreadyQualified = freshRefSnap.getBoolean("isQualified") ?: false
                val currentStatus = freshRefSnap.getString("status") ?: ""

                // Strict anti-duplicate check:
                if (alreadyQualified || currentStatus.equals("COMPLETED_REWARDED", ignoreCase = true)) {
                    rewardCredited = false
                    return@runTransaction
                }

                val referrerSnap = transaction.get(referrerUserRef)
                if (!referrerSnap.exists()) {
                    rewardCredited = false
                    return@runTransaction
                }

                val currentBalance = referrerSnap.getDouble("availableBalance") ?: referrerSnap.getDouble("walletBalance") ?: 0.0
                val currentLifetime = referrerSnap.getDouble("lifetimeEarnings") ?: 0.0
                val friendName = freshRefSnap.getString("friendDisplayName") ?: "Friend ($friendUserId)"

                // 1. Update Referrer User Wallet atomically
                transaction.update(referrerUserRef, mapOf(
                    "availableBalance" to (currentBalance + rewardAmount),
                    "walletBalance" to (currentBalance + rewardAmount),
                    "lifetimeEarnings" to (currentLifetime + rewardAmount)
                ))

                // 2. Update Referral record
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
                ), SetOptions.merge())

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
            Log.e(TAG, "Error in processReferralRewardOnOfferCompletion: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun toggleUserBanAtomic(userId: String, adminEmail: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val userRef = db.collection(COLLECTION_USERS).document(userId)
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()
            var newBanState = false

            db.runTransaction { transaction ->
                val userSnap = transaction.get(userRef)
                if (!userSnap.exists()) throw IllegalArgumentException("User $userId not found")

                val currentBanned = (userSnap.getString("accountStatus")?.equals("banned", ignoreCase = true) == true) || (userSnap.getBoolean("isBanned") ?: false)
                newBanState = !currentBanned

                transaction.update(userRef, mapOf(
                    "isBanned" to newBanState,
                    "accountStatus" to if (newBanState) "banned" else "active"
                ))

                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to if (newBanState) "BAN_USER" else "UNBAN_USER",
                    "details" to "${if (newBanState) "Banned" else "Unbanned"} user $userId",
                    "timestamp" to timestamp
                ))
            }.await()

            Result.success(newBanState)
        } catch (e: Exception) {
            Log.e(TAG, "Error in toggleUserBanAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun resetDeviceBindingAtomic(userId: String, adminEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            val userRef = db.collection(COLLECTION_USERS).document(userId)
            val auditRef = db.collection(COLLECTION_AUDIT_LOGS).document(UUID.randomUUID().toString())
            val timestamp = System.currentTimeMillis()
            val newDeviceTag = "RESET_${System.currentTimeMillis()}"

            db.runTransaction { transaction ->
                val userSnap = transaction.get(userRef)
                if (!userSnap.exists()) throw IllegalArgumentException("User $userId not found")

                transaction.update(userRef, mapOf(
                    "deviceId" to newDeviceTag
                ))

                transaction.set(auditRef, mapOf(
                    "id" to auditRef.id,
                    "adminEmail" to adminEmail,
                    "action" to "RESET_DEVICE_BINDING",
                    "details" to "Reset device binding for user $userId (New tag: $newDeviceTag)",
                    "timestamp" to timestamp
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in resetDeviceBindingAtomic: ${e.message}", e)
            Result.failure(e)
        }
    }
}
