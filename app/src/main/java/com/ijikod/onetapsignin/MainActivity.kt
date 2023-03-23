package com.ijikod.onetapsignin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

class MainActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInClient: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}