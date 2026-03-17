package com.vidagdhan.dynamicadsdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vidagdhan.VidagdhanAds
import com.vidagdhan.dynamicadsdk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Show banner ONLY after sync completes
        VidagdhanAds.syncAds { success ->
            Log.d("VidagdhanAds", "syncAds callback → success=$success")
            if (success) {
                VidagdhanAds.showBanner(
                    activity  = this,
                    container = binding.programmaticBannerContainer,
                    screenId  = "home_screen"
                )
            } else {
                Log.w("VidagdhanAds", "Sync failed — trying cache anyway")
                // Try cache even if sync failed
                VidagdhanAds.showBanner(
                    activity  = this,
                    container = binding.programmaticBannerContainer,
                    screenId  = "home_screen"
                )
            }
        }

        binding.btnShowPopup.setOnClickListener {
            VidagdhanAds.showPopup(
                activity = this,
                screenId = "exit_screen"
            )
        }

        binding.btnRefreshAds.setOnClickListener {
            VidagdhanAds.syncAds { success ->
                VidagdhanAds.showBanner(
                    activity  = this,
                    container = binding.programmaticBannerContainer,
                    screenId  = "home_screen"
                )
            }
        }
    }
}
