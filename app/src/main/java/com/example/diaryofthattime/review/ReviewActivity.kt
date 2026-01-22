package com.example.diaryofthattime.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diaryofthattime.R
import android.widget.TextView

class ReviewActivity : AppCompatActivity() {

    private var diaryId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        diaryId = intent.getLongExtra("diaryId", -1)

        //findViewById<TextView>(R.id.tvQuestion).text =
        //    "지금 이 감정은 정리되었나요?"
    }
}
