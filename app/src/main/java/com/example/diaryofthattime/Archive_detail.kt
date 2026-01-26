package com.example.diaryofthattime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diaryofthattime.data.AppDatabase // 팀의 DB 설정에 맞춰 수정 필요
import com.example.diaryofthattime.data.Diary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar //날짜 정보를 가져오는 클래스
import java.util.Date
import java.util.Locale

// 상세 화면용 데이터 모델
data class DetailRecord(
    val id: Int,       // DB의 고유 ID를 저장하기 위해 추가
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

        // 리사이클러뷰 찾기
        recyclerView = view.findViewById(R.id.recyclerDetailList)

        // 필수: 레이아웃 매니저 설정 (이게 없으면 안 뜹니다!)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // 전달받은 감정 이름 꺼내기 (Key를 "emotionName"으로 일치시킴)
        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"

        // 타이틀 텍스트뷰를 클릭한 감정 이름으로 바꾸기
        val txtDetailTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        txtDetailTitle.text = selectedEmotion

        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)

        // 초기 화면 설정
        updateUI(txtDate)

        // 이번 달 데이터를 처음에 바로 불러옵니다.
        loadDataFromDB(selectedEmotion) // DB 연결 함수 호출

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
            loadDataFromDB(selectedEmotion)
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
            loadDataFromDB(selectedEmotion)
        }
    }

    // UI를 < 2026년 1월 > 형식으로 업데이트하는 함수
    private fun updateUI(txtDate: TextView) {
        txtDate.text = "${currentYear}년 ${currentMonth}월"
    }

    // 데이터 로딩 시 연도 정보도 함께 넘겨주도록 수정
    private fun loadDataFromDB(emotion: String) {
        // TODO: DB에서 해당 연도와 월의 데이터를 가져오는 로직

        // 현재 어떤 감정 페이지인지 가져오기 (Bundle에서 꺼낸 값 활용)
        val yearStr = currentYear.toString()
        val monthStr = String.format("%02d", currentMonth)
        val txtNoData = view?.findViewById<TextView>(R.id.txtNoData)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.diaryDao().getDiariesByMonthAndEmotion(yearStr, monthStr, emotion).collect { list ->
                if (list.isEmpty()) {
                    recyclerView?.visibility = View.GONE
                    txtNoData?.visibility = View.VISIBLE
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    txtNoData?.visibility = View.GONE
                    recyclerView?.adapter = ArchiveDetailAdapter(list, emotion) { diaryId ->
                        // 상세 화면 이동
                        val fragment = DiaryViewFragment().apply {
                            arguments = Bundle().apply { putInt("diaryId", diaryId) }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.main, fragment)
                            .addToBackStack(null).commit()
                    }
                }
            }
        }
    }

    // 상세 화면용 리사이클러뷰 어댑터
    class ArchiveDetailAdapter(
        private val items: List<Diary>,
        private val emotion: String, // 어떤 감정인지 받음
        private val onItemClick: (Int) -> Unit // 클릭 시 ID를 전달할 함수 추가
    ) : RecyclerView.Adapter<ArchiveDetailAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val txtTitle = v.findViewById<TextView>(R.id.itemTitle)
            val txtDate = v.findViewById<TextView>(R.id.itemDate)

            // 행복 레이아웃에는 overcome(극복방법)이 없을 수 있으므로 ?를 붙여 안전하게 찾습니다.
            val txtOvercome: TextView? = v.findViewById(R.id.itemOvercome)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // 감정이 "행복"이면 전용 XML을, 아니면 일반 XML을 인플레이트합니다.
            val layout = if (emotion == "행복") {
                R.layout.item_archive_detail_happy
            } else {
                R.layout.item_archive_detail
            }

            val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.txtTitle.text = item.title
            holder.txtDate.text = SimpleDateFormat("MM월 dd일", Locale.KOREA).format(Date(item.date))

            if (emotion != "행복") {
                // 행복이 아닌 감정(슬픔, 불안 등)의 레이아웃에서만 실행
                holder.txtOvercome?.let { textView ->
                    if (item.overcomeWay.isNullOrBlank()) {
                        textView.text = "아직 기록된 극복 방법이 없어요."
                        textView.alpha = 0.5f // 안내 문구는 흐리게
                    } else {
                        textView.text = item.overcomeWay
                        textView.alpha = 1.0f // 실제 기록은 진하게
                    }
                }
            } else {
                // 행복 페이지일 때는 아무것도 하지 않음
            }

            // 아이템 전체 클릭 시 ID 전달
            holder.itemView.setOnClickListener { onItemClick(item.id) }
        }

        override fun getItemCount() = items.size
    }

}