package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ServiceNoteEntity
import com.example.ui.ServiceViewModel
import com.example.ui.components.DeviceTypeBadge
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.PrintReceiptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusError
import com.example.util.FormatUtils
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    viewModel: ServiceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDeviceFilter.collectAsStateWithLifecycle()

    var noteToPrint by remember { mutableStateOf<ServiceNoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<ServiceNoteEntity?>(null) }

    val paymentMap = remember(payments) {
        payments.filter { it.isVerified }.groupBy { it.noteNumber }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Service (${filteredNotes.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("list_fab_create")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Nota")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Cari nomor nota, nama, nomor WA, unit...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("list_search_input")
            )

            val statusFilters = listOf("Semua", "Menunggu", "Sedang Dikerjakan", "Menunggu Sparepart", "Selesai", "Diambil Pelanggan")
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(statusFilters) { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { viewModel.selectedStatusFilter.value = st },
                        label = { Text(st, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            val deviceFilters = listOf("Semua", "HP", "Laptop")
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(deviceFilters) { dev ->
                    FilterChip(
                        selected = selectedDevice == dev,
                        onClick = { viewModel.selectedDeviceFilter.value = dev },
                        label = { Text(if (dev == "Semua") "Semua Perangkat" else dev, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tidak ada data service yang cocok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (searchQuery.isNotEmpty() || selectedStatus != "Semua") {
                            TextButton(onClick = {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedStatusFilter.value = "Semua"
                                viewModel.selectedDeviceFilter.value = "Semua"
                            }) {
                                Text("Reset Filter")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        val totalPaid = paymentMap[note.noteNumber]?.sumOf { it.amount } ?: 0L
                        val sisa = maxOf(0L, note.totalCost - totalPaid)

                        ServiceListItemCard(
                            note = note,
                            totalPaid = totalPaid,
                            sisa = sisa,
                            onClick = { onNavigateToDetail(note.id) },
                            onEdit = { onNavigateToEdit(note.id) },
                            onPrint = { noteToPrint = note },
                            onDelete = { noteToDelete = note },
                            onWhatsApp = {
                                val msg = WhatsAppHelper.createStatusMessage(note, storeSettings, totalPaid, sisa)
                                WhatsAppHelper.openWhatsApp(context, note.customerPhone, msg)
                            }
                        )
                    }
                }
            }
        }
    }

    if (noteToPrint != null) {
        val totalPaid = paymentMap[noteToPrint!!.noteNumber]?.sumOf { it.amount } ?: 0L
        PrintReceiptDialog(
            note = noteToPrint!!,
            store = storeSettings,
            totalPaid = totalPaid,
            onDismiss = { noteToPrint = null }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Yakin ingin menghapus nota ${noteToDelete!!.noteNumber} (${noteToDelete!!.customerName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = noteToDelete!!
                        noteToDelete = null
                        viewModel.deleteNote(toDel) {
                            Toast.makeText(context, "Nota dihapus", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ServiceListItemCard(
    note: ServiceNoteEntity,
    totalPaid: Long,
    sisa: Long,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("list_item_${note.noteNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(note.noteNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    DeviceTypeBadge(deviceType = note.deviceType)
                }
                StatusBadge(status = note.serviceStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${note.customerName} • ${note.brand} ${note.model}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Keluhan: ${note.issueDescription}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(FormatUtils.formatRupiah(note.totalCost), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        PaymentStatusBadge(totalCost = note.totalCost, totalPaid = totalPaid)
                    }
                    if (sisa > 0) {
                        Text("Sisa: ${FormatUtils.formatRupiah(sisa)}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrint, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onWhatsApp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Send, contentDescription = "WA", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
