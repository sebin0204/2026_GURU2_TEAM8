package com.example.diaryofthattime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diaryofthattime.data.AppDatabase
import kotlinx.coroutines.launch
import java.util.Calendar

// 통계 데이터를 담기 위한 간단한 클래스
//DB용 데이터 (Dao에서 사용)
data class EmotionCount(
    val emotion: String,
    val count: Int
)

//UI용 데이터 (화면에 보여줄 정보들)
data class EmotionReport(
    val name: String,
    val count: Int,
    val bestWay: String
)

class Archive_main : Fragment(R.layout.archive_emotion){

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1~12

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerEmotionCards)
        val txtSummary = view.findViewById<TextView>(R.id.txtSummaryMost)
        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)

        // 날짜 이동 버튼
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        updateUI(txtDate, txtSummary, recyclerView)
        //이전달
        btnPrev.setOnClickListener {
            if (currentMonth > 1) currentMonth-- else {
                currentMonth = 12; currentYear--
            }
            updateUI(txtDate, txtSummary, recyclerView)
        }
        //이번달
        btnNext.setOnClickListener {
            if (currentMonth < 12) currentMonth++ else {
                currentMonth = 1; currentYear++
            }
            updateUI(txtDate, txtSummary, recyclerView)
        }
    }
    private fun updateUI(txtDate: TextView, txtSummary: TextView, rv: RecyclerView) {
        val yearStr = currentYear.toString()
        val monthStr = String.format("%02d", currentMonth)
        txtDate.text = "${yearStr}년 ${monthStr}월"

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            // 1. 해당 월의 모든 일기 가져오기
            db.diaryDao().getDiariesByMonth(yearStr, monthStr).collect { diaries ->

                // 2. 5개 감정 고정 리스트 생성
                val fixedEmotions = listOf("행복", "무기력", "슬픔", "불안", "화남")

                // 3. DB 데이터를 바탕으로 5개 카드 데이터 매핑
                val reportList = fixedEmotions.map { emotionName ->
                    val filteredDiaries = diaries.filter { it.emotion == emotionName }
                    val count = filteredDiaries.size

                    // 해당 감정의 가장 최근 'overcomeWay' 가져오기 (없으면 기본문구)
                    val lastOvercome = filteredDiaries.firstOrNull { !it.overcomeWay.isNullOrBlank() }?.overcomeWay
                        ?: "아직 기록된 극복 방법이 없어요."

                    EmotionReport(
                        name = emotionName,
                        count = count,
                        bestWay = lastOvercome
                    )
                }

                // 상단 행복 횟수 고정 표시
                val happyCount = reportList.find { it.name == "행복" }?.count ?: 0
                txtSummary.text = "행복 : ${if(happyCount > 0) "${happyCount}회" else "- 회"}"

                // 리사이클러뷰 연결
                rv.layoutManager = LinearLayoutManager(requireContext())
                // 어댑터 클릭 리스너 내부에 날짜 데이터 전달 로직 추가
                rv.adapter = EmotionCardAdapter(reportList) { clicked ->
                    val detailFragment = Archive_detail().apply {
                        arguments = Bundle().apply {
                            putString("emotionName", clicked.name)
                            // 핵심 수정: 현재 메인 화면의 연도와 월을 Bundle에 담아 전달합니다.
                            putInt("selectedYear", currentYear)
                            putInt("selectedMonth", currentMonth)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }
}

// 카드뷰 전용 어댑터
class EmotionCardAdapter(
    private val items: List<EmotionReport>,
    private val onClick: (EmotionReport) -> Unit
) : RecyclerView.Adapter<EmotionCardAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card = v.findViewById<CardView>(R.id.cardView)
        val title = v.findViewById<TextView>(R.id.txtEmotionTitle)
        val count = v.findViewById<TextView>(R.id.txtEmotionCount)
        val method = v.findViewById<TextView>(R.id.OvercomeMethod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_archive_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.title.text = data.name

        // 개수가 0보다 크면 'N회', 아니면 '-회'
        holder.count.text = if (data.count > 0) "이번 달 ${data.count}회" else "이번 달 -회"
        holder.method.text = data.bestWay

        // 카드 전체에 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onClick(data)
        }
    }

    override fun getItemCount() = items.size
}