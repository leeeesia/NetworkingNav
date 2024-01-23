package ru.networkignav.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.networkignav.auth.AppAuth
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Post
import ru.networkignav.model.FeedModelState
import ru.networkignav.model.PhotoModel
import ru.networkignav.repository.PostRepository

import ru.networkignav.util.SingleLiveEvent

import javax.inject.Inject

private val empty: Post =
    Post(
        id = 0,
        authorId = 0,
        author = "",
        authorAvatar = "",
        content = "",
        published = "",
        mentionedMe = false,
        likedByMe = false,
        attachment = null,
        ownedByMe = false,
        users = null,
        hidden = false
    )


@HiltViewModel
@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached: Flow<PagingData<FeedItem>> = repository.data_profile.cachedIn(viewModelScope)
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState> get() = _state

    val data: Flow<PagingData<FeedItem>> = appAuth.state
        .flatMapLatest { cached }
        .flowOn(Dispatchers.Default)

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val newerCount: Flow<Int> = data.flatMapLatest {
        repository.getProfileNewerCount().flowOn(Dispatchers.Default)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadWall()
    }

    fun loadWall() {
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getMyWall()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

}
