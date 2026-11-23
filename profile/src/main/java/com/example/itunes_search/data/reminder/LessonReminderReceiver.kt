package com.example.itunes_search.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LessonReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmLessonReminderScheduler.ACTION_REMIND) return
        val scheduler = AlarmLessonReminderScheduler(context)
        val reminder = scheduler.pendingReminder() ?: return
        // Игнорируем старый broadcast, если напоминание уже перенесено на более позднее время.
        if (reminder.triggerAtMillis > System.currentTimeMillis()) return
        LessonReminderNotifications.show(context, reminder)
        scheduler.markDelivered(reminder)
    }
}
