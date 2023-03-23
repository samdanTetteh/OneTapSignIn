package com.ijikod.onetapsignin

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

class MainActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInClient: BeginSignInRequest

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpOnTapSignInClient()
        displayOneTapSignInUI()

    }


    private fun setUpOnTapSignInClient() {
        oneTapClient = Identity.getSignInClient(this)
        signInClient = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build())
            .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                // Your server's client ID, not your Android client ID.
                .setServerClientId(getString(R.string.client_id))
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(true)
                .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
    }


    private fun displayOneTapSignInUI(){
        oneTapClient.beginSignIn(signInClient)
            .addOnSuccessListener {
                try {
                    startIntentSenderForResult(
                        it.pendingIntent.intentSender, REQ_ONE_TAP, null, 0,
                        0, 0, null
                    )
                }catch (e: IntentSender.SendIntentException){
                    Log.e("ONE TAP", "Couldn't start One Tap UI: ${e.localizedMessage}"))
                }

            }

            .addOnFailureListener {
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                it.localizedMessage?.let { errorMsg ->
                    Log.d("ONE TAP", errorMsg)
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d("ONE TAP", "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d("ONE TAP", "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d("ONE TAP", "No ID token or password!")
                        }
                    }
                } catch (e : ApiException) {
                    when(e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d("ONE TAP", "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }

                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d("ONE TAP", "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }

                        else -> {
                            Log.d("ONE_TAP", "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }                    }

                }
            }
        }
    }
}