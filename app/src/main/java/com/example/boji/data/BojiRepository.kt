package com.example.boji.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.boji.ui.theme.ThemeMode
import com.example.boji.util.shiftDateKey
import com.example.boji.util.todayKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Calendar

private val Context.bojiDataStore: DataStore<Preferences> by preferencesDataStore(name = "boji_prefs")

private object K {
    val NICKNAME = stringPreferencesKey("nickname")
    val THEME_MODE = stringPreferencesKey("theme_mode")

    val WATER_ENABLED = booleanPreferencesKey("water_enabled")
    val WATER_GOAL = intPreferencesKey("water_goal_ml")
    val WATER_START = intPreferencesKey("water_start_min")
    val WATER_END = intPreferencesKey("water_end_min")
    val WATER_INTERVAL = intPreferencesKey("water_interval_min")
    val WATER_MAP = stringPreferencesKey("water_map")

    val SLEEP_ENABLED = booleanPreferencesKey("sleep_enabled")
    val SLEEP_TARGET = intPreferencesKey("sleep_target_min")
    val SLEEP_LEAD = intPreferencesKey("sleep_lead_min")
    val SLEEP_FOLLOW_ENABLED = booleanPreferencesKey("sleep_follow_enabled")
    val SLEEP_MAP = stringPreferencesKey("sleep_map")

    val MUSCLE_ENABLED = booleanPreferencesKey("muscle_enabled")
    val MUSCLE_DAYS = stringPreferencesKey("muscle_days")
    val MUSCLE_TIME = intPreferencesKey("muscle_time_min")

}

data class Settings(
    val nickname: String = "预备役",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    val waterEnabled: Boolean = true,
    val waterGoalMl: Int = 2000,
    val waterStartMin: Int = 8 * 60,
    val waterEndMin: Int = 22 * 60,
    val waterIntervalMin: Int = 60,

    val sleepEnabled: Boolean = true,
    val sleepTargetMin: Int = 23 * 60,
    val sleepLeadMin: Int = 30,
    val sleepFollowEnabled: Boolean = true,

    val muscleEnabled: Boolean = true,
    val muscleDays: Set<Int> = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY),
    val muscleTimeMin: Int = 19 * 60
)

data class Records(
    /** yyyy-MM-dd -> 当天已喝水量(ml) */
    val waterByDate: Map<String, Int> = emptyMap(),
    /** 入睡当晚的日期 yyyy-MM-dd -> 是否按时睡 */
    val sleepByDate: Map<String, Boolean> = emptyMap()
)

private const val MAX_KEEP_DAYS = 60

class BojiRepository(private val context: Context) {

