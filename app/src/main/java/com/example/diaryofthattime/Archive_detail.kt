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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Archive_detail : Fragment(R.layout.archive_emotion_detail) {

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerDetailList)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val selectedEmotion = arguments?.getString("emotionName") ?: "감정"
        val txtDetailTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        txtDetailTitle.text = selectedEmotion

        val txtDate = view.findViewById<TextView>(R.id.txtCurrentDate)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNextMonth)

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
    }

    private fun updateUI(txtDate: TextView) {
        txtDate.text = "${currentYear}년 ${currentMonth}월"
    }

    private fun loadDataFromDB(emotion: String) {
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
                            .replace(R.id.fragmentContainer, fragment) // ID 확인 필요
                            .addToBackStack(null).commit()
                    }
                }
            }
        }
    }

    class ArchiveDetailAdapter(
        private val items: List<Diary>,
        private val emotion: String,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ArchiveDetailAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val txtTitle = v.findViewById<TextView>(R.id.itemTitle)
            val txtDate = v.findViewById<TextView>(R.id.itemDate)
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