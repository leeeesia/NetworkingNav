package ru.networkignav.dto

import ru.networkignav.entity.PostEntity

data class Event(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val type: EventType,
    val likedByMe: Boolean,
    val participatedByMe: Boolean,
    val attachment: Attachment?,
    val link: String?,
    val ownedByMe: Boolean,
    val users: PostEntity.Users?,
    val hidden: Boolean
):FeedItem

enum class EventType {
    ONLINE, OFFLINE
}