package ru.networkignav.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.networkignav.repository.EventRepository
import ru.networkignav.repository.EventRepositoryImpl
import ru.networkignav.repository.PostRepository
import ru.networkignav.repository.PostRepositoryImpl
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository

    @Singleton
    @Binds
    fun bindsEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

}