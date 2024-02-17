package ru.networkignav.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.networkignav.entity.EventKeyEntity

@Dao
interface EventRemoteKeyDao {

    @Query("SELECT max(`key`) FROM EventKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT min(`key`) FROM EventKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyEntity: EventKeyEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<EventKeyEntity>)
    @Query("DELETE FROM EventKeyEntity")
    suspend fun removeAll()
}