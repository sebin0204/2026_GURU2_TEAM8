package com.example.diaryofthattime

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.diaryofthattime.ui.main.MainFragment
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 알림 권한이 허용되어 있는지 확인하고, 없으면 요청합니다.
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        if (savedInstanceState == null) {
            // FragmentTransaction을 사용하여 프래그먼트를 화면에 붙입니다.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MainFragment())
                .commit()
        }

        // 랜덤 시간 알림 예약 (앱 켤 때 실행)
        scheduleRandomHappyNotification()
    }

    override fun onResume() {
        super.onResume()
        // 알림을 눌러서 들어왔는지 확인 후 팝업 띄우기
        if (intent.getBooleanExtra("SHOW_HAPPY_POPUP", false)) {
            val content = intent.getStringExtra("HAPPY_CONTENT") ?: ""
            val diaryId = intent.getIntExtra("DIARY_ID", -1) // ID 꺼내기
            showHappyPopup(content, diaryId)

            // 팝업을 한 번 띄웠으므로 데이터를 지워줍니다 (중복 방지)
            intent.removeExtra("SHOW_HAPPY_POPUP")
        }
    }

    private fun scheduleRandomHappyNotification() {
        // 1~24시간 사이의 랜덤한 예약 시간 생성
        //val randomDelay = (1..24).random().toLong()
        val randomDelay = 1L //임시로 1분뒤로 설정

        // 일회성 작업 예약
        val happyWorkRequest = OneTimeWorkRequestBuilder<HappyReminderWorker>()
            .setInitialDelay(randomDelay, TimeUnit.MINUTES) // 1분뒤여서 MINUTES로 수정 -> HOURS
            .build()

        // 시스템에 작업 등록
        WorkManager.getInstance(this).enqueueUniqueWork(
            "RandomHappyReminder",
            ExistingWorkPolicy.REPLACE, // 이미 예약된 게 있다면 유지
            happyWorkRequest
        )
    }

    // 팝업 클릭하면 나타나는 메시지 -> 그날 일기 페이지로 연결할 때 삭제
    private fun showHappyPopup(content: String, diaryId: Int) {
        // AlertDialog 임포트 시 androidx.appcompat.app.AlertDialog 선택
        AlertDialog.Builder(this)
            .setTitle("🌟 다시 보는 행복")
            .setMessage("[$content]\n\n이때의 행복한 마음을 다시 느껴보세요!")
            .setPositiveButton("내용 보기") { _, _ ->
                // 상세 페이지(DiaryViewFragment)로 이동
                if (diaryId != -1) {
                    val detailFragment = DiaryViewFragment().apply {
                        arguments = Bundle().apply {
                            putInt("diaryId", diaryId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }
}