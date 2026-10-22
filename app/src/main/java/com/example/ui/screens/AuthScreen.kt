package com.sesmartcell.notaservice.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sesmartcell.notaservice.R
import com.sesmartcell.notaservice.auth.AuthManager
import com.sesmartcell.notaservice.ui.theme.PrimaryBlue
import com.sesmartcell.notaservice.ui.theme.PrimaryBlueLight

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    var showOtpDialog by remember { mutableStateOf(false) }
    var verificationIdState by remember { mutableStateOf("") }
    var enteredOtpCode by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0B132B)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, PrimaryBlueLight, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.se_smart_cell_logo),
                    contentDescription = "Logo SE SMART CELL",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SE SMART CELL",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Aplikasi Nota Service HP & Laptop",
                fontSize = 13.sp,
                color = Color(0xFF93C5FD),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(22.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlueLight,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Nomor HP",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Google",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Mode Cepat",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = selectedTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Masuk dengan Nomor Handphone",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            "Gunakan nomor HP Anda untuk masuk ke sistem nota dan data bengkel service.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Nomor Handphone (cth: 08123456789)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlueLight) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlueLight,
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = PrimaryBlueLight,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Nama Pemilik / Teknisi Toko") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlueLight) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlueLight,
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = PrimaryBlueLight,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("PIN Akses (4-6 Angka, opsional)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlueLight) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_code_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlueLight,
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = PrimaryBlueLight,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.trim().length < 8) {
                                    Toast.makeText(context, "Masukkan nomor handphone yang valid!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val name = if (ownerName.isBlank()) "Pemilik SE SMART CELL" else ownerName.trim()
                                AuthManager.loginWithPhone(
                                    context = context,
                                    phoneNumber = phoneNumber,
                                    ownerName = name,
                                    pin = pinCode
                                )
                                Toast.makeText(context, "Selamat datang, $name!", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("phone_login_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk Aplikasi Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val activity = context as? Activity
                                if (activity == null) {
                                    Toast.makeText(context, "Activity tidak tersedia", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                if (phoneNumber.trim().length < 8) {
                                    Toast.makeText(context, "Masukkan nomor HP terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                isSendingOtp = true
                                AuthManager.sendPhoneOtp(
                                    activity = activity,
                                    phoneNumber = phoneNumber,
                                    onCodeSent = { id, _ ->
                                        isSendingOtp = false
                                        verificationIdState = id
                                        showOtpDialog = true
                                        Toast.makeText(context, "Kode verifikasi telah dikirim via SMS!", Toast.LENGTH_SHORT).show()
                                    },
                                    onVerificationCompleted = { credential ->
                                        isSendingOtp = false
                                        AuthManager.auth.signInWithCredential(credential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(context, "Verifikasi instan berhasil!", Toast.LENGTH_SHORT).show()
                                                    onAuthSuccess()
                                                }
                                            }
                                    },
                                    onError = { err ->
                                        isSendingOtp = false
                                        Toast.makeText(context, "SMS Gateway: $err (Silakan gunakan tombol Masuk Aplikasi Sekarang)", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isSendingOtp,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryBlueLight, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengirim SMS OTP...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifikasi OTP SMS Firebase", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = selectedTab == 1) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            FeatureRow(
                                icon = Icons.Default.CloudDone,
                                title = "Penyimpanan Cloud Firebase",
                                desc = "Data nota, pembayaran & pengaturan otomatis tersimpan di cloud Firestore"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FeatureRow(
                                icon = Icons.Default.Sync,
                                title = "Sinkronisasi Realtime",
                                desc = "Akses data nota Anda di berbagai perangkat Android kapan saja"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FeatureRow(
                                icon = Icons.Default.Lock,
                                title = "Autentikasi Akun Google",
                                desc = "Aman dengan integrasi resmi Google Credential Manager"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val activity = context as? Activity ?: return@Button
                            isLoading = true
                            AuthManager.signInWithGoogle(
                                activity = activity,
                                scope = scope,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Selamat datang di SE SMART CELL!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                },
                                onError = { error ->
                                    isLoading = false
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                },
                                onCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Masuk dengan Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = selectedTab == 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Mulai Langsung Pakai",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gunakan aplikasi langsung di perangkat ini tanpa login akun online. Seluruh fitur pembuatan nota, cetak thermal Bluetooth, invoice PDF, QRIS pembayaran, dan laporan keuangan tetap berfungsi 100% menggunakan database lokal Room SQLite.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                AuthManager.loginAsGuest(context)
                                Toast.makeText(context, "Masuk mode langsung pakai!", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("guest_login_button")
                        ) {
                            Text("Buka Aplikasi Sekarang (Offline)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SE SMART CELL - Sistem Manajemen Nota & Service HP/Laptop\nVersi 1.0 Final - Siap Pakai",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showOtpDialog) {
            AlertDialog(
                onDismissRequest = { showOtpDialog = false },
                title = { Text("Verifikasi SMS OTP", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Masukkan 6 digit kode yang dikirim ke nomor $phoneNumber:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = enteredOtpCode,
                            onValueChange = { if (it.length <= 6) enteredOtpCode = it },
                            label = { Text("Kode OTP (6 digit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredOtpCode.length < 6) {
                                Toast.makeText(context, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            AuthManager.verifyPhoneOtpAndSignIn(
                                verificationId = verificationIdState,
                                smsCode = enteredOtpCode,
                                onSuccess = {
                                    showOtpDialog = false
                                    Toast.makeText(context, "Login nomor HP berhasil!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) {
                        Text("Verifikasi & Masuk")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
        }
    }
}
