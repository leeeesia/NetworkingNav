package ru.networkignav.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.networkignav.dto.Job

interface JobApiService {
    @GET("/api/{userId}/jobs")
    suspend fun getJobs(@Path("userId")authorId: String) : Response<List<Job>>
    @GET("/api/my/jobs")
    suspend fun getMyJobs() : Response<List<Job>>

    @POST("/api/my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("/api/my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Int): Response<Unit>
}