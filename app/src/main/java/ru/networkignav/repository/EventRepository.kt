package ru.networkignav.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.networkignav.dto.Event
import ru.networkignav.dto.FeedItem

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>

    suspend fun likeById(id: Int)
    suspend fun dislikeById(id: Int)
    suspend fun removeById(id: Int)
    suspend fun save(event: Event)

}
