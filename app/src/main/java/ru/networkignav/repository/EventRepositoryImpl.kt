package ru.networkignav.repository


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.networkignav.api.EventApiService
import ru.networkignav.dao.EventDao
import ru.networkignav.dao.EventRemoteKeyDao
import ru.networkignav.db.AppDb
import ru.networkignav.dto.Event
import ru.networkignav.dto.FeedItem
import ru.networkignav.entity.EventEntity
import ru.networkignav.util.ApiError
import ru.networkignav.util.NetworkError
import ru.networkignav.util.UnknownError
import java.io.File
import java.io.IOException
import javax.inject.Inject


class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: EventApiService,
    private val eventKeyDao: EventRemoteKeyDao,
    appDb: AppDb
): EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(apiService, eventDao, eventKeyDao, appDb)
    ).flow
        .map { it.map(EventEntity::toDto) }


    override suspend fun save(event: Event) {
        try {
            val response = apiService.save(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun likeById(id: Int) {
        try {
            val response = apiService.likeById(id)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body.copy(likedByMe = true)))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun dislikeById(id: Int) {
        try {
            val response = apiService.dislikeById(id)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body.copy(likedByMe = false)))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Int) {
        try {
            eventDao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(event: Event, file: File) {

    }



}
