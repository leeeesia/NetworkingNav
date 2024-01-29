package ru.networkignav.dto

import kotlinx.coroutines.flow.Flow


data class Users(
    val id: Int,
    val login: String,
    val name: String,
    val avatar: String
)

data class UserJob(
    val id: String,
    val userId: String,
    val position: String,
    val company: String,
    val startDate: String,
    val endDate: String?
)

data class UserProfile(
    val user: Users,
    val posts: List<Post>,
    val events: List<Event>,
    val jobs: List<UserJob>,
    val notifications: List<Notification>
)




