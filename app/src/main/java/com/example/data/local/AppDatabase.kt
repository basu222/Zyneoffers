package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        OfferEntity::class,
        TransactionEntity::class,
        WithdrawalEntity::class,
        NotificationEntity::class,
        BannerEntity::class,
        AnnouncementEntity::class,
        SettingsEntity::class,
        ReferralEntity::class,
        AuditLogEntity::class,
        OfferActivityEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun offerDao(): OfferDao
    abstract fun transactionDao(): TransactionDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun notificationDao(): NotificationDao
    abstract fun bannerDao(): BannerDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun settingsDao(): SettingsDao
    abstract fun referralDao(): ReferralDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun offerActivityDao(): OfferActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zyne_offers_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
