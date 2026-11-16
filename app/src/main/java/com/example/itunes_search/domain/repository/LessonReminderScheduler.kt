package com.example.itunes_search.domain.repository

data class LessonReminder(val fullName: String, val triggerAtMillis: Long)

class LessonReminderTimePassedException : IllegalArgumentException("Lesson time has already passed")

interface LessonReminderScheduler {
    fun schedule(reminder: LessonReminder)
}
