package com.example.diaryofthattime.review

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
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

        // 1. Intent 데이터 받기 (Archive_detail에서 보내는 "diaryId"와 일치)
        diaryId = intent.getIntExtra("diaryId", -1)

        // 만약 Archive_detail에서 Long으로 보냈을 경우를 대비한 방어 코드
        if (diaryId == -1) {
            diaryId = intent.getLongExtra("diaryId", -1L).toInt()
        }

        if (diaryId == -1) {
            Toast.makeText(this, "일기 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        isEditMode = intent.getBooleanExtra("isEditMode", false)
        val db = AppDatabase.getDatabase(this)

        // 2. DB에서 데이터 로드
        loadDiaryData(db)

        // 3. [네, 극복했어요] 버튼
        binding.btnOvercomeYes.setOnClickListener {
            binding.layoutOvercomeInput.visibility = View.VISIBLE
            binding.btnOvercomeYes.visibility = View.GONE
            binding.btnOvercomeNo.visibility = View.GONE
        }

        // 4. [아직 못했어요] 버튼 (알람 재예약)
        binding.btnOvercomeNo.setOnClickListener {
            // 매개변수 명칭(createdAt 등) 생략하고 값만 전달하여 오류 방지
            AlarmHelper.scheduleReviewAlarm(
                this,
                diaryId.toLong(),
                System.currentTimeMillis()
            )
            Toast.makeText(this, "일주일 뒤에 다시 물어볼게요.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 5. [회고 완료] 저장 버튼
        binding.btnSaveReview.setOnClickListener {
            saveReviewData(db)
        }

        // 뒤로가기 버튼 (상단바 등이 있다면)
        // onCreate 내부
        val btnBack = findViewById<ImageButton>(com.example.diaryofthattime.R.id.btnBack)
        btnBack?.setOnClickListener {
            finish()
        }
    }

    private fun loadDiaryData(db: AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary = db.diaryDao().getDiaryById(diaryId)

            withContext(Dispatchers.Main) {
                targetDiary?.let { diary ->
                    // UI 세팅
                    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
                    binding.textPastDate.text = "${sdf.format(diary.date)}의 기록"
                    binding.textPastTitle.text = diary.title
                    binding.textPastEmotion.text = "기분: ${diary.emotion}"
                    binding.textPastReason.text = diary.reason

                    // 수정 모드인 경우 기존 값 채우기
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
                } ?: run {
                    Toast.makeText(this@ReviewActivity, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun saveReviewData(db: AppDatabase) {
        val way = binding.editOvercomeWay.text.toString()
        val message = binding.editMessageToMe.text.toString()
        val stillFactValue = binding.rbStillFact.isChecked

        if (way.isBlank() || message.isBlank()) {
            Toast.makeText(this, "모든 항목을 작성해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary?.let { diary ->
                // 객체 데이터 업데이트
                diary.isReviewed = true
                diary.stillFact = stillFactValue
                diary.overcomeWay = way
                diary.messageToMe = message

                db.diaryDao().update(diary)

                withContext(Dispatchers.Main) {
                    val msg = if (isEditMode) "회고가 수정되었습니다." else "성공적으로 기록되었습니다! ✨"
                    Toast.makeText(this@ReviewActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}