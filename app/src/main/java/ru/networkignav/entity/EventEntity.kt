package ru.networkignav.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.networkignav.dto.Attachment
import ru.networkignav.dto.AttachmentType
import ru.networkignav.dto.Event
import ru.networkignav.dto.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
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
    val hidden: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    val link: String?,
    val ownedByMe: Boolean,
    @Embedded
    val users: PostEntity.Users?,
) {

    fun toDto(): Event = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        type,
        likedByMe,
        participatedByMe,
        attachment?.toDto(),
        link,
        ownedByMe,
        users,
        hidden
    )

    companion object {
        fun fromDto(dto: Event): EventEntity = EventEntity(
            dto.id,
            dto.authorId,
            dto.author,
            dto.authorAvatar,
            dto.authorJob,
            dto.content,
            dto.datetime,
            dto.published,
            dto.type,
            dto.likedByMe,
            dto.participatedByMe,
            dto.hidden,
            AttachmentEmbeddable.fromDto(dto.attachment),
            dto.link,
            dto.ownedByMe,
            dto.users
        )
    }
}
data class AttachmentEmbeddable(
    var url: String,
    var attachmentType: AttachmentType,
) {
    fun toDto() = Attachment(url, attachmentType)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}


