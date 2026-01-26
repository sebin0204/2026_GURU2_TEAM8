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

    //일기 삭제 기능 : 상세 화면에서 필요할 수 있는 삭제 기능을 미리 추가해두면 좋습니다.
    @Delete
    suspend fun delete(diary: Diary)

    /**
     * 1. 메인 보고서용: 감정별 횟수 통계
     * 그룹화(GROUP BY)를 통해 각 감정이 몇 번 기록되었는지 계산합니다.
     * 리사이클러뷰에서 횟수순 정렬을 할 때 기초 데이터가 됩니다.
     * 보고서에는 "이번 달" 통계가 필요하므로 연/월 조건을 추가했습니다.
     */
    @Query("""
        SELECT emotion, COUNT(*) as count FROM diary_table 
        WHERE strftime('%Y', date / 1000, 'unixepoch') = :year 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
        GROUP BY emotion 
        ORDER BY count DESC
    """)
    fun getMonthlyEmotionCounts(year: String, month: String): Flow<List<EmotionCount>>

    /**
     * 2. 상세 화면용: 특정 연도 + 특정 월 + 특정 감정의 일기 목록
     * 연도(%Y)와 월(%m)을 모두 체크하도록 수정했습니다.
     * :year와 :month는 "2026", "01" 형태의 문자열로 받는 것이 정확합니다.
     * (SQLite의 strftime 함수를 사용하여 월을 비교합니다)
     */
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date/ 1000, 'unixepoch') = :year 
          AND strftime('%m', date/ 1000, 'unixepoch') = :month 
          AND emotion = :emotion 
        ORDER BY date DESC
    """)
    fun getDiariesByMonthAndEmotion(year: String, month: String, emotion: String): Flow<List<Diary>>

    /**
     * 3. 요약 정보용: 특정 연도 + 특정 월의 전체 감정 데이터
     * 해당 연도의 해당 월 기록만 가져와서 가장 많은/적은 감정을 계산합니다.
     */
    @Query("""
        SELECT * FROM diary_table 
        WHERE strftime('%Y', date) = :year 
          AND strftime('%m', date) = :month
    """)
    fun getDiariesByMonth(year: String, month: String): Flow<List<Diary>>

    //행복한 감정만 가져오기
    @Query("SELECT * FROM diary_table WHERE emotion = '행복'")
    fun getHappyDiariesSync(): List<Diary>
}