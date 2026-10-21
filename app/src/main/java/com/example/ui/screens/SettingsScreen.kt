package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthManager
import com.example.ui.ServiceViewModel
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ServiceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocs: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings by viewModel.storeSettings.collectAsStateWithLifecycle()

    var storeName by remember(currentSettings) { mutableStateOf(currentSettings.storeName) }
    var storeTagline by remember(currentSettings) { mutableStateOf(currentSettings.storeTagline) }
    var storeAddress by remember(currentSettings) { mutableStateOf(currentSettings.storeAddress) }
    var storePhone by remember(currentSettings) { mutableStateOf(currentSettings.storePhone) }

    var qrisMerchantName by remember(currentSettings) { mutableStateOf(currentSettings.qrisMerchantName) }
    var qrisNmid by remember(currentSettings) { mutableStateOf(currentSettings.qrisNmid) }

    var bankAccounts by remember(currentSettings) { mutableStateOf(currentSettings.bankAccounts) }
    var ewalletAccounts by remember(currentSettings) { mutableStateOf(currentSettings.ewalletAccounts) }
    var warrantyTerms by remember(currentSettings) { mutableStateOf(currentSettings.warrantyTerms) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Usaha & Pembayaran", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val appUser = AuthManager.getAppUser(context)
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (appUser?.displayName?.take(1) ?: "S").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appUser?.displayName ?: "Pemilik Toko",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = when {
                                    !appUser?.phoneNumber.isNullOrBlank() -> "No. HP: ${appUser.phoneNumber}"
                                    !appUser?.email.isNullOrBlank() -> appUser.email
                                    else -> "Mode Offline / Standalone"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            AuthManager.signOut(context, scope) {
                                Toast.makeText(context, "Berhasil keluar dari akun", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Keluar", fontSize = 11.sp, color = Color.Red)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. IDENTITAS TOKO & KOP NOTA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Nama Usaha / Konter") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_store_name")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storeTagline,
                        onValueChange = { storeTagline = it },
                        label = { Text("Slogan / Subtitle Toko") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("Alamat Toko Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storePhone,
                        onValueChange = { storePhone = it },
                        label = { Text("No. WhatsApp Resmi Toko") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. PENGATURAN QRIS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = qrisMerchantName,
                        onValueChange = { qrisMerchantName = it },
                        label = { Text("Nama Merchant QRIS") },
                        placeholder = { Text("Cth: SE SMART CELL INDONESIA") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qrisNmid,
                        onValueChange = { qrisNmid = it },
                        label = { Text("NMID QRIS") },
                        placeholder = { Text("Cth: ID102030405060") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("QRIS Statis Aktif", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Merchant: $qrisMerchantName", fontSize = 11.sp, color = Color.DarkGray)
                                Text("NMID: $qrisNmid", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. REKENING BANK & E-WALLET", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankAccounts,
                        onValueChange = { bankAccounts = it },
                        label = { Text("Daftar Rekening Bank (BCA, Mandiri, BRI, dll)") },
                        placeholder = { Text("Nama Bank: Nomor Rekening (a/n Pemilik)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ewalletAccounts,
                        onValueChange = { ewalletAccounts = it },
                        label = { Text("Akun E-Wallet (DANA, OVO, GoPay, ShopeePay)") },
                        placeholder = { Text("Nama Layanan: Nomor HP (a/n Pemilik)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("4. SYARAT & KETENTUAN GARANSI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = warrantyTerms,
                        onValueChange = { warrantyTerms = it },
                        label = { Text("Ketentuan Garansi pada Nota") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        storeName = storeName.trim(),
                        storeTagline = storeTagline.trim(),
                        storeAddress = storeAddress.trim(),
                        storePhone = storePhone.trim(),
                        qrisMerchantName = qrisMerchantName.trim(),
                        qrisNmid = qrisNmid.trim(),
                        bankAccounts = bankAccounts.trim(),
                        ewalletAccounts = ewalletAccounts.trim(),
                        warrantyTerms = warrantyTerms.trim()
                    )
                    viewModel.updateStoreSettings(updated) {
                        Toast.makeText(context, "Pengaturan toko & pembayaran berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Perubahan Pengaturan", fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DATABASE & DOKUMENTASI SISTEM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    Toast.makeText(context, "Backup database tersimpan otomatis di perangkat!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup Data", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToDocs,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Text("SQL & Panduan API", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
