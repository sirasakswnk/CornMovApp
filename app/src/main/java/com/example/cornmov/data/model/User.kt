package com.example.cornmov.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = "",
    val favoriteGenres: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)