package com.example.boji.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.boji.data.BojiRepository
import com.example.boji.data.Records
import com.example.boji.util.todayKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE) ?: return
        val app = context.applicationContext
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = BojiRepository(app)
                val s = repo.settingsSnapshot()
                when (type) {
                    AlarmScheduler.TYPE_WATER -> {
                        if (s.waterEnabled) {
                            val r: Records = repo.recordsSnapshot()
                            val done = r.waterByDate[todayKey()] ?: 0
                            if (done < s.waterGoalMl) Notifier.showWater(app, s, done)
                            AlarmScheduler.scheduleWater(app, s, r)
                        }
                    }

                    AlarmScheduler.TYPE_SLEEP -> {
                        if (s.sleepEnabled) {
                            Notifier.showSleep(app, s, followUp = false)
                            AlarmScheduler.scheduleSleep(app, s)
                        }
                    }

                    AlarmScheduler.TYPE_SLEEP_FOLLOW -> {
                        if (s.sleepEnabled && s.sleepFollowEnabled) {
                            Notifier.showSleep(app, s, followUp = true)
                        }
                    }

                    AlarmScheduler.TYPE_MUSCLE -> {
                        if (s.muscleEnabled) {
                            Notifier.showMuscle(app, s, AlarmScheduler.weekLabel())
                            AlarmScheduler.scheduleMuscle(app, s)
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
