package com.vidagdhan.ads.data.local

import androidx.room.*
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

/**
 * Room DAO for all ad storage operations.
 *
 * All queries are suspend functions — they run on the Room
 * dispatcher and are safe to call from any coroutine.
 */
@Dao
interface AdDao {

    /**
     * Insert or replace the full list of fresh ads received from the API.
     * REPLACE strategy handles re-fetches gracefully.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ads: List<AdItem>)

    /**
     * Return all stored ads for a specific screen.
     * The SDK never hard-codes screen names here — the caller supplies [screenId].
     */
    @Query("SELECT * FROM ads WHERE screen_id = :screenId")
    suspend fun getAdsForScreen(screenId: String): List<AdItem>

    /** Return every ad currently stored (useful for batch date-filtering). */
    @Query("SELECT * FROM ads")
    suspend fun getAllAds(): List<AdItem>

    /** Wipe the entire table — called before a fresh sync to avoid stale data. */
    @Query("DELETE FROM ads")
    suspend fun clearAll()
}
