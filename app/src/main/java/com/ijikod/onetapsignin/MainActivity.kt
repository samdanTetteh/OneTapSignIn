package com.ijikod.onetapsignin

import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

class MainActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInClient: BeginSignInRequest

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
                        it.pendingIntent.intentSender, 1, null, 0,
                        0, 0, null
                    )
                }catch (e: IntentSender.SendIntentException){
                    Log.e("OnTap", "Couldn't start One Tap UI: ${e.localizedMessage}"))
                }

            }

            .addOnFailureListener {
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage)
            }
    }
}