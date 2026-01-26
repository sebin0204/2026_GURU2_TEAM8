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

    // 수정 모드인지 판별하기 위한 변수
    private var diaryId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 💡 [수정] 외부(Fragment 등)에서 보낸 데이터(ID와 수정모드 여부)를 받습니다.
        diaryId = intent.getIntExtra("diaryId", -1)
        isEditMode = intent.getBooleanExtra("isEditMode", false)

        // 2. 오늘 날짜 표시 (ID: textDate)
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        binding.textDate.text = sdf.format(System.currentTimeMillis())

        // 수정 모드라면 기존 데이터를 불러와서 화면에 세팅
        if (isEditMode && diaryId != -1) {
            loadExistingData()
            binding.write.text = "수정하기" // 버튼 텍스트 변경
        }

        // 3. 기록하기 버튼 클릭 (ID: write)
        binding.write.setOnClickListener {
            saveDiaryData()
        }
    }
    // 기존 데이터를 불러와서 EditText와 RadioButton에 채워주는 함수
    private fun loadExistingData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val diary = db.diaryDao().getDiaryById(diaryId)

            withContext(Dispatchers.Main) {
                diary?.let {
                    binding.editTextText2.setText(it.title)      // 제목
                    binding.editTextText.setText(it.reason)      // 내용

                    // 감정(RadioButton) 체크 처리
                    for (i in 0 until binding.emotion.childCount) {
                        val rb = binding.emotion.getChildAt(i) as RadioButton
                        if (rb.text.toString() == it.emotion) {
                            rb.isChecked = true
                            break
                        }
                    }

                    // 사실 여부 체크 처리
                    if (it.isFact) binding.maybetrue.isChecked = true
                    else binding.maybefalse.isChecked = true // false일 때의 ID가 maybefalse라고 가정
                }
            }
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
        val DiaryData = Diary(
            id = if (isEditMode) diaryId else 0, //수정이면 기존 ID, 새 저장이면 0
            title = diaryTitle,
            emotion = emotion,
            reason = reason,
            isFact = isFact,
            date = System.currentTimeMillis(),
            isReviewed = isEditMode
        )

        // F. Room DB에 저장 (비동기 처리)
        val db = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            if (isEditMode) {
                // 기존 일기 수정 (Update)
                db.diaryDao().update(DiaryData)
            } else {
                // 새 일기 저장 (Insert)
                db.diaryDao().insert(DiaryData)
            }

            // 작업 완료 후 메인 스레드에서 화면 닫기
            withContext(Dispatchers.Main) {
                val msg = if (isEditMode) "일기가 수정되었습니다!" else "오늘의 일기가 저장되었습니다!"
                Toast.makeText(this@WriteActivity, msg, Toast.LENGTH_SHORT).show()

                val intent = Intent(this@WriteActivity, MainActivity::class.java)
                intent.putExtra("SELECTED_EMOTION", emotion)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}