package ru.networkignav.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Int,
) {
    enum class KeyType {
        AFTER,
        BEFORE,
    }
}