package ru.networkignav.dto

sealed interface FeedItem {
    val id: String
}

data class Post(
    override val id: String,
    val userId: String,
    val text: String,
    val media: Media?,
    val location: String?,
    val createdAt: String,
    val hidden: Boolean
):FeedItem

data class Media(
    val imageUrl: String?,
    val audioUrl: String?,
    val videoUrl: String?
)
