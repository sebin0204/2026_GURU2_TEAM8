package com.example.diaryofthattime.data

// Room 쿼리 결과를 담기 위해 필드명이 쿼리의 'as count'와 일치해야 합니다.
data class EmotionCount(
    val emotion: String,
    val count: Int
)