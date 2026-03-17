package com.vidagdhan.ad_sdk_lib.manager

import android.content.Context
import android.util.Log
import com.vidagdhan.ad_sdk_lib.api.AdNetworkClient
import com.vidagdhan.ad_sdk_lib.data.model.AdItem
import com.vidagdhan.ads.data.local.AdStorage

internal class AdManager private constructor(
    private val repository: AdRepository,
    private val storage   : AdStorage
) {

    companion object {
        private const val TAG = "VidagdhanAds"

        @Volatile
        private var instance: AdManager? = null

        fun create(
            context      : Context,
            apiUrl       : String,
            applicationId: String,
            enableLogging: Boolean = false
        ): AdManager {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "initialize() called")
            Log.d(TAG, "  apiUrl        = $apiUrl")
            Log.d(TAG, "  applicationId = $applicationId")
            Log.d(TAG, "  enableLogging = $enableLogging")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val apiService = AdNetworkClient.create(apiUrl, enableLogging)
            val storage    = AdStorage(context)
            val fetcher    = AdFetcher(apiService, applicationId)
            val repository = AdRepository(fetcher, storage, applicationId)

            return AdManager(repository, storage).also { instance = it }
        }

        fun require(): AdManager = instance
            ?: error("VidagdhanAds is not initialised. Call VidagdhanAds.initialize() first.")
    }

    suspend fun syncAds(): Boolean {
        Log.d(TAG, "syncAds() → starting network fetch...")
        val success = repository.syncAds()
        Log.d(TAG, "syncAds() → result: ${if (success) "✅ SUCCESS" else "❌ FAILED (using cache)"}")
        return success
    }

    suspend fun resolveAd(screenId: String, type: String): AdItem? {
        Log.d(TAG, "resolveAd() → screenId='$screenId' type='$type'")

        val ad = repository.getAdForScreen(screenId, type)

        if (ad == null) {
            Log.w(TAG, "resolveAd() → ❌ No valid ad found in DB for screenId='$screenId' type='$type'")
            Log.w(TAG, "  Possible reasons:")
            Log.w(TAG, "  1. syncAds() not called or failed")
            Log.w(TAG, "  2. app_id in backend JSON doesn't match '${ ad ?: "unknown" }'")
            Log.w(TAG, "  3. Ad is outside start_date / end_date window")
            Log.w(TAG, "  4. No ad with type='$type' for screenId='$screenId'")
            return null
        }

        Log.d(TAG, "resolveAd() → ✅ Ad found: id=${ad.id} image=${ad.image_url} frequency=${ad.frequency}")

        val shouldShow = storage.shouldShowNow(screenId, ad.id, ad.frequency)
        Log.d(TAG, "resolveAd() → frequency gate: ${if (shouldShow) "✅ SHOW" else "⏭ SKIP (frequency not met yet)"}")

        return if (shouldShow) ad else null
    }
}
