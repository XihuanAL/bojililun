package com.example.boji.util

import android.content.Context
import com.example.boji.R
import kotlin.random.Random

/**
 * 文案读取器：所有文案都来自 res/values/copy_library.xml。
 * 想改文案只改那个 XML，不用碰这里。
 *
 * 为避免连续看到同一条，同一进程内会记住上一次的下标并尽量避开。
 */
object Copy {

    private val lastIndex = mutableMapOf<String, Int>()

    private fun pick(context: Context, arrayRes: Int, key: String): String {
        val arr = context.resources.getStringArray(arrayRes)
        if (arr.isEmpty()) return ""
        if (arr.size == 1) return arr[0]
        var idx = Random.nextInt(arr.size)
        if (idx == lastIndex[key]) idx = (idx + 1 + Random.nextInt(arr.size - 1)) % arr.size
        lastIndex[key] = idx
        return arr[idx]
    }

    private fun fill(
        text: String,
        nick: String = "预备役",
        done: Int = 0,
        goal: Int = 0,
        time: String = "",
        min: Int = 0,
        week: String = ""
    ): String = text
        .replace("{nick}", nick)
        .replace("{done}", done.toString())
        .replace("{goal}", goal.toString())
        .replace("{ml}", (goal - done).coerceAtLeast(0).toString())
        .replace("{time}", time)
        .replace("{min}", min.toString())
        .replace("{week}", week)

    fun waterReminder(c: Context, nick: String, done: Int, goal: Int): String =
        fill(pick(c, R.array.water_reminders, "water"), nick, done, goal)

    fun waterDone(c: Context, nick: String, done: Int, goal: Int): String =
        fill(pick(c, R.array.water_done, "water_done"), nick, done, goal)

    fun waterEmpty(c: Context): String =
        fill(pick(c, R.array.water_empty, "water_empty"))

    fun sleepReminder(c: Context, nick: String, time: String, min: Int): String =
        fill(pick(c, R.array.sleep_reminders, "sleep"), nick, time = time, min = min)

    fun sleepFollowUp(c: Context, nick: String): String =
        fill(pick(c, R.array.sleep_followup, "sleep_follow"), nick)

    fun sleepDone(c: Context, nick: String): String =
        fill(pick(c, R.array.sleep_done, "sleep_done"), nick)

    fun sleepMiss(c: Context, nick: String): String =
        fill(pick(c, R.array.sleep_miss, "sleep_miss"), nick)

    fun sleepEmpty(c: Context): String =
        fill(pick(c, R.array.sleep_empty, "sleep_empty"))

    fun waterQuote(c: Context): String =
        fill(pick(c, R.array.water_quotes, "water_page"))

    fun sleepQuote(c: Context): String =
        fill(pick(c, R.array.sleep_quotes, "sleep_page"))

    fun muscleQuote(c: Context): String =
        fill(pick(c, R.array.muscle_quotes, "muscle"))

    fun muscleReminder(c: Context, nick: String, week: String): String =
        fill(pick(c, R.array.muscle_reminders, "muscle_remind"), nick, week = week)

    fun overviewOpener(c: Context, nick: String): String =
        fill(pick(c, R.array.overview_openers, "opener"), nick)
}
