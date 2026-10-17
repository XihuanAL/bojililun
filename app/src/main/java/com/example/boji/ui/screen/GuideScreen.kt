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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.boji.ui.components.BojiCard
import com.example.boji.util.Permissions

/**
 * 国产手机后台保活引导页。
 * 华为/小米/OPPO/vivo 等都会杀后台，不给权限提醒就收不到，这里把路径讲清楚。
 */
@Composable
fun GuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

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
                text = "后台保活引导",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "国产 ROM 默认会杀后台，下面几项不开，提醒很可能会迟到甚至不来。按顺序点一遍就行。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        BojiCard {
            Text("1 · 通知权限", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Android 13 起需要手动授权，否则提醒不会弹出来。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { Permissions.openNotificationSettings(context) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (Permissions.notificationGranted(context)) "已开启，去检查" else "去开启") }
        }

        Spacer(Modifier.height(14.dp))

        BojiCard {
            Text("2 · 精确闹钟权限", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Android 12 起需要此权限才能准点提醒。没给的话，App 会退化成「不早于设定时间」，可能晚几分钟。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { Permissions.openExactAlarmSettings(context) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (Permissions.exactAlarmGranted(context)) "已开启，去检查" else "去开启") }
        }

        Spacer(Modifier.height(14.dp))

        BojiCard {
            Text("3 · 电池优化白名单", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "把 App 加入「不优化」列表，系统才不会在夜里把它冻住。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { Permissions.requestIgnoreBatteryOptimizations(context) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (Permissions.batteryRestricted(context)) "去加白名单" else "已加白名单") }
        }

        Spacer(Modifier.height(14.dp))

        BojiCard {
            Text("4 · 自启动 / 后台运行", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "这一项没有统一接口，需要手动开：\n" +
                    "· 小米/红米：设置 → 应用设置 → 应用管理 → 本应用 → 自启动 + 省电策略选「无限制」\n" +
                    "· 华为/荣耀：设置 → 应用 → 应用启动管理 → 本应用 → 手动管理，三项全开\n" +
                    "· OPPO/一加：设置 → 应用 → 应用管理 → 本应用 → 允许自启动 + 允许后台运行\n" +
                    "· vivo/iQOO：设置 → 应用与权限 → 应用管理 → 本应用 → 权限 → 自启动\n" +
                    "· 三星：设置 → 电池 → 后台使用限制 → 加入「永不休眠的应用」",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { Permissions.openAppDetails(context) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("打开应用详情页") }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "另外：本 App 在每次启动时都会重新注册一次提醒，即使收不到开机广播也能兜底。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
