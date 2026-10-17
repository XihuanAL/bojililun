package com.example.boji

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.boji.data.BojiRepository
import com.example.boji.data.Records
import com.example.boji.data.Settings
import com.example.boji.reminder.AlarmScheduler
import com.example.boji.reminder.Notifier
import com.example.boji.ui.AppController
import com.example.boji.ui.BojiRoot
import com.example.boji.ui.theme.BojiTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TAB = "boji_tab"
        const val TAB_WATER = "water"
        const val TAB_SLEEP = "sleep"
        const val TAB_MUSCLE = "muscle"
    }

    /** (目标 Tab, 递增序号)。带序号是为了「连续来两条同类通知」时也能触发跳转 */
    private val tabFlow = MutableStateFlow(TabRequest(null, 0))
    private var tabSeq = 0

    private fun pushTab(tab: String?) {
        if (tab == null) return
        tabFlow.value = TabRequest(tab, ++tabSeq)
    }

    data class TabRequest(val tab: String?, val seq: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pushTab(intent?.getStringExtra(EXTRA_TAB))

        setContent {
            val context = LocalContext.current
            val repository = remember { BojiRepository(context.applicationContext) }
            val scope = rememberCoroutineScope()
            val controller = remember {
                AppController(repository, context.applicationContext, scope)
            }
            val settings by repository.settings.collectAsState(initial = Settings())
            val records by repository.records.collectAsState(initial = Records())
            val tabRequest by tabFlow.collectAsState()

            // Android 13+ 通知权限
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* 用户选择后无论结果都继续，设置页可再次引导 */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !Notifier.hasNotificationPermission(context)
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                AlarmScheduler.rescheduleAllSuspend(context.applicationContext)
            }

            BojiTheme(themeMode = settings.themeMode) {
                BojiRoot(
                    controller = controller,
                    settings = settings,
                    records = records,
                    targetTab = tabRequest
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pushTab(intent.getStringExtra(EXTRA_TAB))
    }

    override fun onResume() {
        super.onResume()
        // 从系统设置页返回时（通知/精确闹钟/电池优化）重新确认排期，异步执行不卡主线程
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            AlarmScheduler.rescheduleAllSuspend(applicationContext)
        }
    }
}
