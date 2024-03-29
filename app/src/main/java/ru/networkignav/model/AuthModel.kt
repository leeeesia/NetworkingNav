package ru.networkignav.model


data class AuthModel(
    val id: Int = 0,
    val token: String? = null,
)

data class AuthModelState(
    val isActing: Boolean = false,
    val error: Boolean = false,
    val response: AuthResponse = AuthResponse(),
)

data class AuthResponse(
    val code: Int = 0,
    val message: String? = null,
)



