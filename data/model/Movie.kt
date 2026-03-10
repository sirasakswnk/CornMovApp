package com.example.cornmov.data.model

// data/model/Movie.kt
data class Movie(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val rating: Double = 0.0,
    val releaseDate: String = ""
) {
    val posterUrl get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
    val releaseYear get() = releaseDate.take(4)
    val ratingFormatted get() = String.format("%.1f", rating)
}