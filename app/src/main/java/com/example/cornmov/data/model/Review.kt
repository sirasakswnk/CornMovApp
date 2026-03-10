package com.example.cornmov.data.model

data class Review(
    val reviewId: String = "",
    val uid: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L,
    val likes: List<String> = emptyList()
) {
    val likeCount get() = likes.size
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - createdAt
        val days = diff / (1000 * 60 * 60 * 24)
        val weeks = days / 7
        return when {
            days < 1  -> "วันนี้"
            days < 7  -> "${days} วันที่แล้ว"
            weeks < 4 -> "${weeks} สัปดาห์ที่แล้ว"
            else      -> "${weeks / 4} เดือนที่แล้ว"
        }
    }
}

sealed class ReviewSubmitState {
    object Idle : ReviewSubmitState()
    object Loading : ReviewSubmitState()
    object Success : ReviewSubmitState()
    data class Error(val message: String) : ReviewSubmitState()
}