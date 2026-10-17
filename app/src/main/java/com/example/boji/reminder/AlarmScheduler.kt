package com.example.boji.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.boji.data.BojiRepository
import com.example.boji.data.Records
import com.example.boji.data.Settings
import com.example.boji.util.WEEKDAY_LABEL
import com.example.boji.util.WEEK_ORDER
import com.example.boji.util.todayKey
import com.example.boji.util.todayWeekday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * 提醒调度：全部使用 AlarmManager 的精确闹钟（setExactAndAllowWhileIdle）。
 *
 * 由于精确闹钟是一次性的，每次触发后都要自己算下一次：
 *   触发时（ReminderReceiver）重排 + 开机/升级/时间变更（BootReceiver）重排 + App 启动重排。
 */
object AlarmScheduler {

    const val EXTRA_TYPE = "boji_reminder_type"
    const val TYPE_WATER = "water"
    const val TYPE_SLEEP = "sleep"
    const val TYPE_SLEEP_FOLLOW = "sleep_follow"
    const val TYPE_MUSCLE = "muscle"

    private const val REQ_WATER = 3001
    private const val REQ_SLEEP = 3002
    private const val REQ_SLEEP_FOLLOW = 3003
    private const val REQ_MUSCLE = 3004

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun canUseExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager(context).canScheduleExactAlarms()
    }

    private fun pendingIntent(context: Context, reqCode: Int, type: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_TYPE, type)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getBroadcast(context, reqCode, intent, flags)
    }

    private fun set(context: Context, reqCode: Int, type: String, triggerAt: Long) {
        val am = alarmManager(context)
        val pi = pendingIntent(context, reqCode, type)
        if (canUseExact(context)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // 没有精确闹钟权限时退化为「不早于该时刻」，保证仍能提醒
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancel(context: Context, reqCode: Int, type: String) {
        alarmManager(context).cancel(pendingIntent(context, reqCode, type))
    }

    // ------------------------------------------------------------------
    // 对外入口
    // ------------------------------------------------------------------

    /** 重新注册全部提醒（开机、App 启动、设置变更后调用）。同步版本，供 BroadcastReceiver 使用。 */
    fun rescheduleAll(context: Context) {
        runBlocking(Dispatchers.IO) { rescheduleAllSuspend(context) }
    }

    /** 挂起版本，供在协程里调用 */
    suspend fun rescheduleAllSuspend(context: Context) {
        val app = context.applicationContext
        val repo = BojiRepository(app)
        val s = repo.settingsSnapshot()
        val r = repo.recordsSnapshot()
        scheduleWater(app, s, r)
        scheduleSleep(app, s)
        scheduleMuscle(app, s)
    }

    // ------------------------------------------------------------------
    // 各类提醒的排期算法
    // ------------------------------------------------------------------

    private fun todayAt(minuteOfDay: Int, nowMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
        cal.set(Calendar.MINUTE, minuteOfDay % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun tomorrowAt(minuteOfDay: Int, nowMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = todayAt(minuteOfDay, nowMillis) }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    fun scheduleWater(context: Context, s: Settings, r: Records) {
        if (!s.waterEnabled) {
            cancel(context, REQ_WATER, TYPE_WATER)
            return
        }
        val now = System.currentTimeMillis()
        val done = r.waterByDate[todayKey()] ?: 0

        // 已达成目标：今天不再提醒，直接排到明天的开始时间
        if (done >= s.waterGoalMl) {
            set(context, REQ_WATER, TYPE_WATER, tomorrowAt(s.waterStartMin, now))
            return
        }

        val start = todayAt(s.waterStartMin, now)
        val end = todayAt(s.waterEndMin, now)
        val trigger = when {
            now < start -> start
            now >= end -> tomorrowAt(s.waterStartMin, now)
            else -> {
                val next = now + s.waterIntervalMin * 60_000L
                if (next >= end) tomorrowAt(s.waterStartMin, now) else next
            }
        }
        set(context, REQ_WATER, TYPE_WATER, trigger)
    }

    fun scheduleSleep(context: Context, s: Settings) {
        if (!s.sleepEnabled) {
            cancel(context, REQ_SLEEP, TYPE_SLEEP)
            cancel(context, REQ_SLEEP_FOLLOW, TYPE_SLEEP_FOLLOW)
            return
        }
        val now = System.currentTimeMillis()
        val leadMin = (s.sleepTargetMin - s.sleepLeadMin + 1440) % 1440
        var trigger = todayAt(leadMin, now)
        if (trigger <= now) trigger = tomorrowAt(leadMin, now)

        set(context, REQ_SLEEP, TYPE_SLEEP, trigger)

        // 追加提醒落在目标就寝时刻（= 首次提醒 + 提前量），提前量为 0 时不重复发
        if (s.sleepFollowEnabled && s.sleepLeadMin > 0) {
            set(context, REQ_SLEEP_FOLLOW, TYPE_SLEEP_FOLLOW, trigger + s.sleepLeadMin * 60_000L)
        } else {
            cancel(context, REQ_SLEEP_FOLLOW, TYPE_SLEEP_FOLLOW)
        }
    }

    fun scheduleMuscle(context: Context, s: Settings) {
        if (!s.muscleEnabled || s.muscleDays.isEmpty()) {
            cancel(context, REQ_MUSCLE, TYPE_MUSCLE)
            return
        }
        val now = System.currentTimeMillis()
        for (offset in 0..7) {
            val cal = Calendar.getInstance().apply { timeInMillis = todayAt(s.muscleTimeMin, now) }
            cal.add(Calendar.DAY_OF_YEAR, offset)
            if (cal.timeInMillis > now && cal.get(Calendar.DAY_OF_WEEK) in s.muscleDays) {
                set(context, REQ_MUSCLE, TYPE_MUSCLE, cal.timeInMillis)
                return
            }
        }
        cancel(context, REQ_MUSCLE, TYPE_MUSCLE)
    }

    /** 给通知用的星期名 */
    fun weekLabel(): String = WEEKDAY_LABEL[todayWeekday()] ?: "今天"

    /** 今天是健身日吗 */
    fun isMuscleDay(s: Settings): Boolean = todayWeekday() in s.muscleDays

    /** 明天是不是健身日（用于总览页的副文案） */
    fun nextMuscleDayGap(s: Settings): Int {
        if (s.muscleDays.isEmpty()) return -1
        val today = todayWeekday()
        val order = WEEK_ORDER
        val idx = order.indexOf(today)
        for (step in 0..7) {
            val d = order[(idx + step) % 7]
            if (d in s.muscleDays) return step
        }
        return -1
    }
}
