package com.example.itunes_search.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.itunes_search.MainActivity
import com.example.itunes_search.R
import com.example.itunes_search.domain.repository.LessonReminder

internal object LessonReminderNotifications {
    const val CHANNEL_ID = "favorite_lesson"
    private const val NOTIFICATION_ID = 7001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.lesson_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.lesson_notification_channel_description) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun areEnabled(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = context.getSystemService(NotificationManager::class.java)
                .getNotificationChannel(CHANNEL_ID)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    fun show(context: Context, reminder: LessonReminder) {
        createChannel(context)
        if (!LessonReminderAccess(context).notificationsEnabled()) return
        // Activity открывается напрямую: notification trampoline не используется.
        val openApp = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val name = reminder.fullName.ifBlank { context.getString(R.string.profile_name_empty) }
        val text = context.getString(R.string.lesson_notification_text, name)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lesson_notification)
            .setContentTitle(context.getString(R.string.lesson_notification_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        // Разрешение может быть отозвано между проверкой и notify.
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // После отзыва разрешения Android запрещает показ уведомления.
        }
    }
}
