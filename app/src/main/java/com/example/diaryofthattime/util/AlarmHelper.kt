package com.example.diaryofthattime.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.diaryofthattime.receiver.ReviewAlarmReceiver

object AlarmHelper {

    fun scheduleReviewAlarm(
        context: Context,
        diaryId: Long,
        createdAt: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ Android 12+ : 정확 알람 권한 체크 (막혀 있으면 setExact이 제대로 안 될 수 있음)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(i)
                return
            }
        }

        val intent = Intent(context, ReviewAlarmReceiver::class.java).apply {
            action = "com.example.diaryofthattime.ACTION_REVIEW_ALARM" // ✅ PendingIntent 동일성 안정화
            putExtra("diaryId", diaryId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            diaryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // ✅ 테스트: 1분 뒤 (실제 운영: createdAt + 7days)
        val triggerTime = System.currentTimeMillis() + 60_000L

        // ✅ 같은 PI면 이전 예약을 지우고 다시 예약(중복/꼬임 방지)
        alarmManager.cancel(pendingIntent)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

