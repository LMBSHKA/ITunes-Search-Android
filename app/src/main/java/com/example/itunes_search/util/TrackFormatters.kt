package com.example.itunes_search.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

fun formatDuration(milliseconds: Long?): String {
    if (milliseconds == null || milliseconds < 0) return "—"
    val totalSeconds = milliseconds / 1000
    return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}

fun formatReleaseDate(releaseDate: String?): String {
    if (releaseDate.isNullOrBlank()) return "—"
    val datePart = releaseDate.take(10)
    if (!datePart.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return "—"

    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val date = parser.parse(datePart) ?: return "—"
        SimpleDateFormat("dd.MM.yyyy", Locale.US).format(date)
    } catch (_: java.text.ParseException) {
        "—"
    }
}

fun formatPrice(price: Double?, currencyCode: String?): String {
    if (price == null || price < 0 || !price.isFinite()) return "—"
    if (currencyCode.isNullOrBlank()) return String.format(Locale.US, "%.2f", price)

    return try {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance(currencyCode)
        }.format(price)
    } catch (_: IllegalArgumentException) {
        String.format(Locale.US, "%.2f %s", price, currencyCode)
    }
}
