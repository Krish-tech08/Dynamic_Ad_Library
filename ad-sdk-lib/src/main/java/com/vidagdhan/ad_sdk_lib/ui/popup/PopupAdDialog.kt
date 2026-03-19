package com.vidagdhan.ad_sdk_lib.ui.popup

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.vidagdhan.ad_sdk_lib.R
import com.vidagdhan.ad_sdk_lib.data.model.AdItem

class PopupAdDialog private constructor(
    context: Context,
    private val ad: AdItem
) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set popup to 90% of screen width
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_popup_ad, null)
        setContentView(view)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val adImage    : ImageView   = view.findViewById(R.id.popupAdImage)
        val closeButton: ImageButton = view.findViewById(R.id.btnClosePopup)

        Glide.with(context)
            .load(ad.image_url)
            .placeholder(R.drawable.ad_placeholder)
            .error(R.drawable.ad_placeholder)
            .into(adImage)

        adImage.setOnClickListener {
            ad.cta?.let { url ->
                if (url.isNotEmpty()) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            dismiss()
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_UP) {
                dismiss()
                true
            } else {
                false
            }
        }
    }

    companion object {
        fun show(context: Context, ad: AdItem) {
            PopupAdDialog(context, ad).show()
        }
    }
}
