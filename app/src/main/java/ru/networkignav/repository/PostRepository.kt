package ru.networkignav.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Job
import ru.networkignav.dto.Post
import ru.networkignav.entity.PostEntity
import ru.networkignav.model.AuthModel
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    val dataProfile: Flow<List<FeedItem>>
    val dataUser: Flow<List<FeedItem>>
    val user: Flow<PostEntity.Users>
    val dataJob: Flow<List<FeedItem>>
    val job: Flow<List<FeedItem>>

    var userId:String
    suspend fun getAll()
    suspend fun getProfile(): PostEntity.Users
    suspend fun getUser(userId: String): PostEntity.Users

    suspend fun getJob(): List<Job>
    suspend fun getMyJob(): List<Job>
    suspend fun getPostsByUserId(userId: String)

    fun getNewPost()
    fun updateUserId(newUserId: String)

    suspend fun signIn(login: String, password: String): AuthModel

    suspend fun signUp(login: String, password: String, name: String): AuthModel
    suspend fun save(post: Post)
    suspend fun saveJob(job: Job)
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun removeById(id: Int)

    suspend fun removeJobById(id: Int)

}
