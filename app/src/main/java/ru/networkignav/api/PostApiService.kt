package ru.networkignav.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.networkignav.dto.Media
import ru.networkignav.dto.Post
import ru.networkignav.entity.PostEntity
import ru.networkignav.model.AuthModel

interface PostApiService {
    @GET("/api/posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("/api/posts/{post_id}/before/")
    suspend fun getBefore(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("/api/posts/{post_id}/after/")
    suspend fun getAfter(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>
    @DELETE("/api/posts/{post_id}/")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>


    @GET("/api/my/wall/")
    suspend fun getMyWall(): Response<List<Post>>

    @GET("/api/my/wall/latest/")
    suspend fun getLatestMyWall(@Query("count") count: Int): Response<List<Post>>

    @GET("/api/my/wall/{post_id}/before/")
    suspend fun getBeforeMyWall(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("/api/my/wall/{post_id}/newer/")
    suspend fun getAfterMyWall(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>


    @GET("/api/posts/")
    suspend fun getPosts(): Response<List<Post>>

    @POST("/api/posts")
    suspend fun savePosts(@Body post: Post): Response<Post>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<Media>


    @GET("/api/users/{user_id}/")
    suspend fun getUser(@Path("user_id") userId: String): Response<PostEntity.Users>


    @GET("/api/{author_id}/wall/")
    suspend fun getWallByAuthor(@Path("author_id") authorId: String): Response<List<Post>>

    @FormUrlEncoded
    @POST("/api/users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") password: String,
    ): Response<AuthModel>

    @FormUrlEncoded
    @POST("/api/users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthModel>
}
