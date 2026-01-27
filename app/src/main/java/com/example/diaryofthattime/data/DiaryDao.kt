package com.example.diaryofthattime.data

import androidx.room.*
// 💡 만약 EmotionCount를 data 패키지로 옮겼다면 아래 import는 필요 없거나 경로가 바뀌어야 합니다.
// import com.example.diaryofthattime.EmotionCount
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    // 1. 기본 CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: Diary)

    @Update
    suspend fun update(diary: Diary)

    @Delete
    suspend fun delete(diary: Diary)

    @Query("SELECT * FROM diary_table WHERE id = :id")
    suspend fun getDiaryById(id: Int): Diary?

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<Diary>>

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiariesSync(): List<Diary>

    // 2. 날짜 범위 및 출석 체크
    @Query("SELECT COUNT(*) FROM diary_table WHERE date BETWEEN :start AND :end")
    suspend fun hasDiaryOnDate(start: Long, end: Long): Int

    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end")
    suspend fun getDiaryByDateRange(start: Long, end: Long): List<Diary>

    // 3. 월별 통계 및 목록
    @Query("""
        SELECT emotion, COUNT(*) as count FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
        GROUP BY emotion 
        ORDER BY count DESC
    """)
    fun getMonthlyEmotionCounts(year: String, month: String): Flow<List<EmotionCount>>

    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month
    """)
    fun getDiariesByMonth(year: String, month: String): Flow<List<Diary>>

    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
          AND emotion = :emotion 
        ORDER BY date DESC
    """)
    fun getDiariesByMonthAndEmotion(year: String, month: String, emotion: String): Flow<List<Diary>>

    @Query("SELECT * FROM diary_table WHERE emotion = '행복'")
    fun getHappyDiariesSync(): List<Diary>
}