package ru.networkignav.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.networkignav.auth.AppAuth
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Job
import ru.networkignav.entity.PostEntity
import ru.networkignav.model.FeedModelState
import ru.networkignav.model.PhotoModel
import ru.networkignav.repository.PostRepository
import ru.networkignav.util.SingleLiveEvent
import javax.inject.Inject

private val empty: Job =
    Job(
        id = 0,
        name = "",
        position = "",
        start = "",
        finish = "",
        link = ""
    )


@HiltViewModel
@ExperimentalCoroutinesApi
class JobViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState> get() = _state

    val data: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.data_profile }
        .flowOn(Dispatchers.Default)
    private var _profile = MutableLiveData<PostEntity.Users>()
    val profile: LiveData<PostEntity.Users>
        get() = _profile


    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated


    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo



    fun loadProfile() {
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                _profile.value = repository.getProfile()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun saveJob() {
        edited.value?.let { job ->
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    _state.postValue(FeedModelState(loading = true))
                    repository.saveJob(job)
                    _state.postValue(FeedModelState(loading = false))
                } catch (e: Exception) {
                    _state.postValue(FeedModelState(error = true))
                }
            }
        }
        clear()
    }
    fun changeContent(
        name: String,
        position: String,
        start: String,
        finish: String,
        link: String
    ) {
        if (edited.value?.name == name.trim()
            && edited.value?.position == position.trim()
            && edited.value?.start == start.trim()
            && edited.value?.finish == finish.trim()
            && edited.value?.link == link.trim()
        ) {
            return
        }
        if (link.isBlank()) {
            edited.value = edited.value?.copy(name = name, position = position,start= start, finish = finish, link = null)
        } else {
            edited.value = edited.value?.copy(name = name, position = position,start= start, finish = finish, link = link)
        }
    }

    fun clear() {
        edited.value?.let {
            edited.value = empty
        }
    }


}