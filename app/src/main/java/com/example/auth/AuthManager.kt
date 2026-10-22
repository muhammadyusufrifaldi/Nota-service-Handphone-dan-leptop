package com.sesmartcell.notaservice.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class AppUser(
    val uid: String,
    val displayName: String,
    val phoneNumber: String?,
    val email: String?,
    val loginMethod: String // "GOOGLE", "PHONE"
)

object AuthManager {

    private const val PREFS_NAME = "se_smart_cell_auth_prefs"
    private const val KEY_LOGGED_IN = "key_is_logged_in"
    private const val KEY_UID = "key_user_uid"
    private const val KEY_NAME = "key_user_name"
    private const val KEY_PHONE = "key_user_phone"
    private const val KEY_METHOD = "key_login_method"
    private const val KEY_OTP = "key_whatsapp_otp"
    private const val KEY_OTP_EXPIRES_AT = "key_whatsapp_otp_expires_at"

    const val FALLBACK_WEB_CLIENT_ID = "242770648535-ra83pvtinim0562mi93ka0v5rq8513ni.apps.googleusercontent.com"

    val auth: FirebaseAuth
        get() = Firebase.auth

    val currentUser: FirebaseUser?
        get() = try {
            Firebase.auth.currentUser
        } catch (e: Exception) {
            null
        }

    fun getServerClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val str = context.getString(resId)
                if (str.isNotBlank()) str else FALLBACK_WEB_CLIENT_ID
            } else {
                FALLBACK_WEB_CLIENT_ID
            }
        } catch (e: Exception) {
            FALLBACK_WEB_CLIENT_ID
        }
    }

    fun normalizePhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        require(digits.length >= 8) { "Nomor handphone tidak valid" }
        return if (digits.startsWith("0")) "+62${digits.substring(1)}" else digits
    }

    fun generateOtp(): String {
        val code = (100000..999999).random()
        return code.toString()
    }

    fun saveWhatsAppOtp(context: Context, otp: String, validForMinutes: Int = 5) {
        val expiresAt = System.currentTimeMillis() + (validForMinutes * 60_000L)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_OTP, otp)
            .putLong(KEY_OTP_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun getSavedWhatsAppOtp(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expiresAt = prefs.getLong(KEY_OTP_EXPIRES_AT, 0L)
        if (expiresAt > 0L && System.currentTimeMillis() > expiresAt) {
            prefs.edit().remove(KEY_OTP).remove(KEY_OTP_EXPIRES_AT).apply()
            return null
        }
        return prefs.getString(KEY_OTP, null)
    }

    fun getAppUser(context: Context): AppUser? {
        val fbUser = currentUser
        if (fbUser != null) {
            return AppUser(
                uid = fbUser.uid,
                displayName = fbUser.displayName ?: fbUser.email ?: "Pengguna Google",
                phoneNumber = fbUser.phoneNumber,
                email = fbUser.email,
                loginMethod = "GOOGLE"
            )
        }

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

    fun loginWithPhone(
        context: Context,
        phoneNumber: String,
        ownerName: String,
        pin: String
    ): AppUser {
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        val uid = "phone_${normalizedPhone.filter { it.isDigit() }}"
        val displayName = ownerName.ifBlank { "Pemilik SE SMART CELL" }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_UID, uid)
            .putString(KEY_NAME, displayName)
            .putString(KEY_PHONE, normalizedPhone)
            .putString(KEY_METHOD, "PHONE")
            .apply()

        if (pin.isNotBlank()) {
            prefs.edit().putString("key_user_pin", pin).apply()
        }

        return AppUser(
            uid = uid,
            displayName = displayName,
            phoneNumber = normalizedPhone,
            email = null,
            loginMethod = "PHONE"
        )
    }

    fun openWhatsAppVerification(context: Context, phoneNumber: String, ownerName: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        require(digits.length >= 8) { "Nomor handphone tidak valid" }

        val otp = generateOtp()
        saveWhatsAppOtp(context, otp)

        val waNumber = if (digits.startsWith("0")) "62${digits.substring(1)}" else digits
        val displayName = ownerName.ifBlank { "Pemilik Toko" }
        val message = "Halo, saya $displayName. Saya ingin verifikasi login aplikasi SE SMART CELL. OTP verifikasi Anda adalah: $otp. Mohon balas konfirmasi agar saya bisa masuk ke aplikasi."
        val encodedText = Uri.encode(message)

        val waUri = Uri.parse("https://wa.me/$waNumber?text=$encodedText")
        val intent = Intent(Intent.ACTION_VIEW, waUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("https://api.whatsapp.com/send?phone=$waNumber&text=$encodedText")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }

        return otp
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val credentialManager = CredentialManager.create(activity)
        val serverClientId = getServerClientId(activity)

        if (serverClientId.isBlank()) {
            onError("Google Sign-In belum dikonfigurasi. Periksa client ID Firebase di strings.xml.")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                val googleIdToken = when {
                    credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    }
                    else -> null
                }

                if (googleIdToken.isNullOrBlank()) {
                    onError("Google Sign-In gagal karena kredensial tidak valid. Coba buka aplikasi kembali.")
                    return@launch
                }

                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val userCredential = auth.signInWithCredential(authCredential).await()
                val user = userCredential.user
                if (user != null) {
                    val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean(KEY_LOGGED_IN, true)
                        .putString(KEY_UID, user.uid)
                        .putString(KEY_NAME, user.displayName ?: user.email ?: "Pengguna Google")
                        .putString(KEY_PHONE, user.phoneNumber ?: "")
                        .putString(KEY_METHOD, "GOOGLE")
                        .apply()
                    onSuccess()
                } else {
                    onError("Login Google berhasil tetapi akun tidak terbaca.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In dibatalkan: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Gagal login dengan Google", e)
                onError(e.localizedMessage ?: "Login Google gagal. Pastikan akun Google aktif dan perangkat terhubung ke internet.")
            }
        }
    }

    fun attemptSilentSignIn(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        if (currentUser != null || getAppUser(context) != null) {
            onSuccess()
            return
        }

        val serverClientId = getServerClientId(context)
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
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
