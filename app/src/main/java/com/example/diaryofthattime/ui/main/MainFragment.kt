package com.example.diaryofthattime.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.diaryofthattime.R
import com.example.diaryofthattime.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        // 감정 기록 화면 이동 (박세빈 화면)
        binding.btnWriteEmotion.setOnClickListener {
            // 나중에 NavGraph 또는 FragmentTransaction 연결
        }

        // 감정 보관함 이동 (최윤정 화면)
        binding.btnArchive.setOnClickListener {
            // 나중에 Archive_main.kt 연결
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
