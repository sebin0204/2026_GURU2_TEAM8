package com.example.diaryofthattime.ui.archive

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diaryofthattime.R
import com.example.diaryofthattime.data.AppDatabase
import com.example.diaryofthattime.review.ReviewActivity   // ⭐ 핵심
import com.example.diaryofthattime.ui.archive.adapter.DiaryAdapter
import kotlinx.coroutines.launch

class ArchiveFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiaryAdapter
    private lateinit var viewModel: ArchiveViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)

        adapter = DiaryAdapter { diary ->
            val intent = Intent(requireContext(), ReviewActivity::class.java)
            intent.putExtra("DIARY_ID", diary.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val dao = AppDatabase.getDatabase(requireContext()).diaryDao()

        viewModel = ViewModelProvider(
            this,
            ArchiveViewModelFactory(dao)
        )[ArchiveViewModel::class.java]

        lifecycleScope.launch {
            viewModel.diaries.collect { diaryList ->
                adapter.submitList(diaryList)
            }
        }
    }
}
