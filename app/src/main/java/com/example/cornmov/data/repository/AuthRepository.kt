package com.example.cornmov.data.repository

import com.example.cornmov.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// data/repository/AuthRepository.kt
class AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // ──────────── REGISTER ────────────
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        favoriteGenres: List<String>
    ): Result<Unit> = runCatching {

        // Step 1: สร้าง account ใน Firebase Auth
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid

        // Step 2: บันทึกข้อมูลเพิ่มเติมลง Firestore
        val user = User(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            favoriteGenres = favoriteGenres,
            createdAt = System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(user).await()
    }

    // ──────────── LOGIN ────────────
    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    // ──────────── LOGOUT ────────────
    fun logout() {
        auth.signOut()
    }

    // ──────────── RESET PASSWORD ────────────
    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    val isLoggedIn: Boolean get() = auth.currentUser != null
    val currentUid: String? get() = auth.currentUser?.uid




    
}