package com.example.diaryofthattime.data

import androidx.room.*
import com.example.diaryofthattime.EmotionCount
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    // 1. 기본 CRUD 기능
    @Insert
    suspend fun insert(diary: Diary) : Long

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<Diary>>

    @Update
    suspend fun update(diary: Diary)


    @Delete
    suspend fun delete(diary: Diary)

    // 2. [오류 해결] 메인 화면 출석 체크용 기능
    @Query("""
        SELECT COUNT(*) FROM diary_table
        WHERE date BETWEEN :start AND :end
    """)
    suspend fun hasDiaryOnDate(start: Long, end: Long): Int

    // 3. 메인 보고서용: 감정별 횟수 통계 (이번 달 기준)
    @Query("""
        SELECT emotion, COUNT(*) as count FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
        GROUP BY emotion 
        ORDER BY count DESC
    """)
    fun getMonthlyEmotionCounts(year: String, month: String): Flow<List<EmotionCount>>

    // 4. 상세 화면용: 특정 연도 + 특정 월 + 특정 감정의 일기 목록
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
          AND emotion = :emotion 
        ORDER BY date DESC
    """)
    fun getDiariesByMonthAndEmotion(year: String, month: String, emotion: String): Flow<List<Diary>>

    // 5. 요약 정보용: 특정 연도 + 특정 월의 전체 감정 데이터
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month
    """)
    fun getDiariesByMonth(year: String, month: String): Flow<List<Diary>>

    // 6. 행복한 감정만 가져오기 (테스트용)
    @Query("SELECT * FROM diary_table WHERE emotion = '행복'")
    fun getHappyDiariesSync(): List<Diary>

    // com.example.diaryofthattime.data.DiaryDao.kt
    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end")
    suspend fun getDiaryByDateRange(start: Long, end: Long): List<Diary>

    @Query("SELECT * FROM diary_table WHERE id = :id")
    suspend fun getDiaryById(id: Int): Diary?

    // com.example.diaryofthattime.data.DiaryDao.kt
    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiariesSync(): List<Diary> // 모든 일기를 리스트로 가져오는 함수
}