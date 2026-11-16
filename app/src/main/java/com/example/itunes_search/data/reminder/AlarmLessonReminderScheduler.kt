package com.example.itunes_search.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.itunes_search.domain.repository.LessonReminder
import com.example.itunes_search.domain.repository.LessonReminderScheduler
import com.example.itunes_search.domain.repository.LessonReminderTimePassedException
import java.io.IOException

class AlarmLessonReminderScheduler(context: Context) : LessonReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val preferences = appContext.getSharedPreferences("lesson_reminder", Context.MODE_PRIVATE)
    private val access = LessonReminderAccess(appContext)

    override fun schedule(reminder: LessonReminder) {
        if (reminder.triggerAtMillis <= System.currentTimeMillis()) throw LessonReminderTimePassedException()
        if (!access.notificationsEnabled() || !access.canScheduleExactAlarms()) {
            throw SecurityException("Notification and exact alarm permissions are required")
        }
        val previous = pendingReminder()
        writeReminder(reminder)
        try {
            setAlarm(reminder)
        } catch (exception: Exception) {
            writeReminder(previous)
            throw exception
        }
    }

    fun cancel() {
        alarmManager.cancel(alarmIntent())
        writeReminder(null)
    }

    internal fun pendingReminder(): LessonReminder? {
        val triggerAt = preferences.getLong(TRIGGER_AT, 0L)
        if (triggerAt == 0L) return null
        return LessonReminder(preferences.getString(FULL_NAME, "").orEmpty(), triggerAt)
    }

    internal fun restore() {
        val reminder = pendingReminder() ?: return
        if (reminder.triggerAtMillis <= System.currentTimeMillis()) {
            writeReminder(null)
            return
        }
        if (access.canScheduleExactAlarms() && access.notificationsEnabled()) {
            try {
                setAlarm(reminder)
            } catch (_: SecurityException) {
                // Пользователь мог отозвать доступ во время обработки системного broadcast.
            }
        }
    }

    internal fun markDelivered(reminder: LessonReminder) {
        if (pendingReminder() == reminder) writeReminder(null)
    }

    private fun setAlarm(reminder: LessonReminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            throw SecurityException("Exact alarm permission was revoked")
        }
        // Один PendingIntent заменяет предыдущее напоминание при повторном сохранении.
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAtMillis, alarmIntent())
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        REQUEST_CODE,
        Intent(appContext, LessonReminderReceiver::class.java).setAction(ACTION_REMIND),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun writeReminder(reminder: LessonReminder?) {
        val editor = preferences.edit()
        if (reminder == null) {
            editor.remove(TRIGGER_AT).remove(FULL_NAME)
        } else {
            editor.putLong(TRIGGER_AT, reminder.triggerAtMillis).putString(FULL_NAME, reminder.fullName)
        }
        if (!editor.commit()) throw IOException("Cannot save lesson reminder")
    }

    companion object {
        internal const val ACTION_REMIND = "com.example.itunes_search.action.LESSON_REMINDER"
        private const val REQUEST_CODE = 7001
        private const val TRIGGER_AT = "trigger_at"
        private const val FULL_NAME = "full_name_at_scheduling"
    }
}
