package ru.networkignav.viewmodel

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
import ru.networkignav.model.FeedModelState
import ru.networkignav.model.PhotoModel
import ru.networkignav.repository.PostRepository
import ru.networkignav.util.SingleLiveEvent
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Post
import javax.inject.Inject


private val empty: Post =
    Post(
        id = 0,
        authorId = 0,
        author = "",
        content = "",
        published = 0L,
        mentionedMe = false,
        likedByMe = false,
        attachment =null,
        ownedByMe = false,
        users = null,
        hidden = false
    )

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached: Flow<PagingData<FeedItem>> = repository
        .data
        .cachedIn(viewModelScope)
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    val data: Flow<PagingData<FeedItem>> = appAuth.state
        .flatMapLatest { token ->
            cached.map { posts ->
                posts.map {
                    it
                }
            }
        }.flowOn(Dispatchers.Default)

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val newerCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerCount()
            .flowOn(Dispatchers.Default)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }

    fun setPhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun loadPosts() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getAll()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun loadNewPosts() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getNewPost()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun refresh() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(refreshing = true))
            try {
                repository.getAll()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }


}
