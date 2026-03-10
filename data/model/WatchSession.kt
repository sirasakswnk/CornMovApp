package com.example.cornmov.data.model

data class WatchSession(
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String = "",
    val rating: Double = 0.0,
    val releaseYear: String = "",
    val addedAt: Long = 0L,
    val status: String = "Want",
    val personalRating: Float = 0f,
    val reviewText: String = "",
    val type: String = "Movie" // ✅ เพิ่มฟิลด์นี้ (ค่าเริ่มต้นให้เป็น "Movie", ถ้าเป็นซีรีส์ให้เซฟเป็น "Series")
)