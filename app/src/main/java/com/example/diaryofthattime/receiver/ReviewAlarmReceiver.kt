package com.example.diaryofthattime.receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.diaryofthattime.R
import com.example.diaryofthattime.review.ReviewActivity

class ReviewAlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        // 1. 테스트를 위한 로그 출력 (Logcat에서 확인 가능)
        val diaryId = intent.getLongExtra("diaryId", -1)
        Log.d("ALARM_CHECK", "알람 수신 성공! 일기 ID: $diaryId")

        if (diaryId == -1L) return

        // 2. 알림 채널 생성 (Android 8.0 이상 필수)
        createNotificationChannel(context)

        // 3. 알림 클릭 시 ReviewActivity로 이동 설정
        val notificationIntent = Intent(context, ReviewActivity::class.java).apply {
            putExtra("diaryId", diaryId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            diaryId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. 알림 객체 생성
        val notification = NotificationCompat.Builder(context, "review_channel")
            .setSmallIcon(R.drawable.ic_notification) // drawable에 ic_notification이 있어야 함
            .setContentTitle("회고 알림")
            .setContentText("일주일 전의 감정을 돌아볼 시간이에요.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 5. 알림 전송
        try {
            NotificationManagerCompat.from(context).notify(diaryId.toInt(), notification)
        } catch (e: Exception) {
            Log.e("ALARM_CHECK", "알림 전송 실패: ${e.message}")
        }
    }

    // 알림 채널 생성 함수
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "회고 알림"
            val descriptionText = "일주일 전 감정을 복기하기 위한 알림입니다."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("review_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}