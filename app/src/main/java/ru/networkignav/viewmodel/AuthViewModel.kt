package ru.networkignav.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.networkignav.auth.AppAuth
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {
    init {
        Log.d("MyLog", "00000000000")
    }

    val data = appAuth.state
        .asLiveData(Dispatchers.Default)

    val isAutificated: Boolean
        get() = data.value?.token != null

}