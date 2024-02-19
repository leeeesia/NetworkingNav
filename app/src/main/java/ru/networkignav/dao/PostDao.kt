package ru.networkignav.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.networkignav.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>


    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("UPDATE PostEntity SET hidden = 0")
    fun getNewPost()

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty():Boolean

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getLateId(): Long

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    suspend fun newerCount():Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShadow(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :text WHERE id = :id")
    suspend fun updateContentById(id: String, text: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0) insert(post) else updateContentById(post.id.toString(), post.content)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: String)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeJobById(id: String)
}
