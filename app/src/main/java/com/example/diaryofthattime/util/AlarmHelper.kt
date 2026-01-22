package com.example.diaryofthattime.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.diaryofthattime.receiver.ReviewAlarmReceiver

object AlarmHelper {

    @SuppressLint("ServiceCast")
    fun scheduleReviewAlarm(
        context: Context,
        diaryId: Long,
        createdAt: Long
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReviewAlarmReceiver::class.java).apply {
            putExtra("diaryId", diaryId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            diaryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = createdAt + 7 * 24 * 60 * 60 * 1000L

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}
