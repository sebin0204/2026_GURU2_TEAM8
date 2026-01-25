package com.example.diaryofthattime.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.diaryofthattime.R
import com.example.diaryofthattime.review.ReviewActivity// 패키지 경로 확인 필수

class ReviewAlarmReceiver : BroadcastReceiver() {

    // @SuppressLint를 추가하여 권한 체크 경고를 해결합니다.
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        // 1. 전달받은 일기 ID 가져오기
        val diaryId = intent.getLongExtra("diaryId", -1)
        if (diaryId == -1L) return

        // 2. 알림 클릭 시 실행될 Intent 설정 (ReviewActivity로 이동)
        val notificationIntent = Intent(context, ReviewActivity::class.java).apply {
            putExtra("diaryId", diaryId.toInt()) // ReviewActivity에서 쓰는 키값과 통일
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 3. PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            context,
            diaryId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. 알림 객체 생성
        val notification = NotificationCompat.Builder(context, "review_channel")
            .setSmallIcon(R.drawable.ic_notification) // drawable에 아이콘이 있어야 함
            .setContentTitle("회고 알림")
            .setContentText("일주일 전의 감정을 돌아볼 시간이에요.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 설정
            .setAutoCancel(true)
            .build()

        // 5. 알림 띄우기
        NotificationManagerCompat.from(context).notify(diaryId.toInt(), notification)
    }
}