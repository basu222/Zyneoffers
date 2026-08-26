package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdDirect(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getUserByDeviceId(deviceId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY registeredAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET deviceId = :newDeviceId WHERE userId = :userId")
    suspend fun resetDeviceId(userId: String, newDeviceId: String)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE isActive = 1 ORDER BY isPinned DESC, createdAt DESC")
    fun getActiveOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers ORDER BY isPinned DESC, createdAt DESC")
    fun getAllOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE id = :id LIMIT 1")
    fun getOfferById(id: String): Flow<OfferEntity?>

    @Query("SELECT * FROM offers WHERE id = :id LIMIT 1")
    suspend fun getOfferByIdDirect(id: String): OfferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOffer(offer: OfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOffers(offers: List<OfferEntity>)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOffer(id: String)

    @Query("DELETE FROM offers")
    suspend fun deleteAllOffers()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteTransactionsByUserId(userId: String)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsByUserId(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId AND status = 'Pending' LIMIT 1")
    suspend fun getPendingWithdrawalByUserId(userId: String): WithdrawalEntity?

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("DELETE FROM withdrawals WHERE userId = :userId")
    suspend fun deleteWithdrawalsByUserId(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getNotificationsByUserId(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteNotificationsByUserId(userId: String)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners ORDER BY displayOrder ASC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBanner(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBanner(id: String)

    @Query("DELETE FROM banners")
    suspend fun deleteAllBanners()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY publishedAt DESC LIMIT 1")
    fun getActiveAnnouncement(): Flow<AnnouncementEntity?>

    @Query("SELECT * FROM announcements ORDER BY publishedAt DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String)

    @Query("DELETE FROM announcements")
    suspend fun deleteAllAnnouncements()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: SettingsEntity)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerUserId = :referrerUserId ORDER BY dateReferred DESC")
    fun getReferralsByReferrer(referrerUserId: String): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE friendUserId = :friendUserId LIMIT 1")
    suspend fun getReferralForFriend(friendUserId: String): ReferralEntity?

    @Query("SELECT * FROM referrals ORDER BY dateReferred DESC")
    fun getAllReferrals(): Flow<List<ReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReferral(referral: ReferralEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReferrals(referrals: List<ReferralEntity>)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface OfferActivityDao {
    @Query("SELECT * FROM offer_activities WHERE userId = :userId ORDER BY startedAt DESC")
    fun getOfferActivitiesByUser(userId: String): Flow<List<OfferActivityEntity>>

    @Query("SELECT * FROM offer_activities WHERE trackingId = :trackingId LIMIT 1")
    suspend fun getOfferActivityByTrackingId(trackingId: String): OfferActivityEntity?

    @Query("SELECT * FROM offer_activities WHERE userId = :userId AND offerId = :offerId LIMIT 1")
    suspend fun getOfferActivityByUserAndOffer(userId: String, offerId: String): OfferActivityEntity?

    @Query("SELECT * FROM offer_activities ORDER BY startedAt DESC")
    fun getAllOfferActivities(): Flow<List<OfferActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: OfferActivityEntity)
}
