package com.example.diaryofthattime

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityDiaryDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DiaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent로 넘어온 일기 ID 받기
        val diaryId = intent.getIntExtra("DIARY_ID", -1)
        if (diaryId == -1) {
            finish()
            return
        }

        // 2. DB에서 해당 일기 데이터 불러오기
        loadDiaryData(diaryId)
    }

    private fun loadDiaryData(diaryId: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val diary = db.diaryDao().getDiaryById(diaryId) //

            withContext(Dispatchers.Main) {
                diary?.let { displayDiary(it) }
            }
        }
    }

    private fun displayDiary(diary: Diary) {
        // 3. 기본 정보 표시
        binding.textDetailTitle.text = diary.title
        binding.textDetailReason.text = diary.reason
        binding.textDetailEmotion.text = "그날의 기분: ${diary.emotion}"

        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        binding.textDetailDate.text = sdf.format(Date(diary.date))

        // 4. 회고 완료 여부에 따른 도장 및 결과 표시
        if (diary.isReviewed) {
            // 회고 데이터 표시
            binding.layoutOvercomeResult.visibility = View.VISIBLE
            binding.textOvercomeWay.text = diary.overcomeWay
            binding.textMessageToMe.text = "미래의 나에게: ${diary.messageToMe}"
            binding.imgOvercomeStamp.apply {
                visibility = View.VISIBLE
                elevation = 100f      // 노란 박스보다 훨씬 높게 띄움
                bringToFront()        // 레이어 순서를 맨 앞으로
                requestLayout()       // 화면 갱신 강제 요청
            }

            // ★ 도장 보여주기 및 애니메이션 ★
            binding.imgOvercomeStamp.visibility = View.VISIBLE //

            // 도장이 '쾅' 하고 찍히는 느낌의 애니메이션 추가
            val stampAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            binding.imgOvercomeStamp.startAnimation(stampAnim)
            binding.imgOvercomeStamp.visibility = View.VISIBLE
        } else {
            binding.layoutOvercomeResult.visibility = View.GONE
            binding.imgOvercomeStamp.visibility = View.GONE
        }
    }
}