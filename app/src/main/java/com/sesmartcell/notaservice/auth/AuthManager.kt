package com.sesmartcell.notaservice.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class AppUser(
    val uid: String,
    val displayName: String,
    val phoneNumber: String?,
    val email: String?,
    val loginMethod: String // "PHONE"
)

object AuthManager {

    private const val PREFS_NAME = "se_smart_cell_auth_prefs"
    private const val PREFS_USERS = "se_smart_cell_users_db"
    private const val KEY_LOGGED_IN = "key_is_logged_in"
    private const val KEY_UID = "key_user_uid"
    private const val KEY_NAME = "key_user_name"
    private const val KEY_PHONE = "key_user_phone"
    private const val KEY_METHOD = "key_login_method"

    val auth: FirebaseAuth
        get() = Firebase.auth

    val currentUser: FirebaseUser?
        get() = try {
            Firebase.auth.currentUser
        } catch (e: Exception) {
            null
        }

    fun normalizePhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        require(digits.length >= 8) { "Nomor handphone tidak valid" }
        return if (digits.startsWith("0")) "+62${digits.substring(1)}" else digits
    }

    fun getAppUser(context: Context): AppUser? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)

        if (isLoggedIn) {
            val uid = prefs.getString(KEY_UID, "guest_admin") ?: "guest_admin"
            val name = prefs.getString(KEY_NAME, "Operator Toko") ?: "Operator Toko"
            val phone = prefs.getString(KEY_PHONE, "") ?: ""
            val method = prefs.getString(KEY_METHOD, "PHONE") ?: "PHONE"
            return AppUser(
                uid = uid,
                displayName = name,
                phoneNumber = phone.ifBlank { null },
                email = null,
                loginMethod = method
            )
        }

        return null
    }

    fun registerUser(
        context: Context,
        phoneNumber: String,
        ownerName: String,
        pin: String
    ): Result<AppUser> {
        return try {
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            if (pin.length < 4) {
                return Result.failure(IllegalArgumentException("PIN minimal 4 digit"))
            }
            val uid = "phone_${normalizedPhone.filter { it.isDigit() }}"
            val displayName = ownerName.ifBlank { "Pemilik Toko" }

            val prefsUsers = context.getSharedPreferences(PREFS_USERS, Context.MODE_PRIVATE)
            prefsUsers.edit()
                .putString("name_$normalizedPhone", displayName)
                .putString("pin_$normalizedPhone", pin)
                .putString("uid_$normalizedPhone", uid)
                .commit()

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_UID, uid)
                .putString(KEY_NAME, displayName)
                .putString(KEY_PHONE, normalizedPhone)
                .putString(KEY_METHOD, "PHONE")
                .commit()

            Result.success(
                AppUser(
                    uid = uid,
                    displayName = displayName,
                    phoneNumber = normalizedPhone,
                    email = null,
                    loginMethod = "PHONE"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loginWithPhonePin(
        context: Context,
        phoneNumber: String,
        pin: String
    ): Result<AppUser> {
        return try {
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            val prefsUsers = context.getSharedPreferences(PREFS_USERS, Context.MODE_PRIVATE)
            val savedPin = prefsUsers.getString("pin_$normalizedPhone", null)

            if (savedPin == null) {
                return Result.failure(IllegalStateException("Nomor HP belum terdaftar. Silakan daftar akun baru terlebih dahulu."))
            }
            if (savedPin != pin) {
                return Result.failure(IllegalArgumentException("PIN Akses salah. Periksa kembali PIN Anda."))
            }

            val name = prefsUsers.getString("name_$normalizedPhone", "Pemilik Toko") ?: "Pemilik Toko"
            val uid = prefsUsers.getString("uid_$normalizedPhone", "phone_${normalizedPhone.filter { it.isDigit() }}") ?: "phone_${normalizedPhone.filter { it.isDigit() }}"

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_UID, uid)
                .putString(KEY_NAME, name)
                .putString(KEY_PHONE, normalizedPhone)
                .putString(KEY_METHOD, "PHONE")
                .commit()

            Result.success(
                AppUser(
                    uid = uid,
                    displayName = name,
                    phoneNumber = normalizedPhone,
                    email = null,
                    loginMethod = "PHONE"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("Auth", "Error Firebase signOut", e)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val credentialManager = CredentialManager.create(context)
        scope.launch(Dispatchers.IO) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Gagal clear credential state", e)
            } finally {
                launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}
