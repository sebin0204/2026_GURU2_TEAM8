package com.example.diaryofthattime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class HappyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        // 가짜 데이터(더미)
        val allHappyMemories = listOf(
            "정말 즐거웠던 산책",
            "맛있는 점심 식사",
            "친구와의 깜짝 만남",
            "햇살 좋은 오후",
            "작은 화분에 물 주기"
        )

        if (allHappyMemories.isNotEmpty()) {
            // 전체 데이터 중 하나를 랜덤으로 뽑습니다.
            val randomMemory = allHappyMemories.random()

            // 휴대폰 알림창으로 보냅니다.
            sendNotification(randomMemory)
        }

        return Result.success()
    }

    private fun sendNotification(message: String) {
        val channelId = "HAPPY_CHANNEL_V2"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 채널 설정 (Android 8.0 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "행복 알림",
                NotificationManager.IMPORTANCE_HIGH // 중요도를 높여야 배너가 뜹니다!
            ).apply {
                description = "행복한 기억을 되새겨주는 알림입니다."
                enableLights(true)
                enableVibration(true) // 진동도 함께 울리게 설정
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 실행될 Intent 설정 : 앱의 메인 화면으로 이동하며 정보를 전달합니다.
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_HAPPY_POPUP", true)
            putExtra("HAPPY_CONTENT", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 설정 알림창 메시지
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 폰 상단 바에 뜰 아이콘
            .setContentTitle("🌟 잊고 있던 행복한 기억") // 알림 제목
            .setContentText("\"$message\" 기억나시나요?") // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 배너(Heads-up) 알림을 위한 설정
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // 진동, 소리 기본값 사용
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 클릭하면 자동으로 사라짐
            .setFullScreenIntent(pendingIntent, true) // 잠금 화면 등에서도 강하게 나타나게 함

        // 알림 발송 (상단 바, 알림창, 배너)
        notificationManager.notify(1, builder.build())
    }
}