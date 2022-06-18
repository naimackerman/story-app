package com.dicoding.naim.story.ui.view.login

import androidx.lifecycle.*
import com.dicoding.naim.story.database.AuthRepository
import com.dicoding.naim.story.helper.Event
import com.dicoding.naim.story.helper.getErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val pref: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _token = MutableLiveData<Event<String>>()
    val token: LiveData<Event<String>> = _token

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _token.value = Event(pref.login(email, password))
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