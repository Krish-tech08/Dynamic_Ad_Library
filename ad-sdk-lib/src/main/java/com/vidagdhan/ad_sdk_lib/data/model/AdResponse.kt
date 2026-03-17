package com.vidagdhan.ad_sdk_lib.data.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level wrapper returned by the ads API.
 *
 * Example JSON:
 * {
 *   "status": true,
 *   "data": [ { … } ]
 * }
 */
data class AdResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data")   val data: List<AdItemRaw>
)

/**
 * Raw API shape before we enrich it with a deterministic [AdItem.id].
 * Kept separate so Room's @Entity never receives arbitrary network fields.
 */
data class AdItemRaw(
    @SerializedName("app_id")     val app_id: String,
    @SerializedName("screen_id")  val screen_id: String,
    @SerializedName("type")       val type: String,
    @SerializedName("image_url")  val image_url: String,
    @SerializedName("frequency")  val frequency: Int,
    @SerializedName("start_date") val start_date: String,
    @SerializedName("end_date")   val end_date: String,
    @SerializedName("cta")        val cta: String? = null
) {
    /** Convert to the Room-ready [AdItem] with a stable composite ID. */
    fun toAdItem(): AdItem = AdItem(
        id = "${app_id}_${screen_id}_${image_url}".hashCode().toString(),
        app_id = app_id,
        screen_id = screen_id,
        type = type,
        image_url = image_url,
        frequency = frequency,
        start_date = start_date,
        end_date = end_date,
        cta = cta
    )
}
