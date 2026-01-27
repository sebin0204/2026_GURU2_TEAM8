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
import java.util.Calendar
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.databinding.ActivityDiaryDetailBinding
import com.example.diaryofthattime.review.ReviewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class Archive_detail : Fragment(R.layout.archive_emotion_detail) {

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var recyclerView: RecyclerView? = null
    private var onlyOvercome: Boolean = false
    private var dataJob: Job? = null

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

        // 타이틀 설정
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

    private fun loadDataFromDB(emotion: String) {
        val yearStr = currentYear.toString()
        val monthStr = String.format("%02d", currentMonth)
        val txtNoData = view?.findViewById<TextView>(R.id.txtNoData)

        dataJob?.cancel()
        dataJob = lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.diaryDao().getDiariesByMonthAndEmotion(yearStr, monthStr, emotion).collect { list ->

                // 필터링 로직
                val finalList = if (onlyOvercome) {
                    list.filter { it.isReviewed && !it.overcomeWay.isNullOrBlank() }
                } else {
                    list
                }

                withContext(Dispatchers.Main) {
                    if (finalList.isEmpty()) {
                        recyclerView?.visibility = View.GONE
                        txtNoData?.visibility = View.VISIBLE
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
                                    val intent = Intent(requireContext(), DiaryDetailActivity::class.java).apply {
                                        putExtra("DIARY_ID", diaryId) // DiaryDetailActivity가 사용하는 Key값
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
}

// --- 어댑터 클래스 ---
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
        holder.txtOvercome?.text = item.overcomeWay ?: "아직 기록된 극복 방법이 없어요."
        holder.itemView.setOnClickListener { onItemClick(item.id) }
    }

    override fun getItemCount() = items.size
}


// --- 일기 확인(상세) 프래그먼트 (클래스 분리) ---
class DiaryViewFragment : Fragment(R.layout.diary_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val diaryId = arguments?.getInt("diaryId") ?: return
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack?.setOnClickListener { parentFragmentManager.popBackStack() }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val diary = db.diaryDao().getDiaryById(diaryId) ?: return@launch

            btnEdit.setOnClickListener {
                if (diary.isReviewed) {
                    val intent = Intent(requireContext(), ReviewActivity::class.java).apply {
                        putExtra("diaryId", diaryId)
                        putExtra("isEditMode", true)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(requireContext(), WriteActivity::class.java).apply {
                        putExtra("diaryId", diaryId)
                        putExtra("isEditMode", true)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}