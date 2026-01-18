package com.example.diaryofthattime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

class Report_main : Fragment(R.layout.archive_emotion){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerEmotionCards)
        val txtMost = view.findViewById<TextView>(R.id.txtSummaryMost)
        val txtLeast = view.findViewById<TextView>(R.id.txtSummaryLeast)

        // 1. 5개 감정 더미 데이터 (기획 반영)
        val reportList = listOf(
            EmotionReport("행복", 12, "가족들과 맛있는 저녁을 먹으며 대화하기"),
            EmotionReport("슬픔", 5, "슬픈 영화를 보며 한바탕 울고 푹 자고 일어나기"),
            EmotionReport("분노", 3, "운동장에서 숨이 찰 때까지 달리기"),
            EmotionReport("불안", 8, "명상 앱을 켜고 5분간 호흡에 집중하기"),
            EmotionReport("무기력", 2, "작은 화분에 물 주기나 방 청소하기")
        )
        // 2. 정렬 로직: count(횟수)를 기준으로 내림차순(descending) 정렬
        val sortedList = reportList.sortedByDescending { it.count }

        // 3. 요약 텍스트 설정
        if (sortedList.isNotEmpty()) {
            val mostEmotion = sortedList.first() // 정렬 후 첫 번째가 가장 많음
            val leastEmotion = sortedList.last() // 정렬 후 마지막이 가장 적음

            txtMost.text = "Most: ${mostEmotion.name} ${mostEmotion.count}회"
            txtLeast.text = "Least: ${leastEmotion.name} ${leastEmotion.count}회"
        } else {
            txtMost.text = "Most: - 회"
            txtLeast.text = "Least: - 회"
        }

        // 4. 리사이클러뷰 설정 (정렬된 리스트 사용)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = EmotionCardAdapter(sortedList) { clickedEmotion ->

            // 1. 상세 화면 프래그먼트 생성
            val detailFragment = Archive_detail()

            // 2. 클릭한 감정 이름을 상세 화면으로 전달 (Bundle 사용)
            val bundle = Bundle().apply {
                putString("emotionName", clickedEmotion.name)
            }
            detailFragment.arguments = bundle

            // 3. 화면 전환 (MainActivity에서 썼던 replace 로직 활용)
            parentFragmentManager.beginTransaction()
                .replace(R.id.main, detailFragment)
                .addToBackStack(null) // 뒤로가기 버튼 누르면 다시 보고서로 돌아옴
                .commit()

        }
    }
}

// 카드뷰 전용 어댑터
class EmotionCardAdapter(
    private val items: List<EmotionReport>,
    private val onClick: (EmotionReport) -> Unit // 🔥 클릭 시 실행할 함수를 인자로 받음
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
        holder.count.text = "이번 달 ${data.count}회"
        holder.method.text = data.bestWay

        // 🔥 카드 전체에 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onClick(data) // 클릭된 데이터를 밖으로 던져줌
        }
    }

    override fun getItemCount() = items.size
}

