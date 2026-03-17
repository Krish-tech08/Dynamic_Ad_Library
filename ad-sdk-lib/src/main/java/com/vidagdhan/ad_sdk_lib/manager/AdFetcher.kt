package com.vidagdhan.ad_sdk_lib.manager

import android.util.Log
import com.vidagdhan.ad_sdk_lib.api.AdApiService
import com.vidagdhan.ad_sdk_lib.data.model.AdItem
import com.vidagdhan.ad_sdk_lib.data.model.AdResponse
import com.vidagdhan.ad_sdk_lib.util.AdDateUtils

class AdFetcher(
    private val apiService   : AdApiService,
    private val applicationId: String
) {
    companion object {
        private const val TAG = "VidagdhanAds"
    }

    suspend fun fetchAndFilter(): Result<List<AdItem>> {
        return try {
            Log.d(TAG, "fetchAndFilter() → calling API with app_id=$applicationId")

            // Pass applicationId as query param — backend filters by app_id
            val body: AdResponse = apiService.fetchAds(applicationId)

            Log.d(TAG, "fetchAndFilter() → status=${body.status} totalAds=${body.data.size}")

            if (!body.status) {
                Log.w(TAG, "fetchAndFilter() → ❌ Backend returned status=false")
                return Result.success(emptyList())
            }

            body.data.forEachIndexed { i, raw ->
                Log.d(TAG, "  [Ad $i] app_id=${raw.app_id} screen=${raw.screen_id} " +
                    "type=${raw.type} start=${raw.start_date} end=${raw.end_date}")
            }

            val filtered = body.data
                .filter { raw ->
                    val appMatch  = raw.app_id == applicationId
                    val dateValid = AdDateUtils.isAdActive(raw.start_date, raw.end_date)
                    Log.d(TAG, "  filter → appMatch=$appMatch dateValid=$dateValid " +
                        "(got=${raw.app_id} want=$applicationId)")
                    appMatch && dateValid
                }
                .map { it.toAdItem() }

            Log.d(TAG, "fetchAndFilter() → ✅ ${filtered.size} ads passed filter")
            Result.success(filtered)

        } catch (e: Exception) {
            Log.e(TAG, "fetchAndFilter() → ❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
