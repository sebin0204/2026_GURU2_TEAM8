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

// 통계 데이터를 담기 위한 클래스들
data class EmotionCount(val emotion: String, val count: Int)
data class EmotionReport(val name: String, val count: Int, val bestWay: String)

class Archive_main : Fragment(R.layout.archive_emotion) {

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerEmotionCards)
        val txtSummary = view.findViewById<TextView>(R.id.txtSummaryMost)
        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)

        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        updateUI(txtDate, txtSummary, recyclerView)

        btnPrev.setOnClickListener {
            if (currentMonth > 1) currentMonth-- else {
                currentMonth = 12; currentYear--
            }
            updateUI(txtDate, txtSummary, recyclerView)
        }

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
            db.diaryDao().getDiariesByMonth(yearStr, monthStr).collect { diaries ->

                val fixedEmotions = listOf("행복", "무기력", "슬픔", "불안", "화남")

                val reportList = fixedEmotions.map { emotionName ->
                    val filteredDiaries = diaries.filter { it.emotion == emotionName }
                    val count = filteredDiaries.size

                    val lastOvercome = filteredDiaries.firstOrNull { !it.overcomeWay.isNullOrBlank() }?.overcomeWay
                        ?: "아직 기록된 극복 방법이 없어요."

                    EmotionReport(
                        name = emotionName,
                        count = count,
                        bestWay = lastOvercome
                    )
                }

                val happyCount = reportList.find { it.name == "행복" }?.count ?: 0
                txtSummary.text = "행복 : ${if (happyCount > 0) "${happyCount}회" else "- 회"}"

                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = EmotionCardAdapter(
                    reportList,
                    onCardClick = { clicked -> moveToDetail(clicked.name, false) },
                    onMethodClick = { clicked -> moveToDetail(clicked.name, true) }
                )
            }
        }
    }

    // 💡 [수정] 이 함수가 updateUI 밖에, Archive_main 클래스 안에 정확히 위치해야 합니다.
    private fun moveToDetail(emotionName: String, onlyOvercome: Boolean) {
        val detailFragment = Archive_detail().apply {
            arguments = Bundle().apply {
                putString("emotionName", emotionName)
                putInt("selectedYear", currentYear)
                putInt("selectedMonth", currentMonth)
                putBoolean("onlyOvercome", onlyOvercome)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main, detailFragment)
            .addToBackStack(null)
            .commit()
    }
}

// 💡 [수정] 어댑터 클래스는 Archive_main 클래스 외부(파일 하단)에 있는 것이 깔끔합니다.
class EmotionCardAdapter(
    private val items: List<EmotionReport>,
    private val onCardClick: (EmotionReport) -> Unit,
    private val onMethodClick: (EmotionReport) -> Unit
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
        holder.count.text = if (data.count > 0) "이번 달 ${data.count}회" else "이번 달 -회"
        holder.method.text = data.bestWay

        // 카드 전체 클릭
        holder.itemView.setOnClickListener { onCardClick(data) }

        // 극복 방법 텍스트 클릭
        holder.method.setOnClickListener { onMethodClick(data) }
    }

    override fun getItemCount() = items.size
}
