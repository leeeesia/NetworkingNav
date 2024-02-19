@file:Suppress("KotlinConstantConditions")

package ru.networkignav.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.networkignav.api.EventApiService
import ru.networkignav.dao.EventDao
import ru.networkignav.dao.EventRemoteKeyDao
import ru.networkignav.db.AppDb
import ru.networkignav.entity.EventEntity
import ru.networkignav.entity.EventKeyEntity
import ru.networkignav.util.ApiError
import java.io.IOException

@Suppress("KotlinConstantConditions")
@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: EventApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,

) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, EventEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val result = eventRemoteKeyDao.max()?.let { id ->
                        apiService.getEventsAfter(id = id, count = state.config.pageSize)
                    }

                    if (result == null || !result.isSuccessful || result.body().isNullOrEmpty()) {
                        apiService.getEventsLatest(state.config.pageSize)
                    } else {
                        result
                    }
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getEventsBefore(id = id, count = state.config.pageSize)
                }
            }
            if (!result.isSuccessful) {
                throw HttpException(result)
            }

            val body = result.body() ?: throw ApiError(
                result.code(),
                result.message()
            )


            appDb.withTransaction {

                when (loadType) {

                    LoadType.REFRESH -> {
                        val remoteKeys = mutableListOf(
                            EventKeyEntity(
                                EventKeyEntity.KeyType.AFTER,
                                body.first().id,
                            )
                        )
                        if (eventDao.isEmpty()) {
                            remoteKeys.add(
                                EventKeyEntity(
                                    EventKeyEntity.KeyType.BEFORE,
                                    body.last().id,
                                )
                            )
                        }
                        eventRemoteKeyDao.insert(remoteKeys)
                    }

                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            EventKeyEntity(
                                EventKeyEntity.KeyType.AFTER,
                                body.first().id,
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventKeyEntity(
                                EventKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            )
                        )
                    }

                }

                eventDao.insert(body.map(EventEntity::fromDto))

            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }

}