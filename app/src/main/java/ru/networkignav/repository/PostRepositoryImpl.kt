package ru.networkignav.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.networkignav.api.JobApiService
import ru.networkignav.api.PostApiService
import ru.networkignav.auth.AppAuth
import ru.networkignav.dao.PostDao
import ru.networkignav.dao.PostRemoteKeyDao
import ru.networkignav.db.AppDb
import ru.networkignav.dto.Attachment
import ru.networkignav.dto.AttachmentType
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Job
import ru.networkignav.dto.Media
import ru.networkignav.dto.Post
import ru.networkignav.entity.PostEntity
import ru.networkignav.model.AuthModel
import ru.networkignav.util.ApiError
import ru.networkignav.util.NetworkError
import ru.networkignav.util.UnknownError
import java.io.File
import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService,
    private val jobApiService: JobApiService,
    private val appAuth: AppAuth,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,

    ) : PostRepository {
    override var userId = "0"

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { it.map(PostEntity::toDto) }

    override val dataProfile: Flow<List<FeedItem>> = flow {
        try {
            val response = apiService.getMyWall()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val posts = response.body() ?: throw RuntimeException("Тело ответа пусто")

            emit(posts)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override val dataUser: Flow<List<FeedItem>> = flow {
        try {

            val response = apiService.getWallByAuthor(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val posts = response.body() ?: throw RuntimeException("Тело ответа пусто")

            emit(posts)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override val dataJob: Flow<List<FeedItem>> = flow {
        try {
            val response = jobApiService.getJobs(userId)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val jobs = response.body() ?: throw RuntimeException("Тело ответа пусто")

            emit(jobs)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override var job: Flow<List<FeedItem>> = flow {
        try {
            val response = jobApiService.getMyJobs()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val jobs = response.body() ?: throw RuntimeException("Тело ответа пусто")

            emit(jobs)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override val user: Flow<PostEntity.Users> = flow {
        try {
            val response = apiService.getUser(appAuth.state.value?.id.toString())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val user = response.body() ?: throw RuntimeException("Тело ответа пусто")

            emit(user)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun getAll() {
        val response = apiService.getPosts()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto))
    }


    override suspend fun getProfile(): PostEntity.Users {
        try {
            val response = apiService.getUser(appAuth.state.value?.id.toString())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUser(userId: String): PostEntity.Users {
        try {
            val response = apiService.getUser(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJob(): List<Job> {
        try {
            val response = jobApiService.getJobs(appAuth.state.value?.id.toString())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getMyJob(): List<Job> {
        try {
            val response = jobApiService.getMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun getPostsByUserId(userId: String) {
        val response = apiService.getWallByAuthor(userId)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun removeById(id: Int) {
        try {
            val response = apiService.deletePost(id.toString())
            dao.removeById(id.toString())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeJobById(id: Int) {
        try {
            dao.removeJobById(id.toString())
            val response = jobApiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun updateUserId(newUserId: String) {
        userId = newUserId
    }


    override fun getNewPost() {
        dao.getNewPost()
    }

    override suspend fun signIn(login: String, password: String): AuthModel {
        val response = apiService.updateUser(login, password)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun signUp(name: String, login: String, password: String): AuthModel {
        val response = apiService.registerUser(login, password, name)

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun save(post: Post) {
        try {
            val response = apiService.savePosts(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = jobApiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun uploadMedia(file: File): Media {
        val formData = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        val response = apiService.uploadMedia(formData)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            val media = uploadMedia(file)

            val response = apiService.savePosts(
                post.copy(
                    attachment = Attachment(
                        url = media.imageUrl.toString(),
                        type = AttachmentType.IMAGE
                    )
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
