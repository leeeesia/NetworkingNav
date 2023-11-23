package ru.networkignav.repository


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.util.ApiError
import ru.networkignav.api.PostApiService
import ru.networkignav.dao.PostDao
import ru.networkignav.db.AppDb
import ru.networkignav.dto.FeedItem
import ru.networkignav.entity.PostEntity
import ru.networkignav.entity.toEntity
import ru.networkignav.model.AuthModel
import java.util.concurrent.CancellationException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
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
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
            //it.map(PostEntity::toDto)
            //    .insertSeparators { previous, _ ->
            //        if (previous?.id?.rem(5) == 0L) {
            //             Ad(Random.nextLong(),"figma.jpg" )
            //        } else{
            //        null
            //    }
            //    }
        }

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


    override suspend fun getAll() {
        val response = apiService.getPosts()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto))
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


}
