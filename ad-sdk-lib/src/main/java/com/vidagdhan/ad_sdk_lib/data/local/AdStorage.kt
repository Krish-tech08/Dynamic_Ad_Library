package com.vidagdhan.ads.data.local

import android.content.Context
import android.content.SharedPreferences
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

/**
 * AdStorage — two distinct responsibilities:
 *
 *  1. **Room persistence** — delegates to [AdDao] for structured ad records.
 *  2. **Frequency tracking** — uses a lightweight [SharedPreferences] file
 *     to count how many times each (screenId + adId) pair has been seen,
 *     without polluting the Room schema.
 */
class AdStorage(context: Context) {

    private val dao: AdDao = AdDatabase.getInstance(context).adDao()

    /** Dedicated prefs file — isolated from the host app's prefs. */
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "vidagdhan_ads_frequency", Context.MODE_PRIVATE
    )

    // ─── Room operations ──────────────────────────────────────────────────────

    /** Wipe existing ads and store the fresh list in one transaction. */
    suspend fun replaceAll(ads: List<AdItem>) {
        dao.clearAll()
        dao.insertAll(ads)
    }

    /** Retrieve all stored ads for a given screen. */
    suspend fun getAdsForScreen(screenId: String): List<AdItem> =
        dao.getAdsForScreen(screenId)

    /** Retrieve every stored ad (used for offline date re-filtering). */
    suspend fun getAllAds(): List<AdItem> =
        dao.getAllAds()

    // ─── Frequency tracking ───────────────────────────────────────────────────

    /**
     * Increment the impression counter for [adId] on [screenId] and return
     * whether this impression number is a multiple of [frequency].
     *
     * Counter key format: "freq_<screenId>_<adId>"
     */
    fun shouldShowNow(screenId: String, adId: String, frequency: Int): Boolean {
        if (frequency <= 0) return true                      // safety guard
        val key     = "freq_${screenId}_${adId}"
        val current = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, current).apply()
        return (current % frequency == 0)
    }

    /** Reset impression counters for all ads on a given screen. */
    fun resetCounters(screenId: String) {
        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith("freq_${screenId}_") }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    /** Reset every counter across all screens. */
    fun resetAllCounters() {
        prefs.edit().clear().apply()
    }
}
