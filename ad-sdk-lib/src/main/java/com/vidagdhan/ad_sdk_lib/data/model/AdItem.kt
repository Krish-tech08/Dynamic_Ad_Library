package com.vidagdhan.ad_sdk_lib.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AdItem — core data model that doubles as the Room Entity.
 * Maps 1-to-1 to a single object inside the backend "data" array.
 */
@Entity(tableName = "ads")
data class AdItem(

    /**
     * Stable deterministic ID derived from app_id + screen_id + image_url.
     * Prevents duplicate rows on repeated fetches.
     */
    @PrimaryKey
    val id: String,

    /** Package name this ad belongs to, e.g. "com.vidagdhan.bulk_dialer" */
    val app_id: String,

    /**
     * Logical placement key supplied by the backend.
     * The SDK never interprets or hardcodes this value —
     * the host app passes whatever string it uses.
     */
    val screen_id: String,

    /** "banner" or "popup" */
    val type: String,

    /** Remote URL of the ad creative */
    val image_url: String,

    /**
     * Show this ad every Nth impression for this screen.
     * frequency = 3  →  visible on impressions 3, 6, 9, …
     */
    val frequency: Int,

    /** Backend datetime string: "2026-03-16 00:00:00" */
    val start_date: String,

    /** Backend datetime string: "2026-03-31 23:59:00" */
    val end_date: String,

    /** Click-through URL; null when backend omits it */
    val cta: String? = null
)
