package com.vidagdhan.ads.ui.popup

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.vidagdhan.ad_sdk_lib.R
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

/**
 * PopupAdDialog — a fullscreen-dimmed, centered Dialog that displays
 * a popup ad with a close (✕) button and an optional CTA click.
 *
 * The dialog is created and shown only when [AdManager.resolveAd] returns
 * a non-null ad — the frequency gate is enforced before this class is ever
 * instantiated.
 *
 * Construction is done exclusively through the companion [show] factory so
 * callers never deal with the Dialog lifecycle manually.
 */
class PopupAdDialog private constructor(
    context: Context,
    private val ad: AdItem
) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_popup_ad, null)
        setContentView(view)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val adImage   : ImageView   = view.findViewById(R.id.popupAdImage)
        val closeButton: ImageButton = view.findViewById(R.id.btnClosePopup)

        // Load ad image via Glide
        Glide.with(context)
            .load(ad.image_url)
            .placeholder(R.drawable.ad_placeholder)
            .error(R.drawable.ad_placeholder)
            .into(adImage)

        // CTA click — open URL in browser
        adImage.setOnClickListener {
            ad.cta?.let { url ->
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            dismiss()
        }

        // Close button always dismisses
        closeButton.setOnClickListener { dismiss() }
    }

    companion object {
        /**
         * Show a popup ad dialog for the given [ad].
         *
         * @param context  Activity context (required for Dialog).
         * @param ad       The resolved [AdItem] to display.
         */
        fun show(context: Context, ad: AdItem) {
            PopupAdDialog(context, ad).show()
        }
    }
}
