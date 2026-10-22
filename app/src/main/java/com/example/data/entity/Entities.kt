package com.sesmartcell.notaservice.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_notes",
    indices = [Index(value = ["noteNumber"], unique = true)]
)
data class ServiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteNumber: String,
    val entryDate: Long = System.currentTimeMillis(),
    val customerName: String,
    val customerPhone: String,
    val deviceType: String,
    val brand: String,
    val model: String,
    val imeiOrSerial: String = "",
    val accessories: String = "",
    val issueDescription: String,
    val technicianCheckResult: String = "",
    val repairAction: String = "",
    val serviceFee: Long = 0,
    val sparepartFee: Long = 0,
    val discount: Long = 0,
    val downPayment: Long = 0,
    val estimatedCompletionDate: String = "",
    val technicianName: String = "Teknisi SE SMART",
    val serviceStatus: String = "Menunggu",
    val warrantyPeriod: String = "30 Hari",
    val warrantyEndDate: String = "",
    val initialPaymentMethod: String = "Tunai",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalCost: Long
        get() = maxOf(0L, serviceFee + sparepartFee - discount)
}

@Entity(tableName = "payment_records")
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteNumber: String,
    val amount: Long,
    val paymentMethod: String,
    val referenceNumber: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentType: String = "Pembayaran",
    val isVerified: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val storeName: String = "SE SMART CELL",
    val storeTagline: String = "Service Smartphone & Laptop Profesional",
    val storeAddress: String = "Jl. Raya Veteran No. 88, Pusat Service & Sparepart",
    val storePhone: String = "081234567890",
    val qrisMerchantName: String = "SE SMART CELL INDONESIA",
    val qrisNmid: String = "ID102030405060",
    val bankAccounts: String = "BCA: 123-456-7890 (a/n SE SMART CELL)\nMANDIRI: 987-654-3210 (a/n SE SMART CELL)\nBRI: 5544-3322-1100 (a/n SE SMART CELL)",
    val ewalletAccounts: String = "DANA: 0812-3456-7890 (SE SMART CELL)\nOVO: 0812-3456-7890 (SE SMART CELL)\nGoPay: 0812-3456-7890 (SE SMART CELL)",
    val warrantyTerms: String = "1. Garansi berlaku sesuai periode sejak tanggal pengambilan unit.\n2. Segel garansi wajib utuh, tidak rusak/sobek.\n3. Garansi tidak berlaku untuk kerusakan akibat ...",
    val adminPin: String = "1234"
)
