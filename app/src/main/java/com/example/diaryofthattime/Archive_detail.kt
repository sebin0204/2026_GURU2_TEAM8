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
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Archive_detail : Fragment(R.layout.archive_emotion_detail) {

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var recyclerView: RecyclerView? = null
    private var onlyOvercome: Boolean = false 

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.let {
            currentYear = it.getInt("selectedYear", currentYear)
            currentMonth = it.getInt("selectedMonth", currentMonth)
            onlyOvercome = it.getBoolean("onlyOvercome", false) 
        }

        recyclerView = view.findViewById(R.id.recyclerDetailList)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"
        val txtDetailTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        
        // 💡 타이틀을 통해 현재 "전체"인지 "회고 완료"인지 구분해줍니다.
        txtDetailTitle.text = if (onlyOvercome) "$selectedEmotion (회고 완료)" else selectedEmotion

        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        updateUI(txtDate)
        loadDataFromDB(selectedEmotion)

        btnPrev.setOnClickListener {
            if (currentMonth > 1) currentMonth-- else { currentMonth = 12; currentYear-- }
            updateUI(txtDate)
            loadDataFromDB(selectedEmotion)
        }

        btnNext.setOnClickListener {
            if (currentMonth < 12) currentMonth++ else { currentMonth = 1; currentYear++ }
            updateUI(txtDate)
            loadDataFromDB(selectedEmotion)
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun updateUI(txtDate: TextView) {
        txtDate.text = "${currentYear}년 ${currentMonth}월"
    }

    private var dataJob: kotlinx.coroutines.Job? = null

    private fun loadDataFromDB(emotion: String) {
        val yearStr = currentYear.toString()
        val monthStr = String.format("%02d", currentMonth)
        val txtNoData = view?.findViewById<TextView>(R.id.txtNoData)

        dataJob?.cancel()
        dataJob = lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            db.diaryDao().getDiariesByMonthAndEmotion(yearStr, monthStr, emotion).collect { list ->

                // 💡 [핵심 필터링 로직 수정]
                val finalList = if (onlyOvercome) {
                    // 버튼 클릭 시: 회고가 완료(isReviewed)되고 극복 방법이 있는 것만 필터링
                    list.filter { it.isReviewed == true && !it.overcomeWay.isNullOrBlank() }
                } else {
                    // 카드 클릭 시: 해당 월/감정의 모든 일기 노출
                    list
                }

                withContext(Dispatchers.Main) {
                    if (finalList.isEmpty()) {
                        recyclerView?.visibility = View.GONE
                        txtNoData?.visibility = View.VISIBLE
                        // 안내 문구 세분화
                        txtNoData?.text = if (onlyOvercome) {
                            "${currentMonth}월에 회고 완료된 '${emotion}' 기록이 없습니다."
                        } else {
                            "${currentMonth}월에 작성된 '${emotion}' 일기가 없습니다."
                        }
                    } else {
                        recyclerView?.visibility = View.VISIBLE
                        txtNoData?.visibility = View.GONE

                        recyclerView?.adapter =
                            ArchiveDetailAdapter(finalList, emotion) { diaryId ->
                                if (onlyOvercome) {
                                    // 1️⃣ 극복 방법 리스트(Success Case)에서 클릭 시 -> 회고록(ReviewActivity)으로 이동
                                    val intent =
                                        Intent(requireContext(), ReviewActivity::class.java).apply {
                                            putExtra("diaryId", diaryId.toLong())
                                            putExtra("isEditMode", false) // 조회 모드로 진입
                                        }
                                    startActivity(intent)
                                } else {
                                    // 2️⃣ 일반 감정 리스트에서 클릭 시 -> 일기 상세 프래그먼트(DiaryViewFragment)로 이동
                                    val fragment = DiaryViewFragment().apply {
                                        arguments = Bundle().apply { putInt("diaryId", diaryId) }
                                    }
                                    parentFragmentManager.beginTransaction()
                                        .replace(
                                            R.id.main,
                                            fragment
                                        ) // ID 확인: fragmentContainer
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                    }
                }
            }
        }
    }
    // --- 어댑터 클래스 (동일) ---
    class ArchiveDetailAdapter(
        private val items: List<Diary>,
        private val emotion: String,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ArchiveDetailAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val txtTitle: TextView = v.findViewById(R.id.itemTitle)
            val txtDate: TextView = v.findViewById(R.id.itemDate)
            val txtOvercome: TextView? = v.findViewById(R.id.itemOvercome)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layout = if (emotion == "행복") R.layout.item_archive_detail_happy else R.layout.item_archive_detail
            val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.txtTitle.text = item.title
            holder.txtDate.text = SimpleDateFormat("MM월 dd일", Locale.KOREA).format(Date(item.date))

            if (emotion != "행복") {
                holder.txtOvercome?.let { textView ->
                    if (item.overcomeWay.isNullOrBlank()) {
                        textView.text = "아직 기록된 극복 방법이 없어요."
                        textView.alpha = 0.5f
                    } else {
                        textView.text = item.overcomeWay
                        textView.alpha = 1.0f
                    }
                }
            }
            holder.itemView.setOnClickListener { onItemClick(item.id) }
        }

        override fun getItemCount() = items.size
    }
}

// --- 일기 확인(상세) 프래그먼트 ---
class DiaryViewFragment : Fragment(R.layout.diary_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val diaryId = arguments?.getInt("diaryId") ?: -1

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
        val txtTitle = view.findViewById<TextView>(R.id.viewTitle)
        val txtDate = view.findViewById<TextView>(R.id.viewDate)
        val imgEmotion = view.findViewById<ImageView>(R.id.imgEmotion)
        val txtContent = view.findViewById<TextView>(R.id.viewContent)
        val txtMessage = view.findViewById<TextView>(R.id.viewMessage)
        val overcomeLayout = view.findViewById<View>(R.id.overcomeLayout)
        val labelOvercome = view.findViewById<TextView>(R.id.labelOvercome)
        val txtOvercome = view.findViewById<TextView>(R.id.viewOvercome)
        val txtType = view.findViewById<TextView>(R.id.viewType)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("일기 삭제")
                .setMessage("이 일기를 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(requireContext())
                            val diary = db.diaryDao().getDiaryById(diaryId)

                            if (diary != null) {
                                db.diaryDao().delete(diary)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val diary = db.diaryDao().getDiaryById(diaryId)

                if (diary != null) {
                    // [핵심 1] 제목(txtTitle) 데이터 세팅 추가됨!
                    txtTitle.text = diary.title
                    // [핵심 2] 회고 여부에 따른 수정 버튼 이동 경로 분기
                    btnEdit.setOnClickListener {
                        if (diary.isReviewed) {
                            // 회고 완료 후 -> 회고 수정 페이지(ReviewActivity)
                            val intent = Intent(requireContext(), ReviewActivity::class.java).apply {
                                putExtra("diaryId", diaryId.toLong())
                                putExtra("isEditMode", true)
                            }
                            startActivity(intent)
                        } else {
                            // 회고 전 -> 일기 작성 수정 페이지(WriteActivity)
                            val intent = Intent(requireContext(), WriteActivity::class.java).apply {
                                putExtra("diaryId", diaryId)
                                putExtra("isEditMode", true)
                            }
                            startActivity(intent)
                        }
                    }

                    // [2] 태그 표시 로직 변경 (회고 전/후 구분)
                    // 회고가 완료되었다면 stillFact를 사용하고, 아니면 isFact를 사용합니다.
                    val displayFact = if (diary.isReviewed) {
                        diary.stillFact ?: diary.isFact // 회고 완료 시 stillFact 우선, 없으면 isFact
                    } else {
                        diary.isFact // 회고 전이면 무조건 isFact
                    }

                    if (displayFact) {
                        txtType.text = "사실"
                        txtType.setBackgroundColor(android.graphics.Color.parseColor("#8E9775")) // 올리브 민트
                    } else {
                        txtType.text = "추측"
                        txtType.setBackgroundColor(android.graphics.Color.parseColor("#D4A373")) // 브릭 베이지
                    }
                    txtType.setTextColor(android.graphics.Color.WHITE)

                    txtDate.text = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date(diary.date))
                    txtContent.text = diary.reason
                    txtMessage.text = diary.messageToMe ?: "나에게 남긴 메시지가 없습니다."
                    imgEmotion.setImageResource(getEmotionImage(diary.emotion))

                    // 극복 방법 표시 로직
                    if (!diary.overcomeWay.isNullOrBlank()) {
                        overcomeLayout.visibility = View.VISIBLE
                        txtOvercome.text = diary.overcomeWay
                        labelOvercome.text = if (diary.emotion == "행복") "✨ 행복의 순간" else "💡 이렇게 이겨냈어요"
                    } else {
                        overcomeLayout.visibility = View.VISIBLE
                        txtOvercome.text = "아직 기록된 극복 방법이 없어요."
                    }
                } else {
                    Toast.makeText(requireContext(), "일기 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Log.e("DiaryView", "Error loading data", e)
                Toast.makeText(requireContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun getEmotionImage(emotion: String?): Int {
        return when (emotion) {
            "행복" -> R.drawable.ic_launcher_foreground // 실제 이미지로 변경하세요
            "무기력" -> R.drawable.ic_launcher_foreground
            "슬픔" -> R.drawable.ic_launcher_foreground
            "분노" -> R.drawable.ic_launcher_foreground
            "불안" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }
    }
}
