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
import ru.networkignav.dao.PostRemoteKeyDao
import ru.networkignav.util.ApiError
import ru.networkignav.util.NetworkError
import ru.networkignav.util.UnknownError
import ru.networkignav.api.PostApiService
import ru.networkignav.dao.PostDao
import ru.networkignav.db.AppDb
import ru.networkignav.dto.Attachment
import ru.networkignav.dto.AttachmentType
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Media
import ru.networkignav.dto.Post
import ru.networkignav.entity.PostEntity
import ru.networkignav.entity.toEntity
import ru.networkignav.model.AuthModel
import java.io.File
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val dao_profile: PostDao,
    private val apiService: PostApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {
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
        .map { it.map (PostEntity::toDto) }

    @OptIn(ExperimentalPagingApi::class)
    override val data_profile: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao_profile.getPagingSource() },
        remoteMediator = ProfileRemoteMediator(
            apiService = apiService,
            postDao = dao_profile,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { it.map (PostEntity::toDto) }

    override fun getNewerCount(): Flow<Int> = flow {
        while (true) {
            try {
                kotlinx.coroutines.delay(10_000)
                val id = if (dao.isEmpty()) 0L else dao.getLateId()
                val response = apiService.getLatest(id.toInt())

                val posts = response.body().orEmpty()

                dao.insertShadow(posts.toEntity(true)) //здесь посты с совпадающим id в базе данных  не будут заменены

                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun getProfileNewerCount(): Flow<Int> = flow {
        while (true) {
            try {
                kotlinx.coroutines.delay(10_000)
                val id = if (dao_profile.isEmpty()) 0L else dao.getLateId()
                val response = apiService.getLatestMyWall(id.toInt())

                val posts = response.body().orEmpty()

                dao_profile.insertShadow(posts.toEntity(true)) //здесь посты с совпадающим id в базе данных  не будут заменены

                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    override suspend fun getPostsByUserId(userId: String) {
        val response = apiService.getWallByAuthor(userId)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun getMyWall() {
        val response = apiService.getMyWall()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao_profile.insert(posts.map(PostEntity::fromDto))
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
