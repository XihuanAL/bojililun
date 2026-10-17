package com.example.boji.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.example.boji.MainActivity
import com.example.boji.R
import com.example.boji.data.Settings
import com.example.boji.ui.theme.Muscle
import com.example.boji.ui.theme.Sleep
import com.example.boji.ui.theme.Water
import com.example.boji.util.Copy
import com.example.boji.util.formatMinute

object Notifier {

    const val CH_WATER = "boji_water"
    const val CH_SLEEP = "boji_sleep"
    const val CH_MUSCLE = "boji_muscle"

    private const val ID_WATER = 2001
    private const val ID_SLEEP = 2002
    private const val ID_SLEEP_FOLLOW = 2003
    private const val ID_MUSCLE = 2004

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        listOf(
            NotificationChannel(CH_WATER, "大水理论", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "喝水提醒"
            },
            NotificationChannel(CH_SLEEP, "大睡理论", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "早睡提醒"
            },
            NotificationChannel(CH_MUSCLE, "薄肌理论", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "健身日激励提醒"
            }
        ).forEach { nm.createNotificationChannel(it) }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun canNotify(context: Context): Boolean =
        hasNotificationPermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun contentIntent(context: Context, tab: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TAB, tab)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getActivity(context, tab.hashCode(), intent, flags)
    }

    private fun build(
        context: Context,
        channel: String,
        title: String,
        text: String,
        tab: String,
        color: Int
    ) = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_stat_boji)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setColor(color)
        .setAutoCancel(true)
        .setContentIntent(contentIntent(context, tab))
        .build()

    private fun notify(context: Context, id: Int, notification: android.app.Notification) {
        if (!canNotify(context)) return
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    fun showWater(context: Context, s: Settings, done: Int) {
        val text = Copy.waterReminder(context, s.nickname, done, s.waterGoalMl)
        notify(
            context, ID_WATER,
            build(context, CH_WATER, "大水理论 · 该补水了", text, MainActivity.TAB_WATER, Water.toArgb())
        )
    }

    fun showSleep(context: Context, s: Settings, followUp: Boolean = false) {
        val time = formatMinute(s.sleepTargetMin)
        val text = if (followUp) {
            Copy.sleepFollowUp(context, s.nickname)
        } else {
            Copy.sleepReminder(context, s.nickname, time, s.sleepLeadMin)
        }
        notify(
            context, if (followUp) ID_SLEEP_FOLLOW else ID_SLEEP,
            build(
                context, CH_SLEEP,
                if (followUp) "大睡理论 · 最后通牒" else "大睡理论 · 该睡了",
                text, MainActivity.TAB_SLEEP, Sleep.toArgb()
            )
        )
    }

    fun showMuscle(context: Context, s: Settings, weekLabel: String) {
        val text = Copy.muscleReminder(context, s.nickname, weekLabel)
        notify(
            context, ID_MUSCLE,
            build(context, CH_MUSCLE, "薄肌理论 · 今天是训练日", text, MainActivity.TAB_MUSCLE, Muscle.toArgb())
        )
    }

}
