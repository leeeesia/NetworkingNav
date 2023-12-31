package ru.networkignav.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.networkignav.dao.PostDao
import ru.networkignav.dao.PostRemoteKeyDao
import ru.networkignav.entity.PostEntity
import ru.networkignav.entity.PostRemoteKeyEntity
import ru.netology.nmedia.util.ApiError
import ru.networkignav.api.PostApiService
import ru.networkignav.db.AppDb

import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    postRemoteKeyDao.max()?.let { id ->
                        apiService.getAfter(id = id, count = state.config.pageSize)
                    } ?: apiService.getLatest(state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id = id, count = state.config.pageSize)
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
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id.toString(),
                            )
                        )
                        if (postDao.isEmpty()) {
                            remoteKeys.add(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id.toString(),
                                )
                            )
                        }
                        postRemoteKeyDao.insert(remoteKeys)
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id.toString(),
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id.toString(),
                            )
                        )
                    }

                }

                postDao.insert(body.map(PostEntity::fromDto))
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }

}