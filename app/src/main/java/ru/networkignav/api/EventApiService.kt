package ru.networkignav.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.networkignav.dto.Event
import ru.networkignav.dto.Media

interface EventApiService {
    @GET("/api/events/{id}/before")
    suspend fun getEventsBefore(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/{id}/after")
    suspend fun getEventsAfter(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/latest")
    suspend fun getEventsLatest(@Query("count") count: Int): Response<List<Event>>

    @POST("/api/events")
    suspend fun save(@Body event: Event): Response<Event>

    @DELETE("/api/events/{id}")
    suspend fun removeById(@Path("id") id: Int): Response<Unit>

    @POST("/api/events/{id}/likes")
    suspend fun likeById(@Path("id") id: Int): Response<Event>

    @DELETE("/api/events/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Int): Response<Event>

    @POST("/api/events/{id}/participants")
    suspend fun takePartAtEvent(@Path("id") id: Int): Response<Event>

    @DELETE("/api/events/{id}/participants")
    suspend fun deleteTakingPart(@Path("id") id: Int): Response<Event>

    @Multipart
    @POST("/api/media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>
}
