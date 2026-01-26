package com.example.diaryofthattime.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.Archive_main
import com.example.diaryofthattime.DiaryDetailActivity // 상세 페이지 import 추가
import com.example.diaryofthattime.DiaryViewFragment
import com.example.diaryofthattime.R
import com.example.diaryofthattime.ReviewActivity
import com.example.diaryofthattime.WriteActivity
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var lastClickedTime: Long = 0
    private var lastClickedDate: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        // 달력 잔상을 막기 위해 현재 시간으로 명시적 설정
        binding.calendarView.date = System.currentTimeMillis()

        // 1. 감정 기록 화면 이동 (박세빈 화면)
        binding.btnWriteEmotion.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java)
            startActivity(intent)
        }

        // 2. 감정 보관함 이동 (최윤정 화면)
        binding.btnArchive.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, Archive_main())
                .addToBackStack(null)
                .commit()
        }

        /* -------------------------
         * 3. 달력 날짜 선택 → 출석 여부 판단 및 상세 페이지 이동
         * ------------------------- */
        val diaryDao = AppDatabase.getDatabase(requireContext()).diaryDao()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val currentTime = System.currentTimeMillis()
            val selectedDate = "$year-$month-$dayOfMonth"

            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis

            calendar.set(year, month, dayOfMonth, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis

            lifecycleScope.launch(Dispatchers.IO) {
                // 해당 날짜의 일기 객체를 직접 가져옵니다.
                val diaryList: List<Diary> = diaryDao.getDiaryByDateRange(startOfDay, endOfDay)

                withContext(Dispatchers.Main) {
                    if (diaryList.isNotEmpty()) {
                        binding.txtAttendanceStatus.text = "출석 완료 ✔"

                        // ★ 일기가 있는 날짜를 더블 클릭하면 상세 페이지로 이동 ★
                        if (currentTime - lastClickedTime < 300 && lastClickedDate == selectedDate) {

                            if (diaryList.size > 1) {
                                // 일기가 여러 개일 때는 목록 팝업 띄우기
                                showDiaryListDialog(diaryList, month, dayOfMonth)

                            } else {
                                // 일기가 하나일 때는 바로 이동 여부 묻기
                                showSingleDiaryDialog(diaryList[0], month, dayOfMonth)
                            }
                        }
                    } else {
                        binding.txtAttendanceStatus.text = "아직 기록이 없어요"
                    }
                    // 마지막 클릭 정보 업데이트
                    lastClickedTime = currentTime
                    lastClickedDate = selectedDate
                }
            }
        }
    }

    // 감정별 추천 극복 활동 팝업 (기존 로직 유지)
    override fun onResume() {
        super.onResume()
        val emotion = activity?.intent?.getStringExtra("SELECTED_EMOTION")
        if (emotion != null) {
            showRecommendPopup(emotion)
            activity?.intent?.removeExtra("SELECTED_EMOTION")
        }
    }

    private fun showRecommendPopup(emotion: String) {
        val recommendation = when (emotion) {
            "무기력" -> "가벼운 산책이나 스트레칭은 어떨까요? 작은 움직임이 에너지를 줄 거예요."
            "슬픔" -> "슬픈 감정을 억누르지 말고, 따뜻한 차 한 잔과 함께 좋아하는 음악을 들어보세요."
            "불안" -> "6초간 숨을 들이마시고, 3초간 참고, 7초간 천천히 내뱉어보세요. 모든 일은 결국 다 지나가요."
            "화남" -> "잠시 눈을 감고 10부터 1까지 천천히 세어보는 건 어떨까요?"
            else -> "오늘 하루도 정말 고생 많으셨어요. 당신을 응원합니다! ✨"
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("${emotion}을(를) 느끼고 계시는군요")
            .setMessage(recommendation)
            .setPositiveButton("확인", null)
            .show()
    }

    //일기가 여러 개일 때 선택 목록을 보여주는 다이얼로그
    private fun showDiaryListDialog(diaryList: List<Diary>, month: Int, dayOfMonth: Int) {
        // 다이얼로그에 표시할 제목 리스트 생성 (내용의 일부나 시간을 표시)
        val items = diaryList.mapIndexed { index, diary ->
            "${index + 1}. ${diary.title}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("${month + 1}월 ${dayOfMonth}일의 일기 목록")
            .setItems(items) { _, which ->
                // 선택한 순번에 해당하는 일기 ID 전달
                navigateToDetail(diaryList[which].id)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    //일기가 하나일 때 기존처럼 이동 여부를 묻는 다이얼로그
    private fun showSingleDiaryDialog(diary: Diary, month: Int, dayOfMonth: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("일기 확인")
            .setMessage("${month + 1}월 ${dayOfMonth}일의 일기로 이동하시겠습니까?")
            .setPositiveButton("이동") { _, _ ->
                navigateToDetail(diary.id)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    //상세 페이지로 Fragment 교체 및 ID 전달 공통 함수
    private fun navigateToDetail(diaryId: Int) {
        val fragment = DiaryViewFragment().apply {
            arguments = Bundle().apply {
                putInt("diaryId", diaryId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}