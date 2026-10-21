package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PaymentRecordDao
import com.example.data.dao.ServiceNoteDao
import com.example.data.dao.StoreSettingsDao
import com.example.data.entity.PaymentRecordEntity
import com.example.data.entity.ServiceNoteEntity
import com.example.data.entity.StoreSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ServiceNoteEntity::class,
        PaymentRecordEntity::class,
        StoreSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceNoteDao(): ServiceNoteDao
    abstract fun paymentRecordDao(): PaymentRecordDao
    abstract fun storeSettingsDao(): StoreSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "se_smart_cell.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dbInstance = getInstance(context)
                                dbInstance.storeSettingsDao().insertOrUpdate(
                                    StoreSettingsEntity()
                                )
                                seedSampleData(dbInstance)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedSampleData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            val note1 = ServiceNoteEntity(
                id = 1,
                noteNumber = "SC-20261020-001",
                entryDate = now - (dayMs * 2),
                customerName = "Budi Pratama",
                customerPhone = "081288992211",
                deviceType = "HP",
                brand = "Samsung",
                model = "Galaxy A54 5G",
                imeiOrSerial = "354982109823451",
                accessories = "Unit + Softcase",
                issueDescription = "Layar LCD blank hitam setelah jatuh, getar dan suara notifikasi masih aktif",
                technicianCheckResult = "Flexible LCD robek dan panel OLED pecah dalam. Motherboard aman normal",
                repairAction = "Ganti modul LCD OLED Original + Pasang Tempered Glass",
                serviceFee = 150000,
                sparepartFee = 650000,
                discount = 25000,
                downPayment = 300000,
                estimatedCompletionDate = "21 Okt 2026",
                technicianName = "Rian Hidayat",
                serviceStatus = "Sedang Dikerjakan",
                warrantyPeriod = "30 Hari",
                warrantyEndDate = "20 Nov 2026",
                initialPaymentMethod = "QRIS",
                notes = "Pelanggan minta data WhatsApp dan foto jangan sampai hilang"
            )

            val note2 = ServiceNoteEntity(
                id = 2,
                noteNumber = "SC-20261020-002",
                entryDate = now - (dayMs * 4),
                customerName = "Siti Rahmawati",
                customerPhone = "085712345678",
                deviceType = "Laptop",
                brand = "Asus",
                model = "TUF Gaming F15 FX506",
                imeiOrSerial = "M3NRCX0123984A",
                accessories = "Unit + Charger Original 200W",
                issueDescription = "Cepat panas dan sering mati tiba-tiba saat bermain game atau render video",
                technicianCheckResult = "Thermal paste kering total, debu kipas tebal menghambat ventilasi pendingin",
                repairAction = "Deep Cleaning internal fan & heatsink + Repaste Thermal Grizzly Kryonaut",
                serviceFee = 175000,
                sparepartFee = 75000,
                discount = 0,
                downPayment = 250000,
                estimatedCompletionDate = "19 Okt 2026",
                technicianName = "Agus Kurniawan",
                serviceStatus = "Selesai",
                warrantyPeriod = "14 Hari",
                warrantyEndDate = "03 Nov 2026",
                initialPaymentMethod = "Tunai",
                notes = "Sudah di-stress test FurMark 1 jam suhu stabil maksimal 74 derajat Celsius"
            )

            val note3 = ServiceNoteEntity(
                id = 3,
                noteNumber = "SC-20261020-003",
                entryDate = now - (dayMs * 1),
                customerName = "Denny Firmansyah",
                customerPhone = "082199887766",
                deviceType = "HP",
                brand = "iPhone",
                model = "iPhone 13 Pro 128GB",
                imeiOrSerial = "358741098234112",
                accessories = "Unit Saja",
                issueDescription = "Baterai boros (BH 71%), persentase loncat-loncat dan backdoor retak halus",
                technicianCheckResult = "Baterai degraded cycle 980x, housing backglass retak sudut kiri atas",
                repairAction = "Ganti Baterai Original High Capacity + Replace Backglass laser",
                serviceFee = 200000,
                sparepartFee = 750000,
                discount = 50000,
                downPayment = 450000,
                estimatedCompletionDate = "22 Okt 2026",
                technicianName = "Rian Hidayat",
                serviceStatus = "Menunggu Sparepart",
                warrantyPeriod = "90 Hari",
                warrantyEndDate = "18 Jan 2027",
                initialPaymentMethod = "Transfer Bank",
                notes = "Menunggu pengiriman baterai original dari supplier"
            )

            val note4 = ServiceNoteEntity(
                id = 4,
                noteNumber = "SC-20261020-004",
                entryDate = now,
                customerName = "Mega Lestari",
                customerPhone = "089655443322",
                deviceType = "Laptop",
                brand = "Lenovo",
                model = "IdeaPad Slim 3 14ITL6",
                imeiOrSerial = "PF38XYZ99",
                accessories = "Unit + Adaptor Charger + Tas",
                issueDescription = "Keyboard tombol Spasi, Enter, dan Backspace tidak merespons",
                technicianCheckResult = "Jalur keyboard korosi akibat tumpahan kopi tipis",
                repairAction = "Penggantian Keyboard Tanam Baru Original",
                serviceFee = 125000,
                sparepartFee = 225000,
                discount = 0,
                downPayment = 100000,
                estimatedCompletionDate = "23 Okt 2026",
                technicianName = "Agus Kurniawan",
                serviceStatus = "Menunggu",
                warrantyPeriod = "30 Hari",
                warrantyEndDate = "22 Nov 2026",
                initialPaymentMethod = "E-Wallet",
                notes = "Nomor nota baru hari ini"
            )

            db.serviceNoteDao().insertNote(note1)
            db.serviceNoteDao().insertNote(note2)
            db.serviceNoteDao().insertNote(note3)
            db.serviceNoteDao().insertNote(note4)

            db.paymentRecordDao().insertPayment(
                PaymentRecordEntity(
                    noteNumber = note1.noteNumber,
                    amount = 300000,
                    paymentMethod = "QRIS",
                    referenceNumber = "QRIS-88291039",
                    paymentDate = note1.entryDate,
                    paymentType = "DP"
                )
            )
            db.paymentRecordDao().insertPayment(
                PaymentRecordEntity(
                    noteNumber = note2.noteNumber,
                    amount = 250000,
                    paymentMethod = "Tunai",
                    referenceNumber = "CASH-002",
                    paymentDate = note2.entryDate,
                    paymentType = "Pelunasan"
                )
            )
            db.paymentRecordDao().insertPayment(
                PaymentRecordEntity(
                    noteNumber = note3.noteNumber,
                    amount = 450000,
                    paymentMethod = "Transfer Bank",
                    referenceNumber = "BCA-TRF-49102",
                    paymentDate = note3.entryDate,
                    paymentType = "DP"
                )
            )
            db.paymentRecordDao().insertPayment(
                PaymentRecordEntity(
                    noteNumber = note4.noteNumber,
                    amount = 100000,
                    paymentMethod = "E-Wallet",
                    referenceNumber = "DANA-99210",
                    paymentDate = note4.entryDate,
                    paymentType = "DP"
                )
            )
        }
    }
}
