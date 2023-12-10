package ru.networkignav.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.networkignav.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {

    @Provides
    fun providePostDao(
        appDb: AppDb
    ): PostDao = appDb.postDao()

    @Provides
    fun provideRemoteKeyDao(
        appDb: AppDb
    ): PostRemoteKeyDao = appDb.postRemoteKeyDao()
}