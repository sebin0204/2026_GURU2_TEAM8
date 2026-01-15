package com.example.diaryofthattime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 고유 번호 (자동 생성)
    val emotion: String,           // 지금 느끼는 감정
    val reason: String,            // 감정의 이유
    val isFact: Boolean,           // 사실(true)인지 추측(false)인지
    val date: Long,                // 작성 날짜 (System.currentTimeMillis())

    // 일주일 후 회고 데이터 (처음엔 값이 없으므로 null 허용)
    var isReviewed: Boolean = false,
    var reviewFact: String? = null,
    var howToOvercome: String? = null
)