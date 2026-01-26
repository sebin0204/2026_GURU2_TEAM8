package com.example.diaryofthattime.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.R
import com.example.diaryofthattime.Archive_main
import com.example.diaryofthattime.WriteActivity
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.databinding.FragmentMainBinding
import com.example.diaryofthattime.ui.archive.ArchiveFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        // 감정 기록 화면 이동 (박세빈 화면)
        binding.btnWriteEmotion.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java)
            startActivity(intent)
        }

        // 감정 보관함 이동 (최윤정 화면)
        binding.btnArchive.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, Archive_main())
                .addToBackStack(null)
                .commit()
        }

        /* -------------------------
         * 3. 달력 날짜 선택 → 출석 여부 판단
         * ------------------------- */
        val diaryDao = AppDatabase
            .getDatabase(requireContext())
            .diaryDao()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            // Calendar는 month가 0부터 시작
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startOfDay = calendar.timeInMillis

            calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val endOfDay = calendar.timeInMillis

            // DB에서 해당 날짜 출석 여부 확인
            lifecycleScope.launch(Dispatchers.IO) {
                val count = diaryDao.hasDiaryOnDate(startOfDay, endOfDay)

                withContext(Dispatchers.Main) {
                    if (count > 0) {
                        binding.txtAttendanceStatus.text = "출석 완료 ✔"
                    } else {
                        binding.txtAttendanceStatus.text = "아직 기록이 없어요"
                    }
                }
            }
        }
    }

    //감정별 추천 극복 활동
    override fun onResume() {
        super.onResume()

        // MainActivity로부터 감정 데이터를 확인
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
