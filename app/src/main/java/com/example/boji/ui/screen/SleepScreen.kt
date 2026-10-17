package com.example.boji.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.boji.ui.components.SectionTitle
import com.example.boji.ui.components.SettingRow
import com.example.boji.ui.components.StepperRow
import com.example.boji.ui.components.SwitchRow
import com.example.boji.ui.components.TimePickerDialog
import com.example.boji.ui.components.WeekSquares
import com.example.boji.ui.components.drawPillow
import com.example.boji.ui.theme.Sleep
import com.example.boji.ui.theme.SleepSoft
import com.example.boji.ui.theme.Success
import com.example.boji.ui.theme.sleepInk
import com.example.boji.ui.theme.successInk
import com.example.boji.util.Copy
import com.example.boji.util.formatMinute
import com.example.boji.util.shiftDateKey
import com.example.boji.util.todayKey

/** 总览页卡片与子页共用同一套渐变 */
val SleepCardBrush = Brush.horizontalGradient(
    listOf(Color(0xFF1C1038), Color(0xFF7A45D6))
)

@Composable
fun SleepScreen(
    controller: AppController,
    settings: Settings,
    records: Records,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lastNight = shiftDateKey(todayKey(), -1)
    val lastNightResult = records.sleepByDate[lastNight]
    var quote by remember { mutableStateOf(Copy.sleepQuote(context)) }
    var showTimePick by remember { mutableStateOf(false) }

    val nights = (7 downTo 1).map { shiftDateKey(todayKey(), -it) }
    val results = nights.map { records.sleepByDate[it] }
    val onTimeCount = results.count { it == true }
    val recordedCount = results.count { it != null }

    // 整页一个滚动容器：卡片、昨晚记录、提醒设置是一体的，一起上滑
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 28.dp)
    ) {
        // ---------- 模块卡：七个小方块是主体，激励语小一号 ----------
        ModuleCard(
            title = "大睡理论",
            badge = "就寝 ${formatMinute(settings.sleepTargetMin)}",
            quote = quote,
            brush = SleepCardBrush,
            softColor = SleepSoft,
            glow = Sleep,
            decoration = { drawPillow(it) },
            quoteStyle = MaterialTheme.typography.headlineMedium,
            showQuoteHint = true,
            onCardClick = { quote = Copy.sleepQuote(context) },
            progressSlot = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$onTimeCount",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " / 7 天达标",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = when (lastNightResult) {
                                true -> "昨晚 ✓"
                                false -> "昨晚 ✗"
                                null -> "待记录"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (lastNightResult) {
                                true -> Success
                                false -> Color.White.copy(alpha = 0.45f)
                                null -> Sleep
                            }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    WeekSquares(results = results, height = 48.dp)
                }
            },
            info = when (lastNightResult) {
                true -> "昨晚按时睡了 · 已记录 $recordedCount 天"
                false -> "昨晚没忍住 · 已记录 $recordedCount 天"
                null -> "昨晚还没记录 · 已记录 $recordedCount 天"
            }
        )

        Spacer(Modifier.height(18.dp))

        // ---------- 昨晚记录 ----------
        SectionTitle("昨晚（$lastNight）")
        if (lastNightResult == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { controller.runQuiet { setSleepResult(lastNight, true) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Success, contentColor = Color.White
                    )
                ) { Text("按时睡了", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { controller.runQuiet { setSleepResult(lastNight, false) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("没忍住", fontWeight = FontWeight.Bold) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lastNightResult) Copy.sleepDone(context, settings.nickname)
                    else Copy.sleepMiss(context, settings.nickname),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (lastNightResult) successInk()
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    controller.runQuiet { setSleepResult(lastNight, !lastNightResult) }
                }) { Text("改一下", color = sleepInk()) }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---------- 提醒设置（跟随浅色 / 深色主题） ----------
        SectionTitle("提醒设置")
        BojiCard {
            SwitchRow(
                title = "早睡提醒",
                subtitle = if (settings.sleepEnabled) "开启中" else "已关闭",
                checked = settings.sleepEnabled,
                tint = Sleep
            ) { controller.run { setSleepEnabled(it) } }

            SettingRow(title = "目标就寝", subtitle = formatMinute(settings.sleepTargetMin)) {
                TextButton(onClick = { showTimePick = true }) { Text("修改", color = sleepInk()) }
            }

            StepperRow(
                title = "提前提醒",
                valueText = "${settings.sleepLeadMin} 分",
                tint = Sleep,
                minusEnabled = settings.sleepLeadMin > 0,
                plusEnabled = settings.sleepLeadMin < 180,
                onMinus = { controller.run { setSleepLead(settings.sleepLeadMin - 5) } },
                onPlus = { controller.run { setSleepLead(settings.sleepLeadMin + 5) } }
            )

            SwitchRow(
                title = "睡前再追加一次",
                subtitle = "在目标就寝时刻再喊你一遍",
                checked = settings.sleepFollowEnabled,
                tint = Sleep
            ) { controller.run { setSleepFollowEnabled(it) } }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "首次提醒 ${formatMinute((settings.sleepTargetMin - settings.sleepLeadMin + 1440) % 1440)}，" +
                "追加提醒 ${formatMinute(settings.sleepTargetMin)}。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "已记录 $recordedCount 天，达标 $onTimeCount 天。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    if (showTimePick) {
        TimePickerDialog(
            title = "目标就寝时间",
            initialMinute = settings.sleepTargetMin,
            tint = Sleep,
            onDismiss = { showTimePick = false },
            onConfirm = { m -> controller.run { setSleepTarget(m) }; showTimePick = false }
        )
    }
}
