package com.example.itunes_search.domain.model

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class LessonTime private constructor(val hour: Int, val minute: Int) {
    fun todayAt(nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long =
        Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    companion object {
        private val pattern = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")

        fun parse(value: String): LessonTime? {
            if (!pattern.matches(value)) return null
            return LessonTime(value.substring(0, 2).toInt(), value.substring(3, 5).toInt())
        }

        fun format(hour: Int, minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
    }
}
