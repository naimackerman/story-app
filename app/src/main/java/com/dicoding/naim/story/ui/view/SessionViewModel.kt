package com.dicoding.naim.story.ui.view

import androidx.lifecycle.*
import com.dicoding.naim.story.database.SessionPreference
import kotlinx.coroutines.launch

class SessionViewModel(private val pref: SessionPreference) : ViewModel() {
    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }
}