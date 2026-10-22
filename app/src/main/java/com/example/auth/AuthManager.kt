package com.sesmartcell.notaservice.auth

import android.app.Activity
import android.content.Context
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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
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
    val loginMethod: String // "GOOGLE", "GUEST", "PHONE"
)

object AuthManager {

    private const val PREFS_NAME = "se_smart_cell_auth_prefs"
    private const val KEY_LOGGED_IN = "key_is_logged_in"
    private const val KEY_UID = "key_user_uid"
    private const val KEY_NAME = "key_user_name"
    private const val KEY_PHONE = "key_user_phone"
    private const val KEY_METHOD = "key_login_method"

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
            val name = prefs.getString(KEY_NAME, "Operator Toko (Mode Cepat)") ?: "Operator Toko (Mode Cepat)"
            val phone = prefs.getString(KEY_PHONE, "") ?: ""
            val method = prefs.getString(KEY_METHOD, "GUEST") ?: "GUEST"
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
        val normalizedPhone = phoneNumber.trim()
        if (normalizedPhone.length < 8) {
            throw IllegalArgumentException("Nomor handphone tidak valid")
        }

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

    fun loginAsGuest(context: Context): AppUser {
        val uid = "guest_admin"
        val name = "Operator Toko (Mode Cepat)"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_UID, uid)
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, "")
            .putString(KEY_METHOD, "GUEST")
            .apply()

        return AppUser(
            uid = uid,
            displayName = name,
            phoneNumber = null,
            email = null,
            loginMethod = "GUEST"
        )
    }

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken?) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanPhoneNumber = phoneNumber.trim()
        if (cleanPhoneNumber.length < 8) {
            onError("Nomor handphone tidak valid")
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(cleanPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, token)
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.localizedMessage ?: "Gagal mengirim OTP ke nomor handphone")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneOtpAndSignIn(
        verificationId: String,
        smsCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (verificationId.isBlank() || smsCode.isBlank()) {
            onError("Kode OTP tidak valid")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onSuccess()
                    } else {
                        onError("Verifikasi OTP berhasil, tetapi akun tidak ditemukan.")
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Verifikasi OTP gagal")
                }
            }
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

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
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
                } else {
                    onError("Tipe kredensial tidak sesuai untuk Google Sign-In.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In dibatalkan: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Gagal login dengan Google", e)
                onError(e.localizedMessage ?: "Login Google gagal. Pastikan akun Google sudah aktif dan perangkat terhubung ke internet.")
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
