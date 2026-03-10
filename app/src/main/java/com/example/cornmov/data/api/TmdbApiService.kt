package com.example.cornmov.data.api

// data/api/TmdbApiService.kt
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrending(
        @Query("api_key")  apiKey: String,
        @Query("language") language: String = "th-TH",
        @Query("page")     page: Int = 1
    ): MovieListResponse

    @GET("movie/popular")
    suspend fun getPopular(
        @Query("api_key")  apiKey: String,
        @Query("language") language: String = "th-TH",
        @Query("page")     page: Int = 1
    ): MovieListResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id")          movieId: Int,
        @Query("api_key")          apiKey: String,
        @Query("language")         language: String = "th-TH",
        @Query("append_to_response") append: String = "credits"
    ): MovieDetailResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key")  apiKey: String,
        @Query("query")    query: String,
        @Query("language") language: String = "th-TH",
        @Query("page")     page: Int = 1
    ): MovieListResponse

    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("api_key")      apiKey: String,
        @Query("with_genres")  genreId: Int,
        @Query("language")     language: String = "th-TH",
        @Query("page")         page: Int = 1
    ): MovieListResponse
}
