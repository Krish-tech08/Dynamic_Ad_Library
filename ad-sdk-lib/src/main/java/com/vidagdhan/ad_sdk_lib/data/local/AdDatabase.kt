package com.vidagdhan.ads.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

/**
 * Singleton Room database for the SDK.
 *
 * Uses a dedicated database name ("vidagdhan_ads_db") so it never
 * conflicts with the host app's own Room databases.
 */
@Database(
    entities  = [AdItem::class],
    version   = 1,
    exportSchema = false
)
abstract class AdDatabase : RoomDatabase() {

    abstract fun adDao(): AdDao

    companion object {

        @Volatile
        private var INSTANCE: AdDatabase? = null

        /**
         * Returns the singleton instance, creating it if necessary.
         * Thread-safe via double-checked locking.
         */
        fun getInstance(context: Context): AdDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdDatabase::class.java,
                    "vidagdhan_ads_db"
                )
                    .fallbackToDestructiveMigration()   // safe for an SDK — no user data here
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
