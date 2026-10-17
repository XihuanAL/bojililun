package com.example.boji.ui.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.boji.data.Settings
import com.example.boji.ui.AppController
import com.example.boji.ui.components.BojiCard
import com.example.boji.ui.components.SectionTitle
import com.example.boji.ui.components.SegmentedPicker
import com.example.boji.ui.theme.Danger
import com.example.boji.ui.theme.ThemeMode
import com.example.boji.util.Permissions

@Composable
fun SettingsScreen(
    controller: AppController,
    settings: Settings,
    onBack: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nickname by remember(settings.nickname) { mutableStateOf(settings.nickname) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        SectionTitle("昵称")
        BojiCard {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it.take(12) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("提醒文案里的称呼，默认「预备役」") }
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { controller.runQuiet { setNickname(nickname) } },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("保存昵称") }
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle("外观")
        BojiCard {
            Text("深色模式", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            SegmentedPicker(
                options = listOf("跟随系统", "浅色", "深色"),
                selectedIndex = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> 0
                    ThemeMode.LIGHT -> 1
                    ThemeMode.DARK -> 2
                },
                onSelect = {
                    val mode = when (it) {
                        1 -> ThemeMode.LIGHT
                        2 -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                    controller.runQuiet { setThemeMode(mode) }
                }
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle("提醒与后台")
        BojiCard {
            StatusLine("通知权限", Permissions.notificationGranted(context))
            StatusLine("精确闹钟权限", Permissions.exactAlarmGranted(context))
            StatusLine("电池优化白名单", !Permissions.batteryRestricted(context))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpenGuide,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("查看后台保活引导") }
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle("数据")
        BojiCard {
            Text(
                text = "所有数据都保存在本机，卸载即清空。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showClearConfirm = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger, contentColor = Color.White
                )
            ) { Text("清空全部数据") }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "薄肌预备役 v1.0 · 数据只在本机",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("确认清空？") },
            text = { Text("喝水记录、睡眠记录和个人设置全部删除，且无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    controller.run { clearAllData() }
                    showClearConfirm = false
                }) { Text("清空", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("再想想") }
            }
        )
    }
}

@Composable
private fun StatusLine(label: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = if (ok) "已开启" else "未开启",
            style = MaterialTheme.typography.labelLarge,
            color = if (ok) MaterialTheme.colorScheme.primary else Danger
        )
    }
}
