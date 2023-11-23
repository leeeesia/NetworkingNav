package ru.networkignav.dto

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val date: String,
    val organizer: String
)
