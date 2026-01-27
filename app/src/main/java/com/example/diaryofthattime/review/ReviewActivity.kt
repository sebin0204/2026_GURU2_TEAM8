package com.example.diaryofthattime.review

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityReviewBinding
import com.example.diaryofthattime.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private var diaryId: Int = -1
    private var isEditMode: Boolean = false
    private var targetDiary: Diary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent 데이터 받기
        diaryId = intent.getIntExtra("DIARY_ID", -1)
        if (diaryId == -1) {
            Toast.makeText(this, "일기 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        val db = AppDatabase.getDatabase(this)

        // 2. DB에서 과거 일기 로드
        loadDiaryData(db)

        // 3. [네, 극복했어요]
        binding.btnOvercomeYes.setOnClickListener {
            binding.layoutOvercomeInput.visibility = View.VISIBLE
            binding.btnOvercomeYes.visibility = View.GONE
            binding.btnOvercomeNo.visibility = View.GONE
        }

        // 4. [아직 못했어요] → 재알림
        binding.btnOvercomeNo.setOnClickListener {
            AlarmHelper.scheduleReviewAlarm(
                context = this,
                diaryId = diaryId.toLong(),
                createdAt = System.currentTimeMillis()
            )
            Toast.makeText(this, "일주일 뒤에 다시 물어볼게요.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 5. [회고 완료] 저장
        binding.btnSaveReview.setOnClickListener {
            saveReviewData(db)
        }
    }

    // 🔹 과거 일기 불러오기 (data/ReviewActivity 기능 포함)
    private fun loadDiaryData(db: AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary = db.diaryDao().getDiaryById(diaryId)

            withContext(Dispatchers.Main) {
                targetDiary?.let { diary ->

                    // 날짜 표시 (data/ReviewActivity 기능)
                    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
                    binding.textPastDate.text = "${sdf.format(diary.date)}의 기록"

                    // 기본 정보
                    binding.textPastTitle.text = diary.title
                    binding.textPastEmotion.text = "기분: ${diary.emotion}"
                    binding.textPastReason.text = diary.reason

                    // 수정 모드 처리
                    if (isEditMode && diary.isReviewed) {
                        binding.layoutOvercomeInput.visibility = View.VISIBLE
                        binding.btnOvercomeYes.visibility = View.GONE
                        binding.btnOvercomeNo.visibility = View.GONE

                        binding.editOvercomeWay.setText(diary.overcomeWay)
                        binding.editMessageToMe.setText(diary.messageToMe)

                        if (diary.stillFact == true) {
                            binding.rbStillFact.isChecked = true
                        } else {
                            binding.rbNotFact.isChecked = true
                        }
                    }
                }
            }
        }
    }

    // 🔹 회고 내용 저장 (기존 review 기능 + data 기능 통합)
    private fun saveReviewData(db: AppDatabase) {
        val overcomeWay = binding.editOvercomeWay.text.toString()
        val messageToMe = binding.editMessageToMe.text.toString()
        val stillFactValue = binding.rbStillFact.isChecked

        if (overcomeWay.isBlank() || messageToMe.isBlank()) {
            Toast.makeText(this, "모든 항목을 작성해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary?.apply {
                isReviewed = true
                stillFact = stillFactValue
                this.overcomeWay = overcomeWay
                this.messageToMe = messageToMe
            }
            // [수정] targetDiary 객체의 값을 직접 수정하고 업데이트합니다.
            targetDiary?.let { diary ->
                diary.isReviewed = true // 이 값이 true여야 상세페이지에서 도장이 보입니다!
                diary.stillFact = stillFactValue
                diary.overcomeWay = way
                diary.messageToMe = message

                db.diaryDao().update(diary)

                // 데이터가 잘 저장됐는지 로그캣에 찍어봅니다.
                android.util.Log.d("DiaryTest", "저장 완료! isReviewed: ${diary.isReviewed}")
            }

            withContext(Dispatchers.Main) {
                val msg = if (isEditMode) {
                    "회고 내용이 수정되었습니다!"
                } else {
                    "오늘의 성장을 기록했습니다! ✨"
                }
                Toast.makeText(this@ReviewActivity, msg, Toast.LENGTH_SHORT).show()
                Toast.makeText(this@ReviewActivity, "극복 완료!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
