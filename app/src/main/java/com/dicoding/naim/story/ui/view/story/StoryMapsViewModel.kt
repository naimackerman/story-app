package com.dicoding.naim.story.ui.view.story

import androidx.lifecycle.*
import com.dicoding.naim.story.database.StoryRepository
import com.dicoding.naim.story.helper.Event
import com.dicoding.naim.story.helper.getErrorResponse
import com.dicoding.naim.story.network.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StoryMapsViewModel(private val pref: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        getAllStories()
    }

    private fun getAllStories() {
        viewModelScope.launch {
            try {
                _stories.value = pref.getStoriesWithLocation()
            } catch (exception: HttpException) {
                exception.response()?.errorBody()?.let {
                    val errorResponse = getErrorResponse(it)

                    _error.value = Event(errorResponse.message)
                }
            } catch (exception: Exception) {
                _error.value = Event(exception.localizedMessage ?: "")
            } finally {

            }
        }
    }
}