    val settings: Flow<Settings> = context.bojiDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            Settings(
                nickname = p[K.NICKNAME] ?: "预备役",
                themeMode = runCatching { ThemeMode.valueOf(p[K.THEME_MODE] ?: "SYSTEM") }
                    .getOrDefault(ThemeMode.SYSTEM),
                waterEnabled = p[K.WATER_ENABLED] ?: true,
                waterGoalMl = p[K.WATER_GOAL] ?: 2000,
                waterStartMin = p[K.WATER_START] ?: (8 * 60),
                waterEndMin = p[K.WATER_END] ?: (22 * 60),
                waterIntervalMin = p[K.WATER_INTERVAL] ?: 60,
                sleepEnabled = p[K.SLEEP_ENABLED] ?: true,
                sleepTargetMin = p[K.SLEEP_TARGET] ?: (23 * 60),
                sleepLeadMin = p[K.SLEEP_LEAD] ?: 30,
                sleepFollowEnabled = p[K.SLEEP_FOLLOW_ENABLED] ?: true,
                muscleEnabled = p[K.MUSCLE_ENABLED] ?: true,
                muscleDays = parseDays(p[K.MUSCLE_DAYS]),
                muscleTimeMin = p[K.MUSCLE_TIME] ?: (19 * 60)
            )
        }

    val records: Flow<Records> = context.bojiDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            Records(
                waterByDate = parseIntMap(p[K.WATER_MAP]),
                sleepByDate = parseBoolMap(p[K.SLEEP_MAP])
            )
        }

    /** 供 BroadcastReceiver / 开机重注册等同步场景使用 */
    suspend fun settingsSnapshot(): Settings = settings.first()
    suspend fun recordsSnapshot(): Records = records.first()

    // ---------------- 写操作 ----------------

    suspend fun setNickname(v: String) = edit { it[K.NICKNAME] = v.ifBlank { "预备役" } }
    suspend fun setThemeMode(v: ThemeMode) = edit { it[K.THEME_MODE] = v.name }

    suspend fun setWaterEnabled(v: Boolean) = edit { it[K.WATER_ENABLED] = v }
    suspend fun setWaterGoal(v: Int) = edit { it[K.WATER_GOAL] = v.coerceIn(200, 10000) }
    suspend fun setWaterStart(v: Int) = edit { it[K.WATER_START] = v.coerceIn(0, 1439) }
    suspend fun setWaterEnd(v: Int) = edit { it[K.WATER_END] = v.coerceIn(0, 1439) }
    suspend fun setWaterInterval(v: Int) = edit { it[K.WATER_INTERVAL] = v.coerceIn(15, 480) }

    suspend fun setSleepEnabled(v: Boolean) = edit { it[K.SLEEP_ENABLED] = v }
    suspend fun setSleepTarget(v: Int) = edit { it[K.SLEEP_TARGET] = v.coerceIn(0, 1439) }
    suspend fun setSleepLead(v: Int) = edit { it[K.SLEEP_LEAD] = v.coerceIn(0, 180) }
    suspend fun setSleepFollowEnabled(v: Boolean) = edit { it[K.SLEEP_FOLLOW_ENABLED] = v }

    suspend fun setMuscleEnabled(v: Boolean) = edit { it[K.MUSCLE_ENABLED] = v }
    suspend fun setMuscleDays(v: Set<Int>) = edit { it[K.MUSCLE_DAYS] = v.joinToString(",") }
    suspend fun setMuscleTime(v: Int) = edit { it[K.MUSCLE_TIME] = v.coerceIn(0, 1439) }

    suspend fun addWater(ml: Int) {
        val key = todayKey()
        edit { p ->
            val map = parseIntMap(p[K.WATER_MAP]).toMutableMap()
            map[key] = (map[key] ?: 0) + ml
            p[K.WATER_MAP] = serializeIntMap(map.trimOld())
        }
    }

    suspend fun undoWater(ml: Int) {
        val key = todayKey()
        edit { p ->
            val map = parseIntMap(p[K.WATER_MAP]).toMutableMap()
            val cur = map[key] ?: 0
            val next = (cur - ml).coerceAtLeast(0)
            if (next == 0) map.remove(key) else map[key] = next
            p[K.WATER_MAP] = serializeIntMap(map.trimOld())
        }
    }

    suspend fun resetTodayWater() {
        val key = todayKey()
        edit { p ->
            val map = parseIntMap(p[K.WATER_MAP]).toMutableMap()
            map.remove(key)
            p[K.WATER_MAP] = serializeIntMap(map.trimOld())
        }
    }

    /** dateKey = 入睡当晚的日期 */
    suspend fun setSleepResult(dateKey: String, onTime: Boolean) {
        edit { p ->
            val map = parseBoolMap(p[K.SLEEP_MAP]).toMutableMap()
            map[dateKey] = onTime
            p[K.SLEEP_MAP] = serializeBoolMap(map.trimOldBool())
        }
    }

    suspend fun clearAllData() {
        edit { p ->
            p.clear()
        }
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.bojiDataStore.edit { block(it) }
    }
}

// ---------------- 序列化辅助 ----------------

private fun parseDays(s: String?): Set<Int> {
    if (s.isNullOrBlank()) return setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
    return s.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        .ifEmpty { setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY) }
}

private fun parseIntMap(s: String?): Map<String, Int> {
    if (s.isNullOrBlank()) return emptyMap()
    return s.split("|").mapNotNull { part ->
        val kv = part.split(":")
        if (kv.size == 2) {
            val v = kv[1].toIntOrNull() ?: return@mapNotNull null
            kv[0] to v
        } else null
    }.toMap()
}

private fun serializeIntMap(map: Map<String, Int>): String =
    map.entries.sortedBy { it.key }.joinToString("|") { "${it.key}:${it.value}" }

private fun parseBoolMap(s: String?): Map<String, Boolean> {
    if (s.isNullOrBlank()) return emptyMap()
    return s.split("|").mapNotNull { part ->
        val kv = part.split(":")
        if (kv.size == 2) kv[0] to (kv[1] == "1") else null
    }.toMap()
}

private fun serializeBoolMap(map: Map<String, Boolean>): String =
    map.entries.sortedBy { it.key }.joinToString("|") { "${it.key}:${if (it.value) 1 else 0}" }

private fun Map<String, Int>.trimOld(): Map<String, Int> {
    if (size <= MAX_KEEP_DAYS) return this
    val oldest = shiftDateKey(todayKey(), -MAX_KEEP_DAYS)
    return filterKeys { it >= oldest }
}

private fun Map<String, Boolean>.trimOldBool(): Map<String, Boolean> {
    if (size <= MAX_KEEP_DAYS) return this
    val oldest = shiftDateKey(todayKey(), -MAX_KEEP_DAYS)
    return filterKeys { it >= oldest }
}
