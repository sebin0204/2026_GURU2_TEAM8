package com.example.diaryofthattime

import android.content.Intent
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
            Toast.makeText(this, "현재의 감정을 선택해주세요!", Toast.LENGTH_SHORT).show()
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
        val selectedQuestionId = binding.question.checkedRadioButtonId
        if (selectedQuestionId == -1) {
            Toast.makeText(this, "사실인지 추측인지 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        val isFact = binding.maybetrue.isChecked


        // D. 제목 가져오기(ID : editTextText2)
        val diaryTitle = binding.editTextText2.text.toString()
        if (diaryTitle.isBlank()) {
            Toast.makeText(this, "일기의 제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

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
                Toast.makeText(this@WriteActivity, "오늘의 일기가 성공적으로 저장되었습니다!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@WriteActivity, MainActivity::class.java)

                //사용자가 선택한 감정 DB에 담아 보내기
                intent.putExtra("SELECTED_EMOTION", emotion)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)

                finish() // 현재 화면 종료 (메인 화면으로 돌아감)
            }
        }
    }
}