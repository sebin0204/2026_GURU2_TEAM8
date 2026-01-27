package com.example.diaryofthattime.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.diaryofthattime.R
import com.example.diaryofthattime.review.ReviewActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val diaryId = intent.getLongExtra("diaryId", -1L)
        android.util.Log.d("AlarmTest", "알람 수신 성공! ID: $diaryId")

        if (diaryId != -1L) {
            showNotification(context, diaryId)
        }
    }

    private fun showNotification(context: Context, diaryId: Long) {
        val channelId = "review_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 이상 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "감정 회고 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "일주일 전 감정을 돌아볼 시간입니다."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 클릭 시 ReviewActivity로 이동하는 Intent
        val reviewIntent = Intent(context, ReviewActivity::class.java).apply {
            putExtra("diaryId", diaryId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            diaryId.toInt(),
            reviewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 프로젝트에 맞는 아이콘으로 변경하세요
            .setContentTitle("그때의 일기")
            .setContentText("일주일 전의 감정, 지금은 좀 어떠신가요? 회고를 작성해보세요.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(diaryId.toInt(), builder.build())
    }
}