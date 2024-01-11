package ru.networkignav.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import ru.networkignav.dto.Attachment
import ru.networkignav.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar:String?,
    val content: String,
    val published: String,
    val mentionedMe : Boolean,
    val likedByMe: Boolean,
    val ownedByMe:Boolean = false,
    val hidden: Boolean = false,
    @Embedded
    var attachment: Attachment? ,
    @Embedded
    val users: Users?,
) {
    @Entity
    data class Users(
        val userId: String,
        val login: String,
        val name: String,
    )
    fun toDto() = Post(id,  authorId, author, authorAvatar,content, published,mentionedMe, likedByMe, attachment, ownedByMe, users, hidden)


    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.mentionedMe,
                dto.likedByMe,
                dto.ownedByMe,
                dto.hidden,
                dto.attachment,
                dto.users,
            )

    }
}
fun List<PostEntity>.toDto():List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity.Companion::fromDto).map{
    it.copy(hidden = hidden)
}

