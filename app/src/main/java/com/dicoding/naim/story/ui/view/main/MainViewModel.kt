package com.dicoding.naim.story.ui.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dicoding.naim.story.database.StoryRepository

class MainViewModel(pref: StoryRepository) : ViewModel() {
    val stories = pref.getStoriesPaged().cachedIn(viewModelScope)
}