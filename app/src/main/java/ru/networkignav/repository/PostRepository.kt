package ru.networkignav.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.networkignav.dto.FeedItem
import java.io.File
import ru.networkignav.model.AuthModel
import ru.networkignav.dto.Post
import ru.networkignav.dto.Users

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    val data_profile: Flow<List<FeedItem>>
    fun getNewerCount(): Flow<Int>
    fun getProfileNewerCount(): Flow<Int>
    suspend fun getAll()
    suspend fun getProfile(): Users
    suspend fun getPostsByUserId(userId: String)

    fun getNewPost()

    suspend fun signIn(login: String, password: String): AuthModel

    suspend fun signUp(login: String, password: String, name: String): AuthModel
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)


}
