package ru.networkignav.ui.profile

import android.util.Log
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
import ru.networkignav.dto.Post
import ru.networkignav.dto.Users
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

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState> get() = _state

    val data: Flow<List<FeedItem>> = appAuth.state
        .flatMapLatest { repository.data_profile }
        .flowOn(Dispatchers.Default)
    private var _profile = MutableLiveData<Users>()
    val profile: LiveData<Users>
        get() = _profile


    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val newerCount: Flow<Int> = repository.getProfileNewerCount()


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

}
