package ru.networkignav.model

import ru.networkignav.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)
data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val response: FeedResponse = FeedResponse()
)
data class FeedResponse(
    val code : Int = 0,
    val message: String? = null
)

