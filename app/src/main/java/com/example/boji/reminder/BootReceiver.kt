package com.example.boji.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机 / 应用升级 / 时间或时区变更后，重新注册所有提醒。
 * 国产 ROM 只有在给了「自启动」权限时才会收到 BOOT_COMPLETED，
 * 所以 App 每次启动也会兜底重排一次（见 BojiApplication）。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Notifier.createChannels(app)
                AlarmScheduler.rescheduleAll(app)
            } finally {
                pending.finish()
            }
        }
    }
}
