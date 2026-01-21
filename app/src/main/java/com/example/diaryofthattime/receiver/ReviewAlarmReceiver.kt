package com.example.diaryofthattime.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.diaryofthattime.R
import com.example.diaryofthattime.review.ReviewActivity

class ReviewAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val diaryId = intent.getLongExtra("diaryId", -1)

        val notificationIntent =
            Intent(context, ReviewActivity::class.java).apply {
                putExtra("diaryId", diaryId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            context,
            diaryId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "review_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("회고 알림")
            .setContentText("일주일 전의 감정을 돌아볼 시간이에요.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(diaryId.toInt(), notification)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        TODO("Not yet implemented")
    }
}
