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
import ru.networkignav.dto.Event
import ru.networkignav.dto.EventType
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Post
import ru.networkignav.model.FeedModelState
import ru.networkignav.model.PhotoModel
import ru.networkignav.repository.EventRepository
import ru.networkignav.util.DataType
import ru.networkignav.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    authorJob = "",
    content = "",
    datetime = "",
    published = "",
    type = EventType.OFFLINE,
    likedByMe = false,
    participatedByMe = false,
    attachment = null,
    link = null,
    ownedByMe = false,
    users = null,
    hidden = false
)

@ExperimentalCoroutinesApi
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
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

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated



    fun changeContent(
        content: String,
        date: String
    ) {
        if (edited.value?.content == content.trim()
            && edited.value?.datetime == date.trim()
        ) {
            return
        }
        edited.value = edited.value?.copy(content = content, datetime = date)
    }

    fun clear() {
        edited.value?.let {
            edited.value = empty
        }
    }


    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                _eventCreated.value = Unit
                viewModelScope.launch {
                    try {
                        _state.postValue(FeedModelState(loading = true))
                        repository.save(it)
                        _state.postValue(FeedModelState(loading = false))
                    } catch (e: Exception) {
                        _state.postValue(FeedModelState(error = true))
                    }
                }

            }
            edited.value = empty
        }

    }

    fun edit(event: Event) {
        edited.value = event
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