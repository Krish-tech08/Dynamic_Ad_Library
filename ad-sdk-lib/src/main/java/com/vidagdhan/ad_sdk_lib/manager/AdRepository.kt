package com.vidagdhan.ad_sdk_lib.manager

import com.vidagdhan.ad_sdk_lib.util.AdDateUtils
import com.vidagdhan.ads.data.local.AdStorage
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

/**
 * AdRepository — single source of truth for ad data.
 *
 * Orchestrates the flow:
 *   Network (AdFetcher) → filter → persist (AdStorage) → serve to callers
 *
 * If the network is unavailable the repository falls back to whatever
 * valid ads are already in the local database.
 */
class AdRepository(
    private val fetcher  : AdFetcher,
    private val storage  : AdStorage,
    private val appId    : String
) {

    /**
     * Sync ads from the API and persist them locally.
     *
     * On network success  → replace local cache with fresh data.
     * On network failure  → silently keep existing cached data.
     *
     * @return true if the sync succeeded, false if it fell back to cache.
     */
    suspend fun syncAds(): Boolean {
        val result = fetcher.fetchAndFilter()
        return if (result.isSuccess) {
            storage.replaceAll(result.getOrDefault(emptyList()))
            true
        } else {
            false   // caller may log result.exceptionOrNull() if desired
        }
    }

    /**
     * Return locally stored, currently-valid ads for [screenId].
     *
     * Date filtering is re-applied here so stale cached ads that have
     * expired since the last sync are never surfaced to the UI layer.
     *
     * @param screenId  Opaque string supplied by the host app.
     *                  The SDK never interprets this value.
     */
    suspend fun getAdsForScreen(screenId: String): List<AdItem> {
        return storage.getAdsForScreen(screenId)
            .filter { ad ->
                ad.app_id == appId &&
                AdDateUtils.isAdActive(ad.start_date, ad.end_date)
            }
    }

    /**
     * Convenience: return the first valid ad for [screenId] of [type],
     * or null if none exists.
     */
    suspend fun getAdForScreen(screenId: String, type: String): AdItem? =
        getAdsForScreen(screenId).firstOrNull { it.type == type }
}
