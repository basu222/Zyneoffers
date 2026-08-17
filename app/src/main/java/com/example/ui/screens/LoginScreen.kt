package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.ui.components.ZyneLogoMark
import com.example.ui.theme.ZyneBackground
import com.example.ui.theme.ZyneBlue
import com.example.ui.theme.ZyneBorder
import com.example.ui.theme.ZyneCard
import com.example.ui.theme.ZyneRed
import com.example.ui.theme.ZyneRedBg
import com.example.ui.theme.ZyneRedBorder
import com.example.ui.theme.ZyneTextMuted
import com.example.ui.theme.ZyneTextPrimary
import com.example.ui.theme.ZyneTextSecondary
import com.example.ui.theme.ZyneTextSubtle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

private const val TAG = "ZyneLoginScreen"
private const val FALLBACK_WEB_CLIENT_ID = "939097504919-s6g2a047ommtu8tas6n9h21o5acn8oa5.apps.googleusercontent.com"

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onLoginWithParams: (email: String, name: String, photo: String, deviceId: String, refCode: String?, idToken: String?) -> Unit,
    errorMessage: String?,
    isLoading: Boolean = false,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "DEVICE_${System.currentTimeMillis()}"
    }

    var referralCodeInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var isSigningInLocally by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ZyneBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // BRAND HERO SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ZyneLogoMark(
                    size = 64.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Zyne Offers",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZyneTextPrimary,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Earn rewards by completing verified offers.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ZyneTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // PRIMARY SIGN-IN CARD - GOOGLE SIGN-IN ONLY
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZyneCard),
                border = BorderStroke(1.dp, ZyneBorder)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isBusy = isLoading || isSigningInLocally

                    // Primary Google Sign-In Action Button
                    Button(
                        onClick = {
                            if (isBusy) return@Button
                            localError = null
                            onClearError()
                            isSigningInLocally = true

                            coroutineScope.launch {
                                val webClientId = try {
                                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                                    if (resId != 0) context.getString(resId) else FALLBACK_WEB_CLIENT_ID
                                } catch (e: Exception) {
                                    FALLBACK_WEB_CLIENT_ID
                                }

                                Log.d(TAG, "Initiating Google Sign-In with Web Client ID: $webClientId")
                                val activity = context.findActivity() ?: (context as? Activity)
                                val callContext: Context = activity ?: context
                                val credentialManager = CredentialManager.create(callContext)

                                fun handleCredentialResult(credential: Credential) {
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        try {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val email = googleIdTokenCredential.id
                                            val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                                            val photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                                            val idToken = googleIdTokenCredential.idToken

                                            Log.d(TAG, "Google Sign-In successful for email: $email")
                                            onLoginWithParams(
                                                email,
                                                displayName,
                                                photoUrl,
                                                deviceId,
                                                referralCodeInput.ifBlank { null },
                                                idToken
                                            )
                                        } catch (e: GoogleIdTokenParsingException) {
                                            Log.e(TAG, "Error parsing Google credential: ${e.message}", e)
                                            localError = "Failed to parse Google credentials: ${e.message}"
                                        }
                                    } else {
                                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                                        localError = "Unexpected credential received. Please try again."
                                    }
                                }

                                fun handleException(e: Throwable) {
                                    when (e) {
                                        is GetCredentialCancellationException -> {
                                            Log.i(TAG, "User dismissed Google Sign-In sheet")
                                        }
                                        is NoCredentialException -> {
                                            Log.w(TAG, "NoCredentialException: No Google accounts found on device", e)
                                            localError = "No Google accounts found. Please add a Google account to your device."
                                        }
                                        is SecurityException -> {
                                            Log.w(TAG, "SecurityException connecting to Google Play Services: ${e.message}", e)
                                            localError = "Google Play Services error. Please verify Google Play Services is enabled."
                                        }
                                        is GetCredentialCustomException -> {
                                            Log.e(TAG, "GetCredentialCustomException: ${e.type} - ${e.message}", e)
                                            localError = "Google Sign-In: ${e.message ?: e.type}"
                                        }
                                        is GetCredentialException -> {
                                            Log.e(TAG, "GetCredentialException: ${e.message}", e)
                                            localError = "Google Sign-In: ${e.message ?: "Authentication failed. Please try again."}"
                                        }
                                        else -> {
                                            Log.e(TAG, "Unexpected error in Google Sign-In: ${e.message}", e)
                                            localError = "Authentication error: ${e.localizedMessage ?: e.message ?: "Please try again"}"
                                        }
                                    }
                                }

                                try {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .setAutoSelectEnabled(false)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(callContext, request)
                                    handleCredentialResult(result.credential)
                                } catch (e: NoCredentialException) {
                                    Log.w(TAG, "NoCredentialException caught. Attempting GetSignInWithGoogleOption fallback...", e)
                                    try {
                                        val fallbackOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
                                        val fallbackRequest = GetCredentialRequest.Builder()
                                            .addCredentialOption(fallbackOption)
                                            .build()
                                        val fallbackResult = credentialManager.getCredential(callContext, fallbackRequest)
                                        handleCredentialResult(fallbackResult.credential)
                                    } catch (fallbackEx: Throwable) {
                                        handleException(fallbackEx)
                                    }
                                } catch (e: Throwable) {
                                    handleException(e)
                                } finally {
                                    isSigningInLocally = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZyneBlue,
                            contentColor = Color.White
                        ),
                        enabled = !isBusy
                    ) {
                        if (isBusy) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connecting with Google...",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(22.dp),
                                    shape = CircleShape,
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF4285F4),
                                                fontSize = 13.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Optional Referral Code Input Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Optional: Referral Code",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ZyneTextMuted,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = referralCodeInput,
                            onValueChange = { referralCodeInput = it.uppercase() },
                            placeholder = { Text("Enter referral code", color = ZyneTextSubtle, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZyneBlue,
                                unfocusedBorderColor = ZyneBorder,
                                focusedTextColor = ZyneTextPrimary,
                                unfocusedTextColor = ZyneTextPrimary,
                                focusedContainerColor = ZyneBackground,
                                unfocusedContainerColor = ZyneBackground
                            )
                        )
                    }

                    // Active Error Feedback Banner
                    val activeError = errorMessage ?: localError
                    if (activeError != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = ZyneRedBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ZyneRedBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = ZyneRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ZyneRed,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trust & Features Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeaturePill(icon = Icons.Default.AccountBalanceWallet, text = "Instant UPI")
                FeaturePill(icon = Icons.Default.CheckCircle, text = "Verified Tasks")
                FeaturePill(icon = Icons.Default.Shield, text = "Secure")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms & Privacy Policy Notice
            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ZyneTextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZyneBlue,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = ZyneTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

