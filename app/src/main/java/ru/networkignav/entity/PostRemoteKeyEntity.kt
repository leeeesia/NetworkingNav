package ru.networkignav.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: String,
) {
    enum class KeyType {
        AFTER,
        BEFORE,
    }
}