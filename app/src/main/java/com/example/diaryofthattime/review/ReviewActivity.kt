package com.example.diaryofthattime.review

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.R
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.util.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewActivity : AppCompatActivity() {

    private var diaryId: Long = -1
    private var targetDiary: Diary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // 1. Intent 데이터 및 DB 연결 준비
        diaryId = intent.getLongExtra("diaryId", -1)
        if (diaryId == -1L) finish()

        val db = AppDatabase.getDatabase(this)

        // 2. UI 요소 연결 (XML ID와 맞춰주세요)
        val tvPastTitle = findViewById<TextView>(R.id.textPastTitle)
        val tvPastReason = findViewById<TextView>(R.id.textPastReason)
        val layoutInput = findViewById<LinearLayout>(R.id.layoutOvercomeInput)
        val etWay = findViewById<EditText>(R.id.editOvercomeWay)
        val etMessage = findViewById<EditText>(R.id.editMessageToMe)
        val btnSave = findViewById<Button>(R.id.btnSaveReview)

        // 3. 일주일 전 데이터 불러와서 화면에 세팅
        lifecycleScope.launch(Dispatchers.IO) {
            targetDiary = db.diaryDao().getDiaryById(diaryId.toInt())
            withContext(Dispatchers.Main) {
                targetDiary?.let {
                    tvPastTitle.text = it.title
                    tvPastReason.text = it.reason
                }
            }
        }

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

    private fun saveReviewData(db: AppDatabase, way: String, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 데이터 업데이트
            targetDiary?.apply {
                isReviewed = true
                overcomeWay = way
                messageToMe = message
            }

            targetDiary?.let { db.diaryDao().update(it) }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ReviewActivity, "극복 완료! 도장이 찍혔습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}