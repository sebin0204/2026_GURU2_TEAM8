package com.example.diaryofthattime.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diaryofthattime.R
import android.widget.TextView
import com.example.diaryofthattime.util.AlarmHelper
import android.widget.Button

class ReviewActivity : AppCompatActivity() {

    private var diaryId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        diaryId = intent.getLongExtra("diaryId", -1)
        if (diaryId == -1L) finish()

        val tvQuestion = findViewById<TextView>(R.id.tvQuestion)
        tvQuestion.text = "지금 이 감정은 정리되었나요?"

        val btnYes = findViewById<Button>(R.id.btnOvercomeYes)
        val btnNo = findViewById<Button>(R.id.btnOvercomeNo)

        btnYes.setOnClickListener {
            // 1. 이 감정은 정리됨
            // 2. 리뷰 완료 처리
            // 3. 알림 취소
            finish()
        }

        btnNo.setOnClickListener {
            // 1. 아직 정리 안 됨
            // 2. 리뷰 알림 다시 예약
            AlarmHelper.scheduleReviewAlarm(
                context = this,
                diaryId = diaryId,
                createdAt = 7 * 24 * 60 * 60 * 1000L
            )
            finish()

        }
    }
}
