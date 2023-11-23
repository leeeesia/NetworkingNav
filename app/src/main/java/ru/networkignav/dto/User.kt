package ru.networkignav.dto


data class User(
    val id: String,
    val username: String="",
    val email: String="",
    val fullName: String="",
    val bio: String?= null,
    val profileImage: String?="",
    val followers: Int=0,
    val following: Int=0
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
    val user: User,
    val posts: List<Post>,
    val events: List<Event>,
    val jobs: List<UserJob>,
    val notifications: List<Notification>
)




