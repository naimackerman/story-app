package com.dicoding.naim.story.ui.view.register

import androidx.lifecycle.*
import com.dicoding.naim.story.database.AuthRepository
import com.dicoding.naim.story.helper.Event
import com.dicoding.naim.story.helper.getErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val pref: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Event<Boolean>>()
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _isSuccess.value = Event(pref.register(name, email, password))
            } catch (exception: HttpException) {
                exception.response()?.errorBody()?.let {
                    val errorResponse = getErrorResponse(it)

                    _error.value = Event(errorResponse.message)
                }
            } catch (exception: Exception) {
                _error.value = Event(exception.localizedMessage ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }
}