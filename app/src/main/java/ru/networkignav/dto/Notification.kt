package ru.networkignav.dto

data class Notification(
    val id: String,
    val userId: String,
    val type: String,
    val content: String,
    val timestamp: String
)

