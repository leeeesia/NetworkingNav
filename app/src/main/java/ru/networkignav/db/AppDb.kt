package ru.networkignav.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.networkignav.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.networkignav.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}