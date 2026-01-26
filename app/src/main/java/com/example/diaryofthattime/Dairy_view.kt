package com.example.diaryofthattime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.data.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewFragment : Fragment(R.layout.diary_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val diaryId = arguments?.getInt("diaryId") ?: -1

        // 뷰 연결
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
        val txtTitle = view.findViewById<TextView>(R.id.viewTitle)
        val txtDate = view.findViewById<TextView>(R.id.viewDate)
        val imgEmotion = view.findViewById<ImageView>(R.id.imgEmotion)
        val txtContent = view.findViewById<TextView>(R.id.viewContent)
        val txtMessage = view.findViewById<TextView>(R.id.viewMessage)
        val overcomeLayout = view.findViewById<View>(R.id.overcomeLayout)
        val labelOvercome = view.findViewById<TextView>(R.id.labelOvercome)
        val txtOvercome = view.findViewById<TextView>(R.id.viewOvercome)
        val txtType = view.findViewById<TextView>(R.id.viewType)

        // 뒤로가기
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 수정 화면 이동
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra("diaryId", diaryId)
                putExtra("isEditMode", true)
            }
            startActivity(intent)
            Toast.makeText(requireContext(), "수정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 삭제 로직
        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("일기 삭제")
                .setMessage("이 일기를 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(requireContext())
                            val diary = db.diaryDao().getDiaryById(diaryId)

                            if (diary != null) {
                                db.diaryDao().delete(diary)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 데이터 로드 (핵심 수정 부분: ?. 세이프 콜 적용)
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val diary = db.diaryDao().getDiaryById(diaryId)

                if (diary != null) {
                    txtTitle.text = diary.title

                    // 사실/추측 태그 설정
                    if (diary.stillFact == true) {
                        txtType.text = "사실"
                        txtType.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                    } else {
                        txtType.text = "추측"
                        txtType.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                    }
                    txtType.setTextColor(android.graphics.Color.WHITE)

                    // 날짜 및 내용
                    txtDate.text = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date(diary.date))
                    txtContent.text = diary.reason
                    txtMessage.text = diary.messageToMe ?: "나에게 남긴 메시지가 없습니다."

                    // 감정 이미지
                    imgEmotion.setImageResource(getEmotionImage(diary.emotion))

                    // 극복 방법 표시 로직
                    if (!diary.overcomeWay.isNullOrBlank()) {
                        overcomeLayout.visibility = View.VISIBLE
                        txtOvercome.text = diary.overcomeWay
                        labelOvercome.text = if (diary.emotion == "행복") "✨ 행복의 순간" else "💡 이렇게 이겨냈어요"
                    } else {
                        overcomeLayout.visibility = View.VISIBLE
                        txtOvercome.text = "아직 기록된 극복 방법이 없어요."
                    }
                } else {
                    // diary가 null인 경우 처리
                    Toast.makeText(requireContext(), "일기 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Log.e("DiaryView", "Error loading diary data", e)
                Toast.makeText(requireContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun getEmotionImage(emotion: String?): Int {
        return when (emotion) {
            "행복" -> R.drawable.ic_launcher_foreground
            "무기력" -> R.drawable.ic_launcher_foreground
            "슬픔" -> R.drawable.ic_launcher_foreground
            "분노" -> R.drawable.ic_launcher_foreground
            "불안" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }
    }
}