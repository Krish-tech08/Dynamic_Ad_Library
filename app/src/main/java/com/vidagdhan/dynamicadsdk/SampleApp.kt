package com.vidagdhan.dynamicadsdk

import android.app.Application
import com.vidagdhan.VidagdhanAds

/**
 * SampleApp — demonstrates how the host app initialises the SDK.
 *
 * Initialize once here so the SDK is ready before any Activity starts.
 * Replace ADS_API_URL with your real endpoint.
 */
class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        VidagdhanAds.initialize(
            context       = this,
            apiUrl        = "https://vidagdhan.com/",
            enableLogging = BuildConfig.DEBUG   // logs HTTP traffic in debug only
        )

        // Pre-warm the local cache in the background.
        // Ads will be served from cache until this completes.
        VidagdhanAds.syncAds()
    }

    companion object {
        // Replace this with your actual ads JSON endpoint.
        private const val ADS_API_URL = "https://vidagdhan.com/app_ads/ads.json"
    }
}
