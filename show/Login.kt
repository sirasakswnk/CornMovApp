package com.example.cornmov.show

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornmov.MainActivity
import com.example.cornmov.R
import com.example.cornmov.data.viewmodel.AuthViewModel
import com.example.cornmov.ui.theme.CornmovTheme

// ui/auth/LoginActivity.kt
class LoginActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ถ้า login อยู่แล้วข้ามไป MainActivity เลย
        if (viewModel.isLoggedIn) {
            navigateToMain()
            return
        }

        setContent {
            CornmovTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navigateToMain() },
                    onGoToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onGoogleSignIn = { viewModel.loginWithGoogle(this) }
                )
            }
        }
    }


    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ──────────────────────────────────────────
// LoginScreen Composable
// ──────────────────────────────────────────
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // ตัวแปร input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // observe state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> onLoginSuccess()
            is AuthViewModel.AuthState.ResetPasswordSent -> {
                Toast.makeText(context,
                    "ส่งอีเมลรีเซ็ตรหัสผ่านแล้ว กรุณาตรวจสอบกล่องจดหมาย",
                    Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(64.dp))

            // ── Logo ──
            Text(
                text = "CORN",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                letterSpacing = 4.sp
            )
            Text(
                text = "MOV",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE01C2E),
                letterSpacing = 4.sp,
                modifier = Modifier.offset(y = (-12).dp)
            )
            Text(
                text = "Watch Together. Pick Together. Feel the Hype.",
                fontSize = 13.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // ── Form Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        text = "เข้าสู่ระบบ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "ยินดีต้อนรับกลับ! เข้าสู่ระบบเพื่อดูต่อ",
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("อีเมล") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE01C2E),
                            focusedLabelColor  = Color(0xFFE01C2E),
                            cursorColor        = Color(0xFFE01C2E)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("รหัสผ่าน") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE01C2E),
                            focusedLabelColor  = Color(0xFFE01C2E),
                            cursorColor        = Color(0xFFE01C2E)
                        )
                    )

                    // ลืมรหัสผ่าน
                    TextButton(
                        onClick = {
                            if (email.isEmpty()) {
                                Toast.makeText(context,
                                    "กรอกอีเมลก่อนกดลืมรหัสผ่าน",
                                    Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.resetPassword(email)
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "ลืมรหัสผ่าน?",
                            color = Color(0xFFE01C2E),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ปุ่ม Login
                    Button(
                        onClick = {
                            if (email.isEmpty() || password.length < 6) {
                                Toast.makeText(context,
                                    "กรุณากรอกข้อมูลให้ถูกต้อง",
                                    Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE01C2E)
                        ),
                        enabled = authState !is AuthViewModel.AuthState.Loading
                    ) {
                        if (authState is AuthViewModel.AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "เข้าสู่ระบบ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                    Text(
                        text = "  หรือ  ",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                }

                // ── ปุ่ม Google ──
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFDDDDDD)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    enabled = authState !is AuthViewModel.AuthState.Loading
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "เข้าสู่ระบบด้วย Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── ไปสมัคร ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF0F0)
                ),
                border = BorderStroke(1.dp, Color(0xFFE01C2E).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ยังไม่มีบัญชี?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "สมัครบัญชีเลย ฟรี!",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                    OutlinedButton(
                        onClick = onGoToRegister,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE01C2E)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE01C2E)
                        )
                    ) {
                        Text(text = "สมัคร", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}