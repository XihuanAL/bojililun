package com.example.boji.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boji.data.Records
import com.example.boji.data.Settings
import com.example.boji.ui.AppController
import com.example.boji.ui.components.BojiCard
import com.example.boji.ui.components.ModuleCard
import com.example.boji.ui.components.ModuleRingProgress
import com.example.boji.ui.components.NumberInputDialog
import com.example.boji.ui.components.SectionTitle
import com.example.boji.ui.components.SettingRow
import com.example.boji.ui.components.StepperRow
import com.example.boji.ui.components.SwitchRow
import com.example.boji.ui.components.TimePickerDialog
import com.example.boji.ui.components.drawWaterGlass
import com.example.boji.ui.theme.Water
import com.example.boji.ui.theme.WaterSoft
import com.example.boji.ui.theme.waterInk
import com.example.boji.util.Copy
import com.example.boji.util.formatMinute
import com.example.boji.util.todayKey

/** 总览页卡片与子页共用同一套渐变 */
val WaterCardBrush = Brush.horizontalGradient(
    listOf(Color(0xFF062A45), Color(0xFF1187C4))
)

@Composable
fun WaterScreen(
    controller: AppController,
    settings: Settings,
    records: Records,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val done = records.waterByDate[todayKey()] ?: 0
    val goal = settings.waterGoalMl
    val progress = if (goal > 0) done.toFloat() / goal.toFloat() else 0f
    val reached = goal > 0 && done >= goal

    var quote by remember { mutableStateOf(Copy.waterQuote(context)) }
    var lastAdd by remember { mutableIntStateOf(0) }
    var showCustom by remember { mutableStateOf(false) }
    var timePick by remember { mutableStateOf<Int?>(null) }

    // 整页一个滚动容器：卡片、快捷记录、提醒设置是一体的，一起上滑
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 28.dp)
    ) {
        // ---------- 模块卡：进度是主体，激励语小一号 ----------
        ModuleCard(
            title = "大水理论",
            badge = "目标 $goal ml",
            quote = quote,
            brush = WaterCardBrush,
            softColor = WaterSoft,
            glow = Water,
            decoration = { drawWaterGlass(it) },
            quoteStyle = MaterialTheme.typography.headlineMedium,
            showQuoteHint = true,
            onCardClick = { quote = Copy.waterQuote(context) },
            progressSlot = {
                ModuleRingProgress(
                    progress = progress,
                    color = Water,
                    modifier = Modifier.size(152.dp),
                    stroke = 12.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "已喝 $done ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
            },
            info = if (reached) "目标 $goal ml · 今日已达标"
            else "目标 $goal ml · 还差 ${(goal - done).coerceAtLeast(0)} ml"
        )

        Spacer(Modifier.height(18.dp))

        // ---------- 快捷记录 ----------
        SectionTitle("快捷记录")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAdd(
                text = "+200",
                modifier = Modifier.weight(1f),
                onClick = { lastAdd = 200; controller.addWater(200) }
            )
            QuickAdd(
                text = "+500",
                modifier = Modifier.weight(1f),
                onClick = { lastAdd = 500; controller.addWater(500) }
            )
            QuickAdd(
                text = "自定义",
                modifier = Modifier.weight(1f),
                onClick = { showCustom = true }
            )
        }
        if (done > 0) {
            Row {
                TextButton(onClick = {
                    if (lastAdd > 0) controller.run { undoWater(lastAdd) }
                    lastAdd = 0
                }) { Text("撤销上次", color = waterInk()) }
                TextButton(onClick = {
                    lastAdd = 0
                    controller.run { resetTodayWater() }
                }) { Text("清空今日", color = waterInk()) }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---------- 提醒设置（跟随浅色 / 深色主题） ----------
        SectionTitle("提醒设置")
        BojiCard {
            SwitchRow(
                title = "喝水提醒",
                subtitle = if (settings.waterEnabled) "开启中 · 按间隔提醒" else "已关闭",
                checked = settings.waterEnabled,
                tint = Water
            ) { controller.run { setWaterEnabled(it) } }

            StepperRow(
                title = "每日目标",
                valueText = "${settings.waterGoalMl}",
                tint = Water,
                minusEnabled = settings.waterGoalMl > 200,
                plusEnabled = settings.waterGoalMl < 10000,
                onMinus = { controller.run { setWaterGoal(settings.waterGoalMl - 100) } },
                onPlus = { controller.run { setWaterGoal(settings.waterGoalMl + 100) } }
            )

            SettingRow(title = "提醒开始", subtitle = formatMinute(settings.waterStartMin)) {
                TextButton(onClick = { timePick = 0 }) { Text("修改", color = waterInk()) }
            }
            SettingRow(title = "提醒结束", subtitle = formatMinute(settings.waterEndMin)) {
                TextButton(onClick = { timePick = 1 }) { Text("修改", color = waterInk()) }
            }

            StepperRow(
                title = "提醒间隔",
                valueText = "${settings.waterIntervalMin} 分",
                tint = Water,
                minusEnabled = settings.waterIntervalMin > 15,
                plusEnabled = settings.waterIntervalMin < 480,
                onMinus = { controller.run { setWaterInterval(settings.waterIntervalMin - 15) } },
                onPlus = { controller.run { setWaterInterval(settings.waterIntervalMin + 15) } }
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "达成目标后当天不再提醒，闹钟直接排到第二天。数据只存在本机。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    if (showCustom) {
        NumberInputDialog(
            title = "自定义饮水量",
            initialValue = "250",
            unit = "ml",
            onDismiss = { showCustom = false },
            onConfirm = { v -> lastAdd = v; controller.addWater(v); showCustom = false }
        )
    }

    when (timePick) {
        0 -> TimePickerDialog(
            title = "提醒开始时间",
            initialMinute = settings.waterStartMin,
            tint = Water,
            onDismiss = { timePick = null },
            onConfirm = { m -> controller.run { setWaterStart(m) }; timePick = null }
        )
        1 -> TimePickerDialog(
            title = "提醒结束时间",
            initialMinute = settings.waterEndMin,
            tint = Water,
            onDismiss = { timePick = null },
            onConfirm = { m -> controller.run { setWaterEnd(m) }; timePick = null }
        )
        else -> Unit
    }
}

@Composable
private fun QuickAdd(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Water,
            contentColor = Color.White
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
