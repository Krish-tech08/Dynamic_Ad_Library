package com.vidagdhan.ads.ui.banner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.vidagdhan.ad_sdk_lib.R
import com.vidagdhan.ad_sdk_lib.manager.AdManager
import kotlinx.coroutines.launch

/**
 * BannerAdView — a self-contained FrameLayout that can be dropped into
 * any XML layout or created programmatically.
 *
 * XML usage:
 * ```xml
 * <com.vidagdhan.ads.ui.banner.BannerAdView
 *     android:id="@+id/bannerAd"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:screenId="home_screen" />
 * ```
 *
 * The view reads the `screenId` attribute and auto-loads the correct ad
 * once it is attached to a window that has a LifecycleOwner (Activity/Fragment).
 *
 * Programmatic usage — call [loadAd] manually after attaching to a parent.
 */
class BannerAdView @JvmOverloads constructor(
    context: Context,
    attrs  : AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val imageView: ImageView

    /** Screen identifier set via XML attribute app:screenId or [loadAd]. */
    private var screenId: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_banner_ad, this, true)
        imageView = findViewById(R.id.bannerImage)

        // Read custom XML attribute
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.BannerAdView)
            screenId = ta.getString(R.styleable.BannerAdView_screenId)
            ta.recycle()
        }
    }

    /**
     * Called by Android once the view is attached to a window.
     * If [screenId] was set via XML we auto-trigger the load here.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        screenId?.let { loadAd(it) }
    }

    /**
     * Load and display the banner ad for [screenId].
     *
     * Safe to call from the main thread — launches a coroutine tied to the
     * nearest LifecycleOwner so the job is automatically cancelled on destroy.
     *
     * @param screenId Opaque string that the host app decides; the SDK never
     *                 interprets or hardcodes this value.
     */
    fun loadAd(screenId: String) {
        this.screenId = screenId

        // Resolve lifecycle — falls back to GlobalScope only if truly detached
        val scope = findViewTreeLifecycleOwner()?.lifecycleScope ?: return

        scope.launch {
            val ad = try {
                AdManager.require().resolveAd(screenId, "banner")
            } catch (e: Exception) {
                null
            }

            if (ad == null) {
                visibility = GONE
                return@launch
            }

            visibility = VISIBLE

            Glide.with(context)
                .load(ad.image_url)
                .placeholder(R.drawable.ad_placeholder)
                .error(R.drawable.ad_placeholder)
                .into(imageView)

            setOnClickListener {
                ad.cta?.let { url ->
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }
}
