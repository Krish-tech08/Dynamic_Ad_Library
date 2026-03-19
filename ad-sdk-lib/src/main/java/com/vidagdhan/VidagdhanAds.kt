package com.vidagdhan

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.vidagdhan.ad_sdk_lib.manager.AdManager
import com.vidagdhan.ads.ui.banner.BannerAdView
import com.vidagdhan.ad_sdk_lib.ui.popup.PopupAdDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * VidagdhanAds — the single public entry-point for the Ad SDK.
 *
 * ## Lifecycle
 * 1. Call [initialize] once in Application.onCreate() or before first use.
 * 2. Optionally call [syncAds] to pre-warm the local cache.
 * 3. Call [showBanner] / [showPopup] from any Activity/Fragment.
 *
 * The SDK does **not** attach itself to any screen automatically.
 * The host app decides where and when ads are shown.
 */
object VidagdhanAds {

    /**
     * SDK-owned coroutine scope.
     * SupervisorJob ensures one failed child doesn't cancel the others.
     * Dispatchers.Main is used so UI operations are safe by default.
     */
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ─── Initialisation ───────────────────────────────────────────────────────

    /**
     * Initialise the SDK. Call this **once** before any other SDK method,
     * typically in your Application class.
     *
     * ```kotlin
     * // Application.kt
     * VidagdhanAds.initialize(this, "https://api.example.com/ads")
     * ```
     *
     * @param context       Application context.
     * @param apiUrl        Full URL of the backend JSON endpoint.
     * @param enableLogging Set true in debug builds to see HTTP logs in Logcat.
     *                      Always false in production.
     */
    fun initialize(
        context      : Context,
        apiUrl       : String,
        enableLogging: Boolean = false
    ) {
        AdManager.create(
            context       = context.applicationContext,
            apiUrl        = apiUrl,
            applicationId = context.applicationContext.packageName,
            enableLogging = enableLogging
        )
    }

    // ─── Sync ─────────────────────────────────────────────────────────────────

    /**
     * Fetch fresh ads from the API and persist them locally.
     *
     * This is a fire-and-forget call. Provide [onComplete] if you need
     * to react once the sync finishes (e.g. to show an ad immediately).
     *
     * ```kotlin
     * VidagdhanAds.syncAds { success ->
     *     if (success) VidagdhanAds.showBanner(activity, container, "home_screen")
     * }
     * ```
     *
     * @param onComplete Optional callback executed on the main thread.
     *                   Receives true on success, false if the network failed
     *                   (the local cache will still be used as a fallback).
     */
    fun syncAds(onComplete: ((Boolean) -> Unit)? = null) {
        sdkScope.launch {
            val success = AdManager.require().syncAds()
            onComplete?.invoke(success)
        }
    }

    // ─── Banner ───────────────────────────────────────────────────────────────

    /**
     * Programmatically load and display a banner ad inside [container].
     *
     * The SDK removes any previously SDK-added banner from [container] before
     * adding the new one, so this method is safe to call multiple times.
     *
     * If the frequency gate blocks the ad, [container] is left unchanged.
     *
     * ```kotlin
     * VidagdhanAds.showBanner(activity, binding.adContainer, screenId = "home_screen")
     * ```
     *
     * @param activity  The host Activity (used for lifecycle-aware coroutines).
     * @param container The [ViewGroup] that will hold the banner.
     * @param screenId  Opaque placement key decided by the host app.
     */
    fun showBanner(activity: Activity, container: ViewGroup, screenId: String) {
        // Remove any existing SDK banner to avoid stacking duplicates
        container.findViewWithTag<BannerAdView>(TAG_BANNER)
            ?.let { container.removeView(it) }

        val bannerView = BannerAdView(activity).apply {
            tag = TAG_BANNER
        }

        container.addView(bannerView)
        bannerView.loadAd(screenId)
    }

    // ─── Popup ────────────────────────────────────────────────────────────────

    /**
     * Resolve and display a popup ad for [screenId].
     *
     * If no valid ad exists, or the frequency gate blocks the impression,
     * nothing is shown and no error is thrown.
     *
     * ```kotlin
     * VidagdhanAds.showPopup(activity, screenId = "exit_screen")
     * ```
     *
     * @param activity  The host Activity (needed to show the Dialog).
     * @param screenId  Opaque placement key decided by the host app.
     */
    fun showPopup(activity: Activity, screenId: String) {
        val lifecycleOwner = activity as? LifecycleOwner ?: return

        val scope: CoroutineScope = lifecycleOwner.lifecycleScope

        scope.launch {
            val ad = try {
                AdManager.require().resolveAd(screenId, "popup")
            } catch (e: Exception) {
                null
            } ?: return@launch

            if (!activity.isFinishing && !activity.isDestroyed) {
                PopupAdDialog.show(activity, ad)
            }
        }
    }

    // ─── Internal constants ───────────────────────────────────────────────────

    private const val TAG_BANNER = "vidagdhan_banner_view"
}
