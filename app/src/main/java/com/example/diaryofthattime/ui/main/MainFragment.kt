package com.example.diaryofthattime.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diaryofthattime.R
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

        /* -------------------------
         * 1. 감정 기록 화면 이동
         * ------------------------- */
        binding.btnWriteEmotion.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java)
            startActivity(intent)
        }

        /* -------------------------
         * 2. 감정 보관함 이동
         * ------------------------- */
        binding.btnArchive.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ArchiveFragment())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
