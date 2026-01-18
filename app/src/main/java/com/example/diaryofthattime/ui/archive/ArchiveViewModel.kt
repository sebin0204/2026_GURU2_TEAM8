package com.example.diaryofthattime.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryofthattime.data.Diary
import com.example.diaryofthattime.data.DiaryDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ArchiveViewModel(
    diaryDao: DiaryDao
) : ViewModel() {

    val diaries: StateFlow<List<Diary>> =
        diaryDao.getAllDiaries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
