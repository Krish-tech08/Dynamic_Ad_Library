package com.vidagdhan.ad_sdk_lib.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AdDateUtils — pure utility object for date-range validation.
 *
 * Keeps all date logic in one place so it is easy to unit-test and change.
 */
object AdDateUtils {

    /** Format that matches the backend: "2026-03-16 00:00:00" */
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Returns true if the current wall-clock time falls inside
     * [startDateStr, endDateStr] (both ends inclusive).
     *
     * Any parse failure causes the ad to be **excluded** — fail-safe.
     */
    fun isAdActive(startDateStr: String, endDateStr: String): Boolean {
        return try {
            val now   : Date = Date()
            val start : Date = DATE_FORMAT.parse(startDateStr) ?: return false
            val end   : Date = DATE_FORMAT.parse(endDateStr)   ?: return false
            now in start..end
        } catch (e: Exception) {
            false   // unparseable date → treat ad as inactive
        }
    }

    /**
     * Kotlin range operator for [Date] objects.
     * Usage: `date in start..end`
     */
    private operator fun Date.rangeTo(other: Date): ClosedRange<Date> =
        object : ClosedRange<Date> {
            override val endInclusive: Date   = other
            override val start       : Date   = this@rangeTo
        }
}
