package com.example.diaryofthattime.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    // 일기 저장
    @Insert
    suspend fun insert(diary: Diary)

    // 전체 일기 목록 가져오기 (날짜 최신순)
    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<Diary>>

    // 특정 일기 수정하기 (회고 작성 시 사용)
    @Update
    suspend fun update(diary: Diary)

    // 특정 ID로 일기 하나만 가져오기
    @Query("SELECT * FROM diary_table WHERE id = :id")
    suspend fun getDiaryById(id: Int): Diary
}