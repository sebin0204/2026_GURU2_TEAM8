package com.example.diaryofthattime

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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

    private lateinit var binding: ActivityWriteBinding
    private var diaryId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [수정] 외부(Fragment 등)에서 보낸 데이터(ID와 수정모드 여부)를 받습니다.
        diaryId = intent.getIntExtra("diaryId", -1)
        isEditMode = intent.getBooleanExtra("isEditMode", false)

        // 2. 오늘 날짜 표시 (ID: textDate)
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        binding.textDate.text = sdf.format(System.currentTimeMillis())

        // ★ 3. 최신 방식의 뒤로가기 콜백 설정 (onBackPressed 오류 해결)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val title = binding.editTextText2.text.toString()
                val content = binding.editTextText.text.toString()

                // 제목이나 내용이 입력되어 있으면 확인 팝업 출력
                if (title.isNotBlank() || content.isNotBlank()) {
                    AlertDialog.Builder(this@WriteActivity)
                        .setTitle("작성 취소")
                        .setMessage("작성 중인 일기가 저장되지 않습니다. 나가시겠습니까?")
                        .setPositiveButton("나가기") { _, _ ->
                            isEnabled = false // 콜백 비활성화
                            onBackPressedDispatcher.onBackPressed() // 실제 뒤로가기 수행
                        }
                        .setNegativeButton("계속 쓰기", null)
                        .show()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // 4. 상단 뒤로가기 버튼 클릭 리스너 (XML에 ID: btnBack이 있어야 함)
        binding.btnBack.setOnClickListener {
            callback.handleOnBackPressed()
        }

        // 5. 수정 모드 데이터 로드
        if (isEditMode && diaryId != -1) {
            loadExistingData()
            binding.write.text = "수정하기"
        }

        // 6. 기록하기 버튼 클릭
        binding.write.setOnClickListener {
            saveDiaryData()
        }
    }

    private fun loadExistingData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val diary = db.diaryDao().getDiaryById(diaryId) //

            withContext(Dispatchers.Main) {
                diary?.let {
                    binding.editTextText2.setText(it.title)
                    binding.editTextText.setText(it.reason)

                    for (i in 0 until binding.emotion.childCount) {
                        val rb = binding.emotion.getChildAt(i) as RadioButton
                        if (rb.text.toString() == it.emotion) {
                            rb.isChecked = true
                            break
                        }
                    }

                    if (it.isFact) binding.maybetrue.isChecked = true
                    else binding.maybefalse.isChecked = true
                }
            }
        }
    }

    private fun saveDiaryData() {
        val selectedEmotionId = binding.emotion.checkedRadioButtonId
        if (selectedEmotionId == -1) {
            Toast.makeText(this, "현재의 감정을 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        val emotion = findViewById<RadioButton>(selectedEmotionId).text.toString()

        val reason = binding.editTextText.text.toString()
        if (reason.isBlank()) {
            Toast.makeText(this, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedQuestionId = binding.question.checkedRadioButtonId
        if (selectedQuestionId == -1) {
            Toast.makeText(this, "사실인지 추측인지 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }
        val isFact = binding.maybetrue.isChecked

        val diaryTitle = binding.editTextText2.text.toString()
        if (diaryTitle.isBlank()) {
            Toast.makeText(this, "일기의 제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val existingDiary = if (isEditMode) db.diaryDao().getDiaryById(diaryId) else null
            val diaryData = Diary(
                id = if (isEditMode) diaryId else 0,
                title = diaryTitle,
                emotion = emotion,
                reason = reason,
                isFact = isFact,
                date = System.currentTimeMillis()
            )

            if (isEditMode) {
                db.diaryDao().update(diaryData)
            } else {
                db.diaryDao().insert(diaryData)
            }

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