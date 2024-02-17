package ru.networkignav.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.networkignav.dao.EventDao
import ru.networkignav.dao.EventRemoteKeyDao
import ru.networkignav.dao.JobDao
import ru.networkignav.dao.PostDao
import ru.networkignav.dao.PostRemoteKeyDao
import ru.networkignav.entity.EventEntity
import ru.networkignav.entity.EventKeyEntity
import ru.networkignav.entity.JobEntity
import ru.networkignav.entity.PostEntity
import ru.networkignav.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class, EventEntity::class,
        EventKeyEntity::class, JobEntity::class], version = 1
)

abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao

    abstract fun jobDao(): JobDao
}