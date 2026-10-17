package com.example.boji

import android.app.Application
import com.example.boji.reminder.AlarmScheduler
import com.example.boji.reminder.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BojiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Notifier.createChannels(this)
        // 每次进程启动兜底重排一次，覆盖国产 ROM 收不到 BOOT_COMPLETED 的情况
        CoroutineScope(Dispatchers.IO).launch {
            AlarmScheduler.rescheduleAll(this@BojiApplication)
        }
    }
}
