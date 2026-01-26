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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewFragment : Fragment(R.layout.diary_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val diaryId = arguments?.getInt("diaryId") ?: -1

        // 뷰 연결 (XML ID에 맞춰 수정 필요)
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

        // 뒤로가기
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        //수정
        btnEdit.setOnClickListener {
            // WriteActivity가 작성과 수정을 같이 한다고 가정할 때
            val intent = Intent(requireContext(), WriteActivity::class.java).apply {
                // 택배 상자(Intent)에 데이터 담기
                putExtra("diaryId", diaryId)      // 수정할 일기의 고유 ID
                putExtra("isEditMode", true)     // 수정 모드임을 알리는 신호
            }

            // 액티비티 실행
            startActivity(intent)

            // 안내 메시지
            Toast.makeText(requireContext(), "수정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }
        //삭제
        btnDelete.setOnClickListener {
            // 삭제 확인 팝업창(Dialog) 바로 생성
            AlertDialog.Builder(requireContext())
                .setTitle("일기 삭제")
                .setMessage("이 일기를 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    // [확인] 버튼을 눌렀을 때만 실제 DB 삭제 로직 실행
                    lifecycleScope.launch {
                        try {
                            val db = AppDatabase.getDatabase(requireContext())
                            val diary = db.diaryDao().getDiaryById(diaryId)

                            if (diary != null) {
                                db.diaryDao().delete(diary) // DB에서 삭제
                                Toast.makeText(
                                    requireContext(),
                                    "일기가 삭제되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                //삭제 완료 후 이전 리스트 화면으로 돌아가기
                                parentFragmentManager.popBackStack()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "삭제 중 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("취소", null) // 취소 누르면 아무 일도 안 일어남
                .show()
        }

        // 데이터 로드 DB에서 데이터 가져오기
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val diary = db.diaryDao().getDiaryById(diaryId)
                // 사실/추측 태그 뷰 연결
                val txtType = view.findViewById<TextView>(R.id.viewType)

                txtTitle.text = diary.title

                // stillFact가 true면 "사실", false(또는 null)이면 "추측"으로 처리
                if (diary.stillFact == true) {
                    txtType.text = "사실"
                    // 직접 색상을 지정하거나, 만들어둔 drawable이 있다면 setBackgroundResource 사용
                    txtType.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // 초록색 (사실)
                } else {
                    txtType.text = "추측"
                    txtType.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // 주황색 (추측)
                }
                // 💡 텍스트 색상을 흰색으로 고정 (배경색이 진하므로)
                txtType.setTextColor(android.graphics.Color.WHITE)
                txtDate.text =
                    SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date(diary.date))
                txtContent.text = diary.reason
                txtMessage.text = diary.messageToMe ?: "나에게 남긴 메시지가 없습니다."

                // [추가] 감정 텍스트를 이미지 리소스로 변환하여 적용
                val emotionResId = getEmotionImage(diary.emotion)
                imgEmotion.setImageResource(emotionResId)

                // [조건부 표시] overcomeWay가 있을 때만 레이아웃을 보여줌
                if (!diary.overcomeWay.isNullOrBlank()) {
                    overcomeLayout.visibility = View.VISIBLE
                    txtOvercome.text = diary.overcomeWay
                    labelOvercome.text =
                        if (diary.emotion == "행복") "✨ 행복의 순간" else "💡 이렇게 이겨냈어요"
                } else {
                    overcomeLayout.visibility = View.VISIBLE
                    txtOvercome.text = "아직 기록된 극복 방법이 없어요."
                }
            } catch (e: Exception) {
                // 에러 처리: 로그를 남기고 사용자에게 알림
                Log.e("DiaryView", "Error loading diary data", e)
                Toast.makeText(requireContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                    .show()
                parentFragmentManager.popBackStack() // 에러 발생 시 리스트로 튕겨내기
            }
        }
    }
    // [추가] 감정 이름에 맞는 이미지 리소스를 반환하는 함수
    private fun getEmotionImage(emotion: String?): Int {
        return when (emotion) {
            "행복" -> R.drawable.ic_launcher_foreground   // 실제 이미지 파일명으로 수정하세요
            "무기력" -> R.drawable.ic_launcher_foreground
            "슬픔" -> R.drawable.ic_launcher_foreground
            "분노" -> R.drawable.ic_launcher_foreground
            "불안" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground // 기본 이미지
        }
    }
}