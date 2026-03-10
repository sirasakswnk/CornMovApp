package com.example.cornmov.show

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornmov.MainActivity
import com.example.cornmov.data.viewmodel.AuthViewModel
import com.example.cornmov.ui.theme.CornmovTheme

// ui/auth/RegisterActivity.kt

class RegisterActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CornmovTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = { navigateToMain() },
                    onGoToLogin = { finish() }
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

// ──────────────────────────────────────────
// RegisterScreen Composable
// ──────────────────────────────────────────
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val selectedGenres = remember { mutableStateListOf<String>() }

    val genres = listOf(
        "Action", "Drama", "Comedy", "Horror",
        "Sci-Fi", "Romance", "Thriller", "Animation",
        "Fantasy", "Documentary"
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> onRegisterSuccess()
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context,
                    (authState as AuthViewModel.AuthState.Error).message,
                    Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onGoToLogin) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE01C2E)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "สมัครสมาชิก",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "สร้างบัญชีเพื่อเริ่มต้นใช้งาน",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            // ── ข้อมูลส่วนตัว ──
            SectionCard(title = "ข้อมูลส่วนตัว") {

                // ชื่อ
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อผู้ใช้") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = watchListTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("อีเมล") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = watchListTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // เบอร์โทร
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("เบอร์โทรศัพท์") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = watchListTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── รหัสผ่าน ──
            SectionCard(title = "รหัสผ่าน") {

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("รหัสผ่าน") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff, null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = watchListTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("ยืนยันรหัสผ่าน") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff, null
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text("รหัสผ่านไม่ตรงกัน", color = Color.Red)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = watchListTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── แนวหนังที่ชอบ ──
            SectionCard(title = "แนวหนังที่ชอบ") {
                Text(
                    text = "เลือกได้มากกว่า 1 แนว เพื่อรับคำแนะนำที่ตรงใจ ✨",
                    fontSize = 12.sp,
                    color = Color(0xFFBBBBBB),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.forEach { genre ->
                        val isSelected = genre in selectedGenres
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedGenres.remove(genre)
                                else selectedGenres.add(genre)
                            },
                            label = { Text(genre, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor    = Color(0xFFE01C2E),
                                selectedLabelColor        = Color.White,
                                containerColor            = Color(0xFFFFF8F8),
                                labelColor                = Color(0xFF777777)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── ปุ่มสมัคร ──
            Button(
                onClick = {
                    when {
                        name.isEmpty() ->
                            Toast.makeText(context, "กรุณากรอกชื่อ", Toast.LENGTH_SHORT).show()
                        email.isEmpty() ->
                            Toast.makeText(context, "กรุณากรอกอีเมล", Toast.LENGTH_SHORT).show()
                        password.length < 6 ->
                            Toast.makeText(context, "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร", Toast.LENGTH_SHORT).show()
                        password != confirmPassword ->
                            Toast.makeText(context, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                        else -> viewModel.register(email, password, name, phone, selectedGenres.toList())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE01C2E)),
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("สร้างบัญชี", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── มีบัญชีแล้ว ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("มีบัญชีแล้ว? ", color = Color(0xFF999999), fontSize = 13.sp)
                TextButton(onClick = onGoToLogin) {
                    Text(
                        text = "เข้าสู่ระบบ",
                        color = Color(0xFFE01C2E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ──────────────────────────────────────────
// Reusable Composables
// ──────────────────────────────────────────

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE01C2E),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            content()
        }
    }
}

@Composable
fun watchListTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFE01C2E),
    focusedLabelColor  = Color(0xFFE01C2E),
    cursorColor        = Color(0xFFE01C2E)
)