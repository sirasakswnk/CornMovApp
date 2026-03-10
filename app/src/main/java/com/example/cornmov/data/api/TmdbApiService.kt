package com.example.cornmov.data.api

// data/api/TmdbApiService.kt
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "th-TH",
        @Query("page") page: Int = 1   // ✅ เพิ่ม
    ): MovieListResponse

    @GET("movie/popular")
    suspend fun getPopular(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "th-TH",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "th-TH",
        @Query("append_to_response") append: String = "credits"
    ): MovieDetailResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "th-TH",
        @Query("page") page: Int = 1
    ): MovieListResponse


    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "th-TH",
        @Query("page") page: Int = 1
    ): MovieListResponse


}

// Response wrapper
data class MovieListResponse(
    val results: List<MovieResponse>
)

data class MovieResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList()  // ✅ เพิ่ม
)

data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    val runtime: Int,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("original_language") val originalLanguage: String = "",
    val genres: List<GenreResponse>,
    val credits: CreditsResponse?,
    @SerializedName("production_countries")
    val productionCountries: List<ProductionCountryResponse> = emptyList()  // ✅ เพิ่ม
)

data class GenreResponse(val id: Int, val name: String)

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
