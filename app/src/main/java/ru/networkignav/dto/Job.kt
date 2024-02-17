package ru.networkignav.dto

data class Job(
    override val id: Int,
    val name: String,
    val position: String,
    val start: String,
    val finish:String,
    val link: String?
):FeedItem
