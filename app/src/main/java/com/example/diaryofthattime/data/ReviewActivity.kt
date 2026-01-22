package com.example.diaryofthattime

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityReviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private var targetDiary: Diary? = null // 회고할 일기 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 알림(Intent)으로부터 일기 ID를 전달받음
        val diaryId = intent.getIntExtra("DIARY_ID", -1)

        if (diaryId != -1) {
            loadDiaryData(diaryId)
        } else {
            Toast.makeText(this, "일기 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 2. [극복했어요!] 버튼 클릭 시 입력창 노출
        binding.btnOvercomeYes.setOnClickListener {
            binding.layoutOvercomeInput.visibility = View.VISIBLE
            binding.btnOvercomeNo.visibility = View.GONE // 아니오 버튼 숨기기
        }

        // 3. [아직 못했어요] 버튼 클릭 시 (어진 님 알림 로직과 연결될 부분)
        binding.btnOvercomeNo.setOnClickListener {
            Toast.makeText(this, "조금 더 시간이 필요하군요. 일주일 뒤에 다시 물어볼게요.", Toast.LENGTH_LONG).show()
            // TODO: 어진 님에게 일주일 뒤 재알림 예약 신호 보내기
            finish()
        }

        // 4. 최종 저장 버튼 클릭
        binding.btnSaveReview.setOnClickListener {
            updateDiaryWithReview()
        }
    }

    // DB에서 일주일 전 일기 불러오기
    private fun loadDiaryData(id: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary = db.diaryDao().getDiaryById(id)

            withContext(Dispatchers.Main) {
                targetDiary?.let {
                    // 과거 데이터 화면에 세팅
                    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
                    binding.textPastDate.text = "${sdf.format(it.date)}의 기록"
                    binding.textPastTitle.text = it.title
                    binding.textPastEmotion.text = "기분: ${it.emotion}"
                    binding.textPastReason.text = it.reason
                }
            }
        }
    }

    // 극복 일기 내용 DB에 업데이트
    private fun updateDiaryWithReview() {
        if (targetDiary == null) return

        val overcomeWay = binding.editOvercomeWay.text.toString()
        val messageToMe = binding.editMessageToMe.text.toString()
        val stillFact = binding.rbStillFact.isChecked // 여전히 사실인지 체크

        if (overcomeWay.isBlank() || messageToMe.isBlank()) {
            Toast.makeText(this, "모든 항목을 작성해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            // 기존 객체의 데이터 업데이트
            targetDiary?.apply {
                this.isReviewed = true
                this.stillFact = stillFact
                this.overcomeWay = overcomeWay
                this.messageToMe = messageToMe
            }

            // DB 업데이트 수행 (Dao에 update 함수가 있어야 함)
            targetDiary?.let { db.diaryDao().update(it) }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ReviewActivity, "오늘의 성장을 기록했습니다! ✨", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}