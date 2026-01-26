package com.example.diaryofthattime.ui.archive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diaryofthattime.R
import com.example.diaryofthattime.data.Diary

class DiaryAdapter(
    private val onItemClick: (Diary) -> Unit
) : ListAdapter<Diary, DiaryAdapter.DiaryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiaryViewHolder(
        itemView: View,
        private val onItemClick: (Diary) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val emotionText: TextView = itemView.findViewById(R.id.tvEmotion)
        private val reasonText: TextView = itemView.findViewById(R.id.tvReason)

        fun bind(diary: Diary) {
            emotionText.text = diary.emotion
            reasonText.text = diary.reason

            itemView.setOnClickListener {
                onItemClick(diary)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Diary>() {
            override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
                return oldItem == newItem
            }
        }
    }
}
