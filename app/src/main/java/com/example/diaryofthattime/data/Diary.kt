package com.example.diaryofthattime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,               // 고유 번호 (자동 생성)
    val title: String,             // 일기 제목
    val emotion: String,           // 지금 느끼는 감정 (행복, 슬픔 등)
    val reason: String,            // 감정의 이유
    val isFact: Boolean,           // 사실(true)인지 추측(false)인지
    val date: Long,                // 작성 날짜 (System.currentTimeMillis())

    var isReviewed: Boolean = false, // 회고 완료 여부
    var stillFact: Boolean? = null,  // 회고 시점의 사실/추측 여부
    var overcomeWay: String? = null, // 극복 방법 (Archive 화면에서 사용됨)
    var messageToMe: String? = null  // 나에게 한마디
)