package com.example.cornmov.data.repository

// data/repository/MovieRepository.kt
//import com.example.cornmov.BuildConfig
import com.example.cornmov.data.api.MovieDetailResponse
import com.example.cornmov.data.api.MovieResponse
import com.example.cornmov.data.api.TmdbApiService
import com.example.cornmov.data.model.CastMember
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.model.MovieDetail
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieRepository {

    private val api: TmdbApiService = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TmdbApiService::class.java)

    private val apiKey = "8f763d3a153ca5572d7949dee47bd366" // ใส่ API Key ของคุณ

    suspend fun getTrending(): Result<List<Movie>> = runCatching {
        api.getTrending(apiKey).results.map { it.toMovie() }
    }

    // pagination หน้า AllMovies
    suspend fun getTrendingPaged(page: Int): Result<List<Movie>> = runCatching {
        api.getTrending(apiKey, page = page).results.map { it.toMovie() }
    }

    suspend fun getPopular(): Result<List<Movie>> = runCatching {
        api.getPopular(apiKey).results.map { it.toMovie() }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail> = runCatching {

        val thDetail = api.getMovieDetail(movieId, apiKey, language = "th-TH")

        val enDetail = api.getMovieDetail(movieId, apiKey, language = "en-US")
        thDetail.toMovieDetail(enGenres = enDetail.genres.map { it.name })
    }

    suspend fun searchMovies(query: String): Result<List<Movie>> = runCatching {
        api.searchMovies(apiKey, query).results.map { it.toMovie() }
    }
    suspend fun discoverByGenre(genreId: Int, page: Int): Result<List<Movie>> = runCatching {
        api.discoverByGenre(apiKey, genreId = genreId, page = page).results.map { it.toMovie() }
    }

    private fun MovieResponse.toMovie() = Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        rating = voteAverage,
        releaseDate = releaseDate,
        genreIds = genreIds
    )

    private fun MovieDetailResponse.toMovieDetail(
        enGenres: List<String> = genres.map { it.name }  // default fallback
    ) = MovieDetail(
        id = id, title = title, overview = overview,
        posterPath = posterPath, backdropPath = backdropPath,
        rating = voteAverage, voteCount = voteCount,
        runtime = runtime, releaseDate = releaseDate,
        originalLanguage = originalLanguage,
        genres = enGenres,
        director = credits?.crew
            ?.firstOrNull { it.job == "Director" }?.name ?: "",
        productionCountry = productionCountries.firstOrNull()?.name ?: "",
        cast = credits?.cast?.take(10)?.map {
            CastMember(it.id, it.name, it.character, it.profilePath)
        } ?: emptyList()
    )

}