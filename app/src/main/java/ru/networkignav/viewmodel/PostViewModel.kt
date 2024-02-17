package ru.networkignav.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.flow
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
import ru.networkignav.util.DataType
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
    private val _dataType = MutableLiveData<DataType>()
    val dataType: LiveData<DataType> = _dataType


    fun setDataType(dataType: DataType) {
        _dataType.value = dataType
    }
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated



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
    fun loadWallByUserId(userId: String): Flow<List<Post>> = flow{
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
    fun updateUserId(userId: String){
        repository.updateUserId(userId)
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
    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                try {
                    _photo.value?.let { photoModel ->
                        repository.saveWithAttachment(it, photoModel.file)
                    } ?: run {
                        repository.save(it)
                    }
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
            edited.value = empty
        }

    }
    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }
    fun edit(post: Post) {
        edited.value = post
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeById(id)

            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }


}
