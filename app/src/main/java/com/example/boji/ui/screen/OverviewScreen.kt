package com.example.boji.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boji.data.Records
import com.example.boji.data.Settings
import com.example.boji.reminder.AlarmScheduler
import com.example.boji.ui.AppController
import com.example.boji.ui.components.ModuleCard
import com.example.boji.ui.components.ModuleRingProgress
import com.example.boji.ui.components.WeekSquares
import com.example.boji.ui.components.drawMuscleMark
import com.example.boji.ui.components.drawPillow
import com.example.boji.ui.components.drawWaterGlass
import com.example.boji.ui.theme.Muscle
import com.example.boji.ui.theme.MuscleSoft
import com.example.boji.ui.theme.Sleep
import com.example.boji.ui.theme.SleepSoft
import com.example.boji.ui.theme.Success
import com.example.boji.ui.theme.Water
import com.example.boji.ui.theme.WaterSoft
import com.example.boji.util.Copy
import com.example.boji.util.WEEK_ORDER
import com.example.boji.util.formatMinute
import com.example.boji.util.shiftDateKey
import com.example.boji.util.todayKey
import com.example.boji.util.todayWeekday
import com.example.boji.util.weekdayShort
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun OverviewScreen(
    controller: AppController,
    settings: Settings,
    records: Records,
    onOpenSettings: () -> Unit,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val opener = remember { Copy.overviewOpener(context, settings.nickname) }
    val waterQuote = remember { Copy.waterQuote(context) }
    val sleepQuote = remember { Copy.sleepQuote(context) }
    val muscleQuote = remember { Copy.muscleQuote(context) }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..10 -> "早上好"
            in 11..13 -> "中午好"
            in 14..17 -> "下午好"
            in 18..22 -> "晚上好"
            else -> "还没睡？"
        }
    }
    val todayLabel = remember {
        SimpleDateFormat("M 月 d 日", Locale.CHINA).format(Date()) +
            " · " + when (todayWeekday()) {
                Calendar.SUNDAY -> "周日"
                Calendar.MONDAY -> "周一"
                Calendar.TUESDAY -> "周二"
                Calendar.WEDNESDAY -> "周三"
                Calendar.THURSDAY -> "周四"
                Calendar.FRIDAY -> "周五"
                else -> "周六"
            }
    }

    val done = records.waterByDate[todayKey()] ?: 0
    val goal = settings.waterGoalMl
    val waterProgress = if (goal > 0) done.toFloat() / goal.toFloat() else 0f
    val waterReached = goal > 0 && done >= goal

    val lastNight = shiftDateKey(todayKey(), -1)
    val lastNightResult = records.sleepByDate[lastNight]
    val nights = (7 downTo 1).map { shiftDateKey(todayKey(), -it) }
    val results = nights.map { records.sleepByDate[it] }
    val onTimeCount = results.count { it == true }

    val isMuscleDay = AlarmScheduler.isMuscleDay(settings)
    val gap = AlarmScheduler.nextMuscleDayGap(settings)
    val dayBadge = WEEK_ORDER
        .filter { it in settings.muscleDays }
        .joinToString(" ") { weekdayShort(it) }
        .ifBlank { "未设置" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 28.dp)
    ) {
        // 顶部：日期 + 设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${settings.nickname}，$greeting",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = opener,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // ---------- 三张卡：与子模块页顶部是同一个组件，整张可点击跳转 ----------
        ModuleCard(
            title = "大水理论",
            badge = "目标 $goal ml",
            quote = waterQuote,
            brush = WaterCardBrush,
            softColor = WaterSoft,
            glow = Water,
            decoration = { drawWaterGlass(it) },
            onCardClick = { onNavigate(1) },
            progressSlot = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModuleRingProgress(
                        progress = waterProgress,
                        color = Water,
                        modifier = Modifier.size(76.dp),
                        stroke = 8.dp
                    ) {
                        Text(
                            text = "${(waterProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Water
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "已喝 $done ml",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "目标 $goal ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            info = if (waterReached) "今日已达标，今天不再提醒"
            else "还差 ${(goal - done).coerceAtLeast(0)} ml"
        )

        Spacer(Modifier.height(14.dp))

        ModuleCard(
            title = "大睡理论",
            badge = "就寝 ${formatMinute(settings.sleepTargetMin)}",
            quote = sleepQuote,
            brush = SleepCardBrush,
            softColor = SleepSoft,
            glow = Sleep,
            decoration = { drawPillow(it) },
            onCardClick = { onNavigate(2) },
            progressSlot = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$onTimeCount",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " / 7 天达标",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = when (lastNightResult) {
                                true -> "昨晚 ✓"
                                false -> "昨晚 ✗"
                                null -> "待记录"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (lastNightResult) {
                                true -> Success
                                false -> Color.White.copy(alpha = 0.45f)
                                null -> Sleep
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    WeekSquares(results = results, height = 30.dp)
                }
            },
            info = when (lastNightResult) {
                true -> "昨晚按时睡了 · 近 7 天达标 $onTimeCount 天"
                false -> "昨晚没忍住 · 近 7 天达标 $onTimeCount 天"
                null -> "昨晚还没记录 · 近 7 天达标 $onTimeCount 天"
            }
        )

        Spacer(Modifier.height(14.dp))

        ModuleCard(
            title = "薄肌理论",
            badge = dayBadge,
            quote = muscleQuote,
            brush = MuscleCardBrush,
            softColor = MuscleSoft,
            glow = Muscle,
            decoration = { drawMuscleMark(it) },
            onCardClick = { onNavigate(3) },
            // 薄肌没有进度，占位让三张卡高度对齐
            progressSlot = {},
            info = if (isMuscleDay) "今天训练日 · 提醒 ${formatMinute(settings.muscleTimeMin)}"
            else "今天休息日 · 下次训练日还有 $gap 天"
        )
    }
}
