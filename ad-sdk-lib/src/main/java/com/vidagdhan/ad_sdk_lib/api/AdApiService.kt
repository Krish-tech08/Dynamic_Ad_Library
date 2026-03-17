package com.vidagdhan.ad_sdk_lib.api

import com.vidagdhan.ad_sdk_lib.data.model.AdResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AdApiService {

    @GET("ads.php")
    suspend fun fetchAds(
        @Query("app_id") appId: String
    ): AdResponse
}
