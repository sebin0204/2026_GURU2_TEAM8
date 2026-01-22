package com.example.diaryofthattime

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityWriteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class WriteActivity : AppCompatActivity() {

    // 1. 뷰 바인딩 설정
    private lateinit var binding: ActivityWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 오늘 날짜 표시 (ID: textDate)
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        binding.textDate.text = sdf.format(System.currentTimeMillis())

        // 3. 기록하기 버튼 클릭 (ID: write)
        binding.write.setOnClickListener {
            saveDiaryData()
        }
    }

    private fun saveDiaryData() {
        // A. 선택된 감정 가져오기 (RadioGroup ID: emotion)
        val selectedEmotionId = binding.emotion.checkedRadioButtonId
        if (selectedEmotionId == -1) {
            Toast.makeText(this, "감정을 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        val emotion = findViewById<RadioButton>(selectedEmotionId).text.toString()

        // B. 작성한 내용 가져오기 (EditText ID: editTextText)
        val reason = binding.editTextText.text.toString()
        if (reason.isBlank()) {
            Toast.makeText(this, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        // C. 사실 여부 가져오기 (RadioButton ID: maybetrue 가 체크되었는지 확인)
        val isFact = binding.maybetrue.isChecked

        // D. 제목 가져오기(ID : editTextText2)
        val diaryTitle = binding.editTextText2.text.toString()

        // E. 저장할 Diary 객체 생성
        val newDiary = Diary(
            title = diaryTitle,
            emotion = emotion,
            reason = reason,
            isFact = isFact,
            date = System.currentTimeMillis(),
            isReviewed = false
        )

        // F. Room DB에 저장 (비동기 처리)
        val db = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            db.diaryDao().insert(newDiary)

            // 작업 완료 후 메인 스레드에서 화면 닫기
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WriteActivity, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 현재 화면 종료 (메인 화면으로 돌아감)
            }
        }
    }
}