package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.ServiceNoteEntity
import com.example.ui.ServiceViewModel
import com.example.ui.components.DeviceTypeBadge
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@Composable
fun DashboardScreen(
    viewModel: ServiceViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    val stats = remember(notes, payments) {
        viewModel.getDashboardStats(notes, payments)
    }

    val paymentMap = remember(payments) {
        payments.filter { it.isVerified }.groupBy { it.noteNumber }
    }

    val displayNotes = remember(notes, searchQuery, selectedFilter) {
        notes.filter { note ->
            val matchQuery = searchQuery.isBlank() ||
                note.noteNumber.contains(searchQuery, ignoreCase = true) ||
                note.customerName.contains(searchQuery, ignoreCase = true) ||
                note.brand.contains(searchQuery, ignoreCase = true) ||
                note.model.contains(searchQuery, ignoreCase = true) ||
                note.customerPhone.contains(searchQuery, ignoreCase = true)

            val matchFilter = selectedFilter == "Semua" || note.serviceStatus == selectedFilter
            matchQuery && matchFilter
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header Store Banner
            item {
                StoreHeroHeader(
                    storeName = storeSettings.storeName,
                    tagline = storeSettings.storeTagline,
                    onCreateClick = onNavigateToCreate
                )
            }

            // Stat Cards Grid (2x2)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Ringkasan Servis Hari Ini",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Nota Hari Ini",
                            value = "${stats.notesToday}",
                            subtitle = "Total: ${stats.totalNotes} unit",
                            icon = Icons.Default.Description,
                            gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Sedang Dikerjakan",
                            value = "${stats.inProgressCount}",
                            subtitle = "Antrean: ${stats.waitingCount}",
                            icon = Icons.Default.Build,
                            gradientColors = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Sudah Selesai",
                            value = "${stats.completedCount}",
                            subtitle = "Siap diambil",
                            icon = Icons.Default.CheckCircle,
                            gradientColors = listOf(Color(0xFF065F46), Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Pendapatan Hari Ini",
                            value = FormatUtils.formatRupiah(stats.revenueToday),
                            subtitle = "Bulan Ini: ${FormatUtils.formatRupiah(stats.revenueMonth)}",
                            icon = Icons.Default.MonetizationOn,
                            gradientColors = listOf(Color(0xFF4338CA), Color(0xFF6366F1)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Cari nota, pelanggan, tipe, IMEI...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusFilters = listOf("Semua", "Menunggu", "Sedang Dikerjakan", "Menunggu Sparepart", "Selesai", "Diambil Pelanggan")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(statusFilters) { status ->
                            FilterChip(
                                selected = selectedFilter == status,
                                onClick = { viewModel.selectedStatusFilter.value = status },
                                label = { Text(status, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Recent Notes Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Service Terkini (${displayNotes.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToAllNotes) {
                        Text("Lihat Semua", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Note Item Cards
            if (displayNotes.isEmpty()) {
                item {
                    EmptyNotesPlaceholder(searchQuery = searchQuery)
                }
            } else {
                items(displayNotes, key = { it.id }) { note ->
                    val totalPaid = paymentMap[note.noteNumber]?.sumOf { it.amount } ?: 0L
                    val sisa = maxOf(0L, note.totalCost - totalPaid)

                    DashboardNoteCard(
                        note = note,
                        totalPaid = totalPaid,
                        sisa = sisa,
                        onClick = { onNavigateToDetail(note.id) },
                        onSendWhatsApp = {
                            val msg = WhatsAppHelper.createStatusMessage(note, storeSettings, totalPaid, sisa)
                            WhatsAppHelper.openWhatsApp(context, note.customerPhone, msg)
                        }
                    )
                }
            }
        }

        // Floating Action Button to Create Note
        FloatingActionButton(
            onClick = onNavigateToCreate,
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_create_note")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Buat Nota")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buat Nota", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StoreHeroHeader(
    storeName: String,
    tagline: String,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B132B),
                            Color(0xFF1E293B),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = tagline,
                        fontSize = 12.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCreateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("hero_create_note_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nota Service Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Store App Icon / Logo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, PrimaryBlueLight, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.se_smart_cell_logo),
                        contentDescription = "Logo Toko",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun DashboardNoteCard(
    note: ServiceNoteEntity,
    totalPaid: Long,
    sisa: Long,
    onClick: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("note_card_${note.noteNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Note Number, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.noteNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DeviceTypeBadge(deviceType = note.deviceType)
                }
                StatusBadge(status = note.serviceStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Customer Name, Phone & Device Model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${note.customerName} • ${note.customerPhone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${note.brand} ${note.model}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Issue description
            Text(
                text = "Keluhan: ${note.issueDescription}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Financial Summary & WhatsApp button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FormatUtils.formatRupiah(note.totalCost),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PaymentStatusBadge(totalCost = note.totalCost, totalPaid = totalPaid)
                    }
                    if (sisa > 0) {
                        Text(
                            text = "Sisa: ${FormatUtils.formatRupiah(sisa)}",
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSendWhatsApp,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366))
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotesPlaceholder(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "Tidak ditemukan nota yang sesuai pencarian" else "Belum ada data service yang tercatat",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
