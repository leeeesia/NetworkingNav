package ru.networkignav.ui.profile

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
import ru.networkignav.dto.Post
import ru.networkignav.entity.PostEntity
import ru.networkignav.model.FeedModelState
import ru.networkignav.model.PhotoModel
import ru.networkignav.repository.PostRepository
import ru.networkignav.util.DataType
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

private val emptyJob: Job =
    Job(
        id = 0,
        name = "",
        position = "",
        start = "",
        finish = "",
        link = null
    )
@HiltViewModel
@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState> get() = _state

    val data: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.data_profile }
        .flowOn(Dispatchers.Default)

    val data_user: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.data_user }
        .flowOn(Dispatchers.Default)

    val data_job: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.data_job }
        .flowOn(Dispatchers.Default)

    val job: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.job }
        .flowOn(Dispatchers.Default)

    private var _profile = MutableLiveData<PostEntity.Users>()
    val profile: LiveData<PostEntity.Users>
        get() = _profile


    private val _dataType = MutableLiveData<DataType>()
    val dataType: LiveData<DataType> = _dataType

    fun setDataType(dataType: DataType) {
        _dataType.value = dataType
    }

    private var _user = MutableLiveData<PostEntity.Users>()
    val user: LiveData<PostEntity.Users>
        get() = _user


    private var _wuser = MutableLiveData<PostEntity.Users>()
    val wuser: LiveData<PostEntity.Users>
        get() = _wuser

    val edited = MutableLiveData(empty)
    val editedJob = MutableLiveData(emptyJob)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        getProfile()
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    fun getProfile() = viewModelScope.launch {
        _state.postValue(FeedModelState(loading = true))
        try {
            _user.value = repository.getProfile()
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            _state.postValue(FeedModelState(error = true))
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                _wuser.value = repository.getUser(userId)
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
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

    fun editJob(job: Job) {
        editedJob.value = job
    }

    fun removeJobById(job: Job) {
        viewModelScope.launch {
            try {
                repository.removeJobById(job.id)

            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }


}
