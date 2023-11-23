package ru.networkignav.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.networkignav.dto.Media
import ru.networkignav.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val userId: String,
    val text: String,
    val media: Media?,
    val location: String?,
    val createdAt: String,
    val hidden: Boolean = false,
) {
    fun toDto() = Post(id, userId, text,media, location, createdAt,hidden)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.userId,
                dto.text,
                dto.media,
                dto.location,
                dto.createdAt,
                dto.hidden
            )

    }
}
fun List<PostEntity>.toDto():List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity.Companion::fromDto).map{
    it.copy(hidden = hidden)
}

