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
import com.example.diaryofthattime.data.AppDatabase

class HappyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)

        // DB에서 '행복' 감정인 일기들만 가져오기
        val happyDiaries = db.diaryDao().getHappyDiariesSync() // (아래 Dao 설명 참고)

        if (!happyDiaries.isNullOrEmpty()) {
            val randomDiary = happyDiaries.random()
            // 알림에 ID와 제목을 함께 보냅니다.
            sendNotification(randomDiary.id, randomDiary.title)

            return Result.success()
        }

        return Result.success()
    }

    private fun sendNotification(diaryId: Int, title: String) {
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
            putExtra("HAPPY_CONTENT", title)
            putExtra("DIARY_ID", diaryId) // 일기 ID를 택배에 넣습니다.
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 설정 알림창 메시지
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 폰 상단 바에 뜰 아이콘
            .setContentTitle("🌟 잊고 있던 행복한 기억") // 알림 제목
            .setContentText("\"$title\" 기억나시나요?") // 제목 노출
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 배너(Heads-up) 알림을 위한 설정
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // 진동, 소리 기본값 사용
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 클릭하면 자동으로 사라짐
            .setFullScreenIntent(pendingIntent, true) // 잠금 화면 등에서도 강하게 나타나게 함

        // 알림 발송 (상단 바, 알림창, 배너)
        notificationManager.notify(1, builder.build())
    }
}