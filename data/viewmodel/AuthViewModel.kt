package com.example.cornmov.data.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.User
import com.example.cornmov.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ui/auth/AuthViewModel.kt
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val auth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object ResetPasswordSent : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // ──────────── REGISTER ────────────
    fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        favoriteGenres: List<String>
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(email, password, name, phone, favoriteGenres)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "เกิดข้อผิดพลาด") }
        }
    }

    // ──────────── LOGIN ────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "เกิดข้อผิดพลาด") }
        }
    }

    // ──────────── RESET PASSWORD ────────────
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.resetPassword(email)
                .onSuccess { _authState.value = AuthState.ResetPasswordSent }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "เกิดข้อผิดพลาด") }
        }
    }

    // ──────────── LOGOUT ────────────
    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    val isLoggedIn: Boolean get() = repository.isLoggedIn

    // รีเซ็ต state กลับ Idle (ใช้หลัง navigate หรือ dismiss error)
    fun resetState() {
        _authState.value = AuthState.Idle
    }


    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(context)

                val signInWithGoogleOption = GetSignInWithGoogleOption
                    .Builder("124969582921-hr3qu2de1kemirv7d1ohdtgdfrkhn552.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )
                    val authResult = auth.signInWithCredential(firebaseCredential).await() // ✅ ใช้ auth ที่ประกาศไว้

                    val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        val user = authResult.user!!
                        val newUser = User(
                            uid = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            phone = "",
                            profileImageUrl = user.photoUrl?.toString() ?: "",
                            favoriteGenres = emptyList(),
                            createdAt = System.currentTimeMillis()
                        )
                        Firebase.firestore.collection("users")
                            .document(user.uid).set(newUser).await()
                    }

                    _authState.value = AuthState.Success
                }

            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Sign-in ล้มเหลว: ${e.message}")
            }
        }
    }
}