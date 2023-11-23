package ru.networkignav.api

import kotlinx.coroutines.Job
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.networkignav.dto.Event
import ru.networkignav.dto.Media
import ru.networkignav.dto.Post
import ru.networkignav.dto.User
import ru.networkignav.model.AuthModel

interface PostApiService {
    @GET("/api/posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("/api/posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("/api/posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    // Events
    @GET("/api/events/")
    suspend fun getEvents(): Response<List<Event>>

    @POST("/api/events/")
    suspend fun createEvent(@Body event: Event): Response<Event>

    @GET("/api/events/latest/")
    suspend fun getLatestEvents(): Response<List<Event>>

    // ... Другие эндпоинты для событий

    // Media
    @POST("/api/media/")
    suspend fun createMedia(@Body media: Media): Response<Media>

    // Jobs
    @GET("/api/my/jobs/")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("/api/my/jobs/")
    suspend fun createJob(@Body job: Job): Response<Job>

    // ... Другие эндпоинты для работы

    // MyWall
    @GET("/api/my/wall/")
    suspend fun getMyWall(): Response<List<Post>>

    @GET("/api/my/wall/latest/")
    suspend fun getLatestMyWall(): Response<List<Post>>

    // ... Другие эндпоинты для стены пользователя

    // Posts
    @GET("/api/posts/")
    suspend fun getPosts(): Response<List<Post>>

    @POST("/api/posts/")
    suspend fun createPost(@Body post: Post): Response<Post>

    @GET("/api/posts/latest/")
    suspend fun getLatestPosts(): Response<List<Post>>

    // ... Другие эндпоинты для постов

    // Users
    @GET("/api/users/")
    suspend fun getUsers(): Response<List<User>>

    //@POST("/api/users/authentication/")
    //suspend fun authenticateUser(@Body authRequest: AuthRequest): Response<AuthModel>

    //@POST("/api/users/registration/")
    //suspend fun registerUser(@Body registrationRequest: RegistrationRequest): Response<AuthModel>

    // ... Другие эндпоинты для пользователей

    // Wall
    @GET("/api/{author_id}/wall/")
    suspend fun getWallByAuthor(@Path("author_id") authorId: String): Response<List<Post>>

    @GET("/api/{author_id}/wall/latest/")
    suspend fun getLatestWallByAuthor(@Path("author_id") authorId: String): Response<List<Post>>

    // ... Другие эндпоинты для стены

    // Другие эндпоинты и методы
    @FormUrlEncoded
    @POST("/api/users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Response<AuthModel>

    @FormUrlEncoded
    @POST("/api/users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String,
    ): Response<AuthModel>
}
