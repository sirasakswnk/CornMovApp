package com.example.cornmov.data.model

// data/model/MovieDetail.kt
data class MovieDetail(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val rating: Double = 0.0,
    val voteCount: Int = 0,
    val runtime: Int = 0,
    val releaseDate: String = "",
    val originalLanguage: String = "",
    val genres: List<String> = emptyList(),
    val director: String = "",
    val productionCountry: String = "",
    val cast: List<CastMember> = emptyList()
) {
    val posterUrl get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
    val releaseYear get() = releaseDate.take(4)
    val ratingFormatted get() = String.format("%.1f", rating)
    val runtimeFormatted get() = "${runtime / 60}h ${runtime % 60}m"
    val languageFormatted get() = when (originalLanguage) {
        "en" -> "English"
        "th" -> "Thai"
        "ja" -> "Japanese"
        "ko" -> "Korean"
        "fr" -> "French"
        else -> originalLanguage.uppercase()
    }
    // แปลง vote_average เป็น % แบบ Rotten Tomatoes
    val rottenScore get() = (rating * 10).toInt()
}

data class CastMember(
    val id: Int = 0,
    val name: String = "",
    val character: String = "",
    val profilePath: String? = null
) {
    val photoUrl get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}