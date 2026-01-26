package com.example.diaryofthattime.review

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.R
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityReviewBinding
import com.example.diaryofthattime.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding
    private var diaryId: Long = -1
    private var isEditMode: Boolean = false
    private var targetDiary: Diary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent 데이터 및 DB 연결 준비
        diaryId = intent.getLongExtra("diaryId", -1)
        if (diaryId == -1L) finish()

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        val db = AppDatabase.getDatabase(this)

        // 2. UI 요소 연결 (XML ID와 맞춰주세요)
        val tvPastTitle = findViewById<TextView>(R.id.textPastTitle)
        val tvPastReason = findViewById<TextView>(R.id.textPastReason)
        val layoutInput = findViewById<LinearLayout>(R.id.layoutOvercomeInput)
        val etWay = findViewById<EditText>(R.id.editOvercomeWay)
        val etMessage = findViewById<EditText>(R.id.editMessageToMe)
        val btnSave = findViewById<Button>(R.id.btnSaveReview)

        // 3. 일주일 전 데이터 불러와서 화면에 세팅
        loadDiaryData(db)

        // 4. [네, 극복했어요!] 버튼 클릭 시
        findViewById<Button>(R.id.btnOvercomeYes).setOnClickListener {
            layoutInput.visibility = View.VISIBLE // 입력창 보여주기
            it.visibility = View.GONE // '네' 버튼 숨기기
        }

        // 5. [아직 못했어요] 버튼 클릭 시 (재알람)
        findViewById<Button>(R.id.btnOvercomeNo).setOnClickListener {
            AlarmHelper.scheduleReviewAlarm(this, diaryId, System.currentTimeMillis())
            Toast.makeText(this, "일주일 뒤에 다시 물어볼게요.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 6. [회고 완료] 최종 저장 버튼
        btnSave.setOnClickListener {
            val way = etWay.text.toString()
            val message = etMessage.text.toString()

            if (way.isBlank() || message.isBlank()) {
                Toast.makeText(this, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveReviewData(db, way, message)
        }
    }
    private fun loadDiaryData(db: AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary = db.diaryDao().getDiaryById(diaryId.toInt())
            withContext(Dispatchers.Main) {
                targetDiary?.let {
                    // 과거 일기 정보 표시
                    binding.textPastTitle.text = it.title
                    binding.textPastReason.text = it.reason

                    // 💡 [수정] 수정 모드라면 기존 회고 데이터 채우기
                    if (isEditMode && it.isReviewed) {
                        binding.layoutOvercomeInput.visibility = View.VISIBLE
                        binding.btnOvercomeYes.visibility = View.GONE
                        binding.btnOvercomeNo.visibility = View.GONE

                        binding.editOvercomeWay.setText(it.overcomeWay)
                        binding.editMessageToMe.setText(it.messageToMe)

                        // 사실/추측 재판단 라디오 버튼 체크 (isFact와 별개인 stillFact 값)
                        if (it.stillFact == true) {
                            binding.rbStillFact.isChecked = true
                        } else {
                            binding.rbNotFact.isChecked = true
                        }
                    }
                }
            }
        }
    }
    private fun saveReviewData(db: AppDatabase, way: String, message: String) {

        // 💡 [수정] 라디오 그룹에서 현재 선택된 '사실/추측' 상태 가져오기
        val stillFactValue = binding.rbStillFact.isChecked

        if (way.isBlank() || message.isBlank()) {
            Toast.makeText(this, "내용을 모두 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // 데이터 업데이트
            targetDiary?.apply {
                isReviewed = true
                this.stillFact = stillFactValue // 재판단 결과 저장
                overcomeWay = way
                messageToMe = message
            }

            targetDiary?.let { db.diaryDao().update(it) }

            withContext(Dispatchers.Main) {
                val msg = if (isEditMode) "회고 내용이 수정되었습니다!" else "오늘의 성장을 기록했습니다! ✨"
                Toast.makeText(this@ReviewActivity, "극복 완료! 도장이 찍혔습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}