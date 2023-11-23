package ru.networkignav.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.networkignav.dto.FeedItem
import java.io.File
import ru.networkignav.model.AuthModel
import ru.networkignav.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewerCount(): Flow<Int>
    suspend fun getAll()
    fun getNewPost()

    suspend fun signIn(login: String, password: String): AuthModel

    suspend fun signUp(login: String, password: String, name: String): AuthModel


}
