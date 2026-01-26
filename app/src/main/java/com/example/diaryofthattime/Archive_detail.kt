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
            holder.txtOvercome?.text = item.overcomeWay
            holder.itemView.setOnClickListener { onItemClick(item.id) }
        }

        override fun getItemCount() = items.size
    }
}

// 일기 확인 로직
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
        val overcomeLayout = view.findViewById<View>(R.id.layoutOvercomeResult)
        val imgStamp = view.findViewById<ImageView>(R.id.imgOvercomeStamp)
        val txtOvercome = view.findViewById<TextView>(R.id.textOvercomeWay)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra("diaryId", diaryId)
                putExtra("isEditMode", true)
            }
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("일기 삭제")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    lifecycleScope.launch {
                        val db = AppDatabase.getDatabase(requireContext())
                        val diary = db.diaryDao().getDiaryById(diaryId)
                        if (diary != null) {
                            db.diaryDao().delete(diary)
                            Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                    }
                }
                .setNegativeButton("취소", null).show()
        }

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val diary = db.diaryDao().getDiaryById(diaryId)

                // [수정] 널 체크를 위한 ?. 사용
                txtTitle.text = diary?.title
                txtDate.text = diary?.date?.let { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date(it)) } ?: ""
                txtContent.text = diary?.reason
                txtMessage.text = diary?.messageToMe ?: "나에게 남긴 메시지가 없습니다."

                imgEmotion.setImageResource(getEmotionImage(diary?.emotion))

                // [통합] 세빈 팀장님의 도장 로직 적용
                if (diary?.isReviewed == true) {
                    overcomeLayout.visibility = View.VISIBLE
                    imgStamp.visibility = View.VISIBLE
                    imgStamp.setImageResource(R.drawable.stemp) // stemp.png
                    txtOvercome.text = diary.overcomeWay
                } else {
                    overcomeLayout.visibility = View.GONE
                    imgStamp.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("DiaryView", "Error", e)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun getEmotionImage(emotion: String?): Int {
        return when (emotion) {
            "행복" -> R.drawable.ic_launcher_foreground // 실제 파일명으로 교체
            "무기력" -> R.drawable.ic_launcher_foreground
            "슬픔" -> R.drawable.ic_launcher_foreground
            "화남" -> R.drawable.ic_launcher_foreground
            "불안" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }
    }
}