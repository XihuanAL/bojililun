package com.example.boji.ui

import android.content.Context
import com.example.boji.data.BojiRepository
import com.example.boji.reminder.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * UI 与数据层之间的唯一入口。
 * 任何设置变更后都会自动重排提醒，保证开关/时间改动立刻生效。
 */
class AppController(
    val repository: BojiRepository,
    private val appContext: Context,
    private val scope: CoroutineScope
) {
    /** 执行一段数据操作，结束后重排全部提醒 */
    fun run(block: suspend BojiRepository.() -> Unit) {
        scope.launch(Dispatchers.IO) {
            repository.block()
            AlarmScheduler.rescheduleAllSuspend(appContext)
        }
    }

    /** 只改数据、不重排提醒（用于高频的喝水记录之外的轻量操作） */
    fun runQuiet(block: suspend BojiRepository.() -> Unit) {
        scope.launch(Dispatchers.IO) { repository.block() }
    }

    /** 喝水记录：加完立刻判断是否需要停掉今天的提醒 */
    fun addWater(ml: Int) = run { addWater(ml) }
}
