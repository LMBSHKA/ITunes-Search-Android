package com.example.itunes_search.domain

import com.example.itunes_search.domain.model.LessonTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class LessonTimeTest {
    @Test fun everyMinuteIn24HourFormatIsAccepted() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val parsed = LessonTime.parse(LessonTime.format(hour, minute))
                assertNotNull(parsed)
                assertEquals(hour, parsed!!.hour)
                assertEquals(minute, parsed.minute)
            }
        }
    }

    @Test fun emptyMalformedAndOutOfRangeTimesAreRejected() {
        listOf("", " ", "9:05", "09:5", "24:00", "23:60", "-1:00", "12:000", "12.30",
            "12:30:00", " 12:30", "12:30 ", "ab:cd", "１２:３０", "12:30\n").forEach {
            assertNull("Must reject: $it", LessonTime.parse(it))
        }
    }

    @Test fun triggerUsesTodayInTheDeviceTimeZoneAndClearsSeconds() {
        val zone = TimeZone.getTimeZone("Asia/Yekaterinburg")
        val now = Calendar.getInstance(zone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 16, 10, 15, 47)
            set(Calendar.MILLISECOND, 123)
        }
        val trigger = Calendar.getInstance(zone).apply {
            timeInMillis = LessonTime.parse("18:05")!!.todayAt(now.timeInMillis, zone)
        }
        assertEquals(2026, trigger.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, trigger.get(Calendar.MONTH))
        assertEquals(16, trigger.get(Calendar.DAY_OF_MONTH))
        assertEquals(18, trigger.get(Calendar.HOUR_OF_DAY))
        assertEquals(5, trigger.get(Calendar.MINUTE))
        assertEquals(0, trigger.get(Calendar.SECOND))
        assertEquals(0, trigger.get(Calendar.MILLISECOND))
    }

    @Test fun pastTimeStaysTodayInsteadOfRollingToTomorrow() {
        val zone = TimeZone.getTimeZone("UTC")
        val now = Calendar.getInstance(zone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 16, 15, 0, 0)
        }.timeInMillis
        val trigger = LessonTime.parse("09:00")!!.todayAt(now, zone)
        assertTrue(trigger < now)
        assertEquals(6 * 60 * 60 * 1000L, now - trigger)
    }
}
