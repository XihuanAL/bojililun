package com.example.boji.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 一天的分钟数表示法：0 ~ 1439。例如 23:00 -> 1380 */
private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

fun todayKey(): String = DATE_FMT.format(Date())

fun dateKeyOf(cal: Calendar): String = DATE_FMT.format(cal.time)

fun dateKeyOf(millis: Long): String = DATE_FMT.format(Date(millis))

/** 在给定日期上加减天数，返回 yyyy-MM-dd */
fun shiftDateKey(baseKey: String, days: Int): String {
    val cal = Calendar.getInstance()
    cal.time = requireNotNull(DATE_FMT.parse(baseKey))
    cal.add(Calendar.DAY_OF_YEAR, days)
    return DATE_FMT.format(cal.time)
}

fun formatMinute(minuteOfDay: Int): String {
    val m = ((minuteOfDay % 1440) + 1440) % 1440
    return String.format(Locale.CHINA, "%02d:%02d", m / 60, m % 60)
}

fun parseMinute(hour: Int, minute: Int): Int = hour * 60 + minute

/** 把「今天的第 minuteOfDay 分钟」转成毫秒时间戳；若已过去则顺延到明天 */
fun millisForTodayOrNext(minuteOfDay: Int, nowMillis: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    cal.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
    cal.set(Calendar.MINUTE, minuteOfDay % 60)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (cal.timeInMillis <= nowMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
    return cal.timeInMillis
}

/** 取得今天是星期几（Calendar.MONDAY=2 ... SUNDAY=1） */
fun todayWeekday(nowMillis: Long = System.currentTimeMillis()): Int {
    return Calendar.getInstance().apply { timeInMillis = nowMillis }.get(Calendar.DAY_OF_WEEK)
}

val WEEKDAY_LABEL = mapOf(
    Calendar.SUNDAY to "周日",
    Calendar.MONDAY to "周一",
    Calendar.TUESDAY to "周二",
    Calendar.WEDNESDAY to "周三",
    Calendar.THURSDAY to "周四",
    Calendar.FRIDAY to "周五",
    Calendar.SATURDAY to "周六"
)

/** 顺序展示用的周一到周日 */
val WEEK_ORDER = listOf(
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY,
    Calendar.SUNDAY
)

fun weekdayShort(day: Int): String = WEEKDAY_LABEL[day]?.takeLast(1) ?: "?"
