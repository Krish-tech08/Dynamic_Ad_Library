package com.vidagdhan.ad_sdk_lib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AdNetworkClient {

    fun create(baseUrl: String, enableLogging: Boolean = false): AdApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (enableLogging)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        // Base URL must end with "/" and only contain the domain
        // e.g. "https://vidagdhan.com/" NOT "https://vidagdhan.com/app_ads/ads.json"
        val normalised = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalised)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdApiService::class.java)
    }
}
