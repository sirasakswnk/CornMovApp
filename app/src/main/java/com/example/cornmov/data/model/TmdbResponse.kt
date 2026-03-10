package com.example.cornmov.data.api

import com.google.gson.annotations.SerializedName

data class MovieListResponse(
    val results: List<MovieResponse>
)

data class MovieResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")    val posterPath: String?,
    @SerializedName("backdrop_path")  val backdropPath: String?,
    @SerializedName("vote_average")   val voteAverage: Double,
    @SerializedName("release_date")   val releaseDate: String = "",
    @SerializedName("genre_ids")      val genreIds: List<Int> = emptyList()
)

data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")         val posterPath: String?,
    @SerializedName("backdrop_path")       val backdropPath: String?,
    @SerializedName("vote_average")        val voteAverage: Double,
    @SerializedName("vote_count")          val voteCount: Int,
    val runtime: Int,
    @SerializedName("release_date")        val releaseDate: String = "",
    @SerializedName("original_language")   val originalLanguage: String = "",
    val genres: List<GenreResponse>,
    val credits: CreditsResponse?,
    @SerializedName("production_countries") val productionCountries: List<ProductionCountryResponse> = emptyList()
)

data class GenreResponse(
    val id: Int,
    val name: String
)

data class ProductionCountryResponse(
    @SerializedName("iso_3166_1") val code: String,
    val name: String
)

data class CreditsResponse(
    val cast: List<CastResponse>,
    val crew: List<CrewResponse>
)

data class CastResponse(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class CrewResponse(
    val id: Int,
    val name: String,
    val job: String,
    val department: String
)