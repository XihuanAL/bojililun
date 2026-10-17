package com.example.boji.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.boji.data.Settings
import com.example.boji.reminder.AlarmScheduler
import com.example.boji.ui.AppController
import com.example.boji.ui.components.BojiCard
import com.example.boji.ui.components.Chip
import com.example.boji.ui.components.ModuleCard
import com.example.boji.ui.components.SectionTitle
import com.example.boji.ui.components.SettingRow
import com.example.boji.ui.components.SwitchRow
import com.example.boji.ui.components.TimePickerDialog
import com.example.boji.ui.components.drawMuscleMark
import com.example.boji.ui.theme.Muscle
import com.example.boji.ui.theme.MuscleDeep
import com.example.boji.ui.theme.MuscleSoft
import com.example.boji.ui.theme.muscleInk
import com.example.boji.util.Copy
import com.example.boji.util.WEEKDAY_LABEL
import com.example.boji.util.WEEK_ORDER
import com.example.boji.util.formatMinute
import com.example.boji.util.todayWeekday
import com.example.boji.util.weekdayShort

/** 总览页卡片与子页共用同一套渐变 */
val MuscleCardBrush = Brush.horizontalGradient(
    listOf(MuscleDeep, Color(0xFFE24A22))
)

/**
 * 薄肌理论 · 纯激励页。
 * 不做任何训练记录与统计，所以卡片里没有进度槽，激励语本身就是主体。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MuscleScreen(
    controller: AppController,
    settings: Settings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var quote by remember { mutableStateOf(Copy.muscleQuote(context)) }
    var showTimePick by remember { mutableStateOf(false) }

    val today = todayWeekday()
    val isToday = today in settings.muscleDays
    val gap = AlarmScheduler.nextMuscleDayGap(settings)

    // 整页一个滚动容器：卡片、训练日、提醒设置是一体的，一起上滑
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 28.dp)
    ) {
        ModuleCard(
            title = "薄肌理论",
            badge = if (isToday) "今天训练日" else "今天休息日",
            quote = quote,
            brush = MuscleCardBrush,
            softColor = MuscleSoft,
            glow = Muscle,
            decoration = { drawMuscleMark(it) },
            quoteStyle = MaterialTheme.typography.displayMedium,
            minHeight = 220.dp,
            showQuoteHint = true,
            onCardClick = { quote = Copy.muscleQuote(context) },
            info = if (isToday) "训练日提醒 ${formatMinute(settings.muscleTimeMin)}"
            else "下次训练日还有 $gap 天"
        )

        Spacer(Modifier.height(18.dp))

        // ---------- 训练日 ----------
        SectionTitle("训练日")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WEEK_ORDER.forEach { day ->
                Chip(
                    text = weekdayShort(day),
                    selected = day in settings.muscleDays,
                    tint = Muscle,
                    onClick = {
                        val next = if (day in settings.muscleDays) {
                            settings.muscleDays - day
                        } else {
                            settings.muscleDays + day
                        }
                        controller.run { setMuscleDays(next) }
                    }
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---------- 提醒设置（跟随浅色 / 深色主题） ----------
        SectionTitle("提醒设置")
        BojiCard {
            SettingRow(title = "提醒时间", subtitle = formatMinute(settings.muscleTimeMin)) {
                TextButton(onClick = { showTimePick = true }) { Text("修改", color = muscleInk()) }
            }
            SwitchRow(
                title = "健身日提醒",
                subtitle = if (settings.muscleEnabled) {
                    "开启中 · ${WEEKDAY_LABEL[today] ?: ""}等训练日会喊你"
                } else "已关闭",
                checked = settings.muscleEnabled,
                tint = Muscle
            ) { controller.run { setMuscleEnabled(it) } }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "点到通知直接进这一页，没有完成 / 跳过按钮——练不练你自己说了算。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "这一页不做任何训练记录，练什么、练多重，交给你已经在用的健身软件。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    if (showTimePick) {
        TimePickerDialog(
            title = "健身提醒时间",
            initialMinute = settings.muscleTimeMin,
            tint = Muscle,
            onDismiss = { showTimePick = false },
            onConfirm = { m -> controller.run { setMuscleTime(m) }; showTimePick = false }
        )
    }
}
