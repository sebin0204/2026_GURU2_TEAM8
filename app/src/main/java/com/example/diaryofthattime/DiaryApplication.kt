package com.example.diaryofthattime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class DiaryApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("DiaryApp", "✅ Application onCreate 실행됨")

        createReviewChannel()

        Log.d("DiaryApp", "✅ review_channel 생성 완료(또는 이미 존재)")
    }

    private fun createReviewChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "review_channel",
                "회고 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

