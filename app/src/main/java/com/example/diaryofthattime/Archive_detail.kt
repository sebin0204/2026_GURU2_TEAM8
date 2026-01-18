package com.example.diaryofthattime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar //날짜 정보를 가져오는 클래스

// 1. 상세 화면용 데이터 모델
data class DetailRecord(
    val emotion: String,
    val date: String,
    val overcome: String
)
class Archive_detail : Fragment(R.layout.archive_emotion_detail) {
    // 캘린더 객체에서 현재 월을 가져옵니다.
    // Calendar.MONTH는 0부터 시작(1월이 0)하기 때문에 반드시 +1을 해줘야 합니다.

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerDetailList)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰 찾기
        recyclerView = view.findViewById(R.id.recyclerDetailList)

        // 2. ⭐ 필수: 레이아웃 매니저 설정 (이게 없으면 안 뜹니다!)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // 1. 전달받은 감정 이름 꺼내기 (Key를 "emotionName"으로 일치시킴)
        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"

        // 2. 타이틀 텍스트뷰를 클릭한 감정 이름으로 바꾸기
        // (XML의 상세화면 타이틀 ID가 txtDetailTitle이라고 가정합니다)
        val txtDetailTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        txtDetailTitle.text = selectedEmotion

        val txtMonth = view.findViewById<TextView>(R.id.txtCurrentDate)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)

        // 초기 화면 설정
        updateUI(txtMonth)

        // 이번 달 데이터를 처음에 바로 불러옵니다.
        loadDataForMonth(currentYear, currentMonth)


        // 초기 화면 설정 (2026년 1월 형식으로 표시)
        updateUI(txtMonth)

        // 이전달 클릭 로직 (연도 변경 포함)
        btnPrev.setOnClickListener {
            if (currentMonth > 1) {
                currentMonth--
            } else {
                currentMonth = 12
                currentYear-- // 1월에서 이전으로 가면 작년 12월로 이동
            }
            updateUI(txtMonth)
            loadDataForMonth(currentYear, currentMonth)
        }

        // 다음달 클릭 로직 (연도 변경 포함)
        btnNext.setOnClickListener {
            if (currentMonth < 12) {
                currentMonth++
            } else {
                currentMonth = 1
                currentYear++ // 12월에서 다음으로 가면 내년 1월로 이동
            }
            updateUI(txtMonth)
            loadDataForMonth(currentYear, currentMonth)
        }
    }

    // UI를 < 2026년 1월 > 형식으로 업데이트하는 함수
    private fun updateUI(txtMonth: TextView) {
        txtMonth.text = "${currentYear}년 ${currentMonth}월"
    }

    // 데이터 로딩 시 연도 정보도 함께 넘겨주도록 수정
    private fun loadDataForMonth(year: Int, month: Int) {
        // TODO: DB에서 해당 연도와 월의 데이터를 가져오는 로직

        // 1. 실제로는 여기서 DB 쿼리를 날려야 하지만, 지금은 가짜 리스트를 만듭니다.
        val dummyRecords = listOf(
            // 감정, 날짜, 이겨낸 방법
            DetailRecord("슬픔", "${year}년 ${month}월 05일", "좋은 음악을 들으며 생각을 정리함"),
            DetailRecord("슬픔", "${year}년 ${month}월 12일", "친구와 통화하며 속마음을 털어놓음"),
            DetailRecord("슬픔", "${year}년 ${month}월 15일", "따뜻한 차를 마시며 일찍 잠자리에 듬")
        )

        // 2. 이 데이터를 상세 화면 리사이클러뷰 어댑터에 넘겨줍니다.
        val adapter = ArchiveDetailAdapter(dummyRecords)

        recyclerView?.adapter = adapter
        /**
        // Archive_detail.kt에서 loadData 함수 호출할 때
        val yearStr = currentYear.toString() // "2026"
        val monthStr = String.format("%02d", currentMonth) // 1 -> "01", 10 -> "10"

        //쿼리 실행
        diaryDao.getEntriesByMonthAndEmotion(month, selectedEmotion)
        dao.getDiariesByMonthAndEmotion(yearStr, monthStr, selectedEmotion)
        **/
    }
}

// 2. 상세 화면용 리사이클러뷰 어댑터
class ArchiveDetailAdapter(private val items: List<DetailRecord>) :
    RecyclerView.Adapter<ArchiveDetailAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val txtEmotion = v.findViewById<TextView>(R.id.itemEmotionName)
        val txtDate = v.findViewById<TextView>(R.id.itemDate)
        val txtOvercome = v.findViewById<TextView>(R.id.itemOvercome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 이전에 만든 item_detail_record.xml을 인플레이트합니다.
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_archive_detail, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtEmotion.text = item.emotion
        holder.txtDate.text = item.date
        holder.txtOvercome.text = item.overcome
    }

    override fun getItemCount() = items.size
}