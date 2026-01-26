package com.example.diaryofthattime.data

import androidx.room.*
import com.example.diaryofthattime.EmotionCount
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

    @Query("""
        SELECT COUNT(*) FROM diary_table
        WHERE date BETWEEN :start AND :end
    """)
    suspend fun hasDiaryOnDate(start: Long, end: Long): Int

    /**
     * 1. 메인 보고서용: 감정별 횟수 통계
     */
    @Query("SELECT emotion, COUNT(*) as count FROM diary_table GROUP BY emotion")
    fun getEmotionCounts(): Flow<List<EmotionCount>>

    /**
     * 2. 상세 화면용: 특정 연도 + 특정 월 + 특정 감정의 일기 목록
     */
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
          AND emotion = :emotion 
        ORDER BY date DESC
    """)
    fun getDiariesByMonthAndEmotion(year: String, month: String, emotion: String): Flow<List<Diary>>

    /**
     * 3. 요약 정보용: 특정 연도 + 특정 월의 전체 감정 데이터
     */
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month
    """)
    fun getDiariesByMonth(year: String, month: String): Flow<List<Diary>>
}