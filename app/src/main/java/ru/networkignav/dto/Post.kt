package ru.networkignav.dto

import ru.networkignav.entity.PostEntity


sealed interface FeedItem {
    val id: Int
}

data class Post(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val content: String,
    val published: Long,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val ownedByMe: Boolean = false,
    val users: PostEntity.Users?,
    val hidden: Boolean
):FeedItem

data class Media(
    val imageUrl: String?,
    val audioUrl: String?,
    val videoUrl: String?
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)


enum class AttachmentType {
    IMAGE
}
