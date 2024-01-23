package ru.networkignav.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.networkignav.dto.FeedItem
import java.io.File
import ru.networkignav.model.AuthModel
import ru.networkignav.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    val data_profile: Flow<PagingData<FeedItem>>
    fun getNewerCount(): Flow<Int>
    fun getProfileNewerCount(): Flow<Int>
    suspend fun getAll()
    suspend fun getPostsByUserId(userId: String)
    suspend fun getMyWall()
    fun getNewPost()

    suspend fun signIn(login: String, password: String): AuthModel

    suspend fun signUp(login: String, password: String, name: String): AuthModel
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)


}
