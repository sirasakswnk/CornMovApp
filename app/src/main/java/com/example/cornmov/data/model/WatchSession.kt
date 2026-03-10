package com.example.cornmov.data.model

import com.google.firebase.Timestamp

data class WatchSession(
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String = "",
    val rating: Double = 0.0,
    val releaseYear: String = "",
    val addedAt: Timestamp = Timestamp.now(),
    val status: String = "Want",
    val personalRating: Float = 0f,
    val reviewText: String = "",
    val type: String = "Movie"
)