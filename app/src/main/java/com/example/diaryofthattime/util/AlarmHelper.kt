package com.example.diaryofthattime.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.diaryofthattime.receiver.AlarmReceiver
import java.util.Calendar

//기존 일주일 뒤 알람 코드
/*object AlarmHelper {

    // 일주일(7일)을 밀리초로 계산
    private const val ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L

    /**
     * 회고 알람 예약 함수
     * @param context Context
     * @param diaryId 일기 ID (알람 구분 및 데이터 조회용)
     * @param baseTime 기준 시간 (보통 System.currentTimeMillis())
     */
    fun scheduleReviewAlarm(context: Context, diaryId: Long, baseTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("diaryId", diaryId)
        }

        // 각 일기마다 고유한 알람을 가지도록 diaryId를 requestCode로 사용
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            diaryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 현재 시간으로부터 일주일 뒤 설정
        val triggerTime = baseTime + ONE_WEEK_MILLIS

        // 안드로이드 버전에 따른 정확한 알람 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}*/


//테스트용 1분 뒤 알람 코드
object AlarmHelper {

    // 테스트를 위해 일주일(7일) 대신 1분(60초)으로 설정
    // private const val ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L // 기존 코드 (주석 처리)
    private const val ONE_MINUTE_MILLIS = 60 * 1000L // [테스트용] 1분

    fun scheduleReviewAlarm(context: Context, diaryId: Long, baseTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("diaryId", diaryId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            diaryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // [수정] 1분 뒤로 트리거 시간 설정
        val triggerTime = baseTime + ONE_MINUTE_MILLIS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}