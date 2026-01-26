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

// 상세 화면용 데이터 모델
data class DetailRecord(
    val title: String, //emotion -> title 수정
    val date: String,
    val overcome: String
)
class Archive_detail : Fragment(R.layout.archive_emotion_detail) {
    // 캘린더 객체에서 현재 월을 가져옵니다.
    // Calendar.MONTH는 0부터 시작(1월이 0)하기 때문에 반드시 +1을 해줘야 합니다.

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    // 선언만 해두고 onViewCreated에서 초기화합니다.
    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰 찾기
        recyclerView = view.findViewById(R.id.recyclerDetailList)

        // 2. ⭐ 필수: 레이아웃 매니저 설정 (이게 없으면 안 뜹니다!)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // 1. 전달받은 감정 이름 꺼내기 (Key를 "emotionName"으로 일치시킴)
        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"

        // 2. 타이틀 텍스트뷰를 클릭한 감정 이름으로 바꾸기
        val txtDetailTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        txtDetailTitle.text = selectedEmotion

        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)

        // 초기 화면 설정
        updateUI(txtDate)

        // 이번 달 데이터를 처음에 바로 불러옵니다.
        loadDataForMonth(currentYear, currentMonth)


        // 초기 화면 설정 (2026년 1월 형식으로 표시)
        updateUI(txtDate)

        // 이전달 클릭 로직 (연도 변경 포함)
        btnPrev.setOnClickListener {
            if (currentMonth > 1) {
                currentMonth--
            } else {
                currentMonth = 12
                currentYear-- // 1월에서 이전으로 가면 작년 12월로 이동
            }
            updateUI(txtDate)
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
            updateUI(txtDate)
            loadDataForMonth(currentYear, currentMonth)
        }
    }

    // UI를 < 2026년 1월 > 형식으로 업데이트하는 함수
    private fun updateUI(txtDate: TextView) {
        txtDate.text = "${currentYear}년 ${currentMonth}월"
    }

    // 데이터 로딩 시 연도 정보도 함께 넘겨주도록 수정
    private fun loadDataForMonth(year: Int, month: Int) {
        // TODO: DB에서 해당 연도와 월의 데이터를 가져오는 로직

        // 현재 어떤 감정 페이지인지 가져오기 (Bundle에서 꺼낸 값 활용)
        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"

        // 1. 실제로는 여기서 DB 쿼리를 날려야 하지만, 지금은 가짜 리스트를 만듭니다.
        val dummyRecords = listOf(
            // 감정, 날짜, 이겨낸 방법
            DetailRecord("오늘의 일기", "${year}년 ${month}월 05일", "좋은 음악을 들으며 생각을 정리함"),
            DetailRecord("어려웠던 하루", "${year}년 ${month}월 12일", "친구와 통화하며 속마음을 털어놓음"),
            DetailRecord("마음 정리", "${year}년 ${month}월 15일", "따뜻한 차를 마시며 일찍 잠자리에 듬")
        )

        // 2. 이 데이터를 상세 화면 리사이클러뷰 어댑터에 넘겨줍니다.
        val adapter = ArchiveDetailAdapter(dummyRecords, selectedEmotion)
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
class ArchiveDetailAdapter(private val items: List<DetailRecord>,
                           private val emotion: String // 어떤 감정인지 받음
) : RecyclerView.Adapter<ArchiveDetailAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val txtTitle = v.findViewById<TextView>(R.id.itemTitle)
        val txtDate = v.findViewById<TextView>(R.id.itemDate)
        // 행복 레이아웃에는 overcome(극복방법)이 없을 수 있으므로 ?를 붙여 안전하게 찾습니다.
        val txtOvercome : TextView? = v.findViewById(R.id.itemOvercome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ⭐ 감정이 "행복"이면 전용 XML을, 아니면 일반 XML을 인플레이트합니다.
        val layoutId = if (emotion == "행복") {
            R.layout.item_archive_detail_happy
        } else {
            R.layout.item_archive_detail
        }

        val v = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtTitle.text = item.title
        holder.txtDate.text = item.date

        // 행복 페이지(overcome이 없는 UI)일 때는 에러 방지를 위해 체크 후 데이터 삽입
        holder.txtOvercome?.text = item.overcome
    }

    override fun getItemCount() = items.size
}