package com.aistudio.sesmartcell.srvnot.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.aistudio.sesmartcell.srvnot.ui.ServiceViewModel
import com.aistudio.sesmartcell.srvnot.ui.components.AddPaymentDialog
import com.aistudio.sesmartcell.srvnot.ui.components.DeviceTypeBadge
import com.aistudio.sesmartcell.srvnot.ui.components.ImageReceiptDialog
import com.aistudio.sesmartcell.srvnot.ui.components.PaymentStatusBadge
import com.aistudio.sesmartcell.srvnot.ui.components.PrintReceiptDialog
import com.aistudio.sesmartcell.srvnot.ui.components.StatusBadge
import com.aistudio.sesmartcell.srvnot.ui.theme.PrimaryBlue
import com.aistudio.sesmartcell.srvnot.ui.theme.StatusError
import com.aistudio.sesmartcell.srvnot.ui.theme.StatusSuccess
import com.aistudio.sesmartcell.srvnot.util.FormatUtils
import com.aistudio.sesmartcell.srvnot.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceDetailScreen(
    noteId: Long,
    viewModel: ServiceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()

    val note = notes.find { it.id == noteId }

    var showPrintDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Data nota tidak ditemukan atau telah dihapus")
        }
        return
    }

    val notePayments = remember(payments, note.noteNumber) {
        payments.filter { it.noteNumber == note.noteNumber && it.isVerified }
    }
    val totalPaid = remember(notePayments) {
        notePayments.sumOf { it.amount }
    }
    val sisa = maxOf(0L, note.totalCost - totalPaid)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note.noteNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(note.id) })
