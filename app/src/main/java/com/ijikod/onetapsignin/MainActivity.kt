package com.ijikod.onetapsignin

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInClient: BeginSignInRequest

    private lateinit var signOutButton: Button
    private lateinit var signInButton: Button

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity

    private val viewModel: OneTapSignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signOutButton = findViewById(R.id.sign_out_btn)
        signInButton = findViewById(R.id.sign_in_btn)

        oneTapClient = Identity.getSignInClient(this)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.loggedUserState.collect { loggedInUser ->
                    loggedInUser.isUserLoggedIn?.let { isUserLoggedIn ->
                        when {
                            !isUserLoggedIn -> {
                                showLoginScreen()
                            }

                            else -> {
                                showLoggedInScreen()
                            }
                        }
                    } ?: run { showLoginScreen() }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        oneTapClient.signOut()
    }

    private fun setUpOnTapSignInClient() {
        signInClient = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
    }


    private fun displayOneTapSignInUI() {
        oneTapClient.beginSignIn(signInClient)
            .addOnSuccessListener {
                try {
                    startIntentSenderForResult(
                        it.pendingIntent.intentSender, REQ_ONE_TAP, null, 0,
                        0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("ONE TAP", "Couldn't start One Tap UI: ${e.localizedMessage}")
                    showToastMsg("Couldn't start One Tap UI: ${e.localizedMessage}")
                }

            }

            .addOnFailureListener {
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                it.localizedMessage?.let { errorMsg ->
                    showToastMsg(errorMsg)
                }
            }
    }

    private fun setUpSignIn() {
        setUpOnTapSignInClient()
        displayOneTapSignInUI()
    }


    @Deprecated("Deprecated in Java")
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
                            viewModel.saveUserDetails(credential.id, credential.googleIdToken, true)
                            showLoggedInScreen()
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d("ONE TAP", "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            showToastMsg("No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d("ONE TAP", "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                        }

                        CommonStatusCodes.NETWORK_ERROR -> {
                            // Try again or just ignore.
                            showToastMsg("One-tap encountered a network error.")
                        }

                        else -> {
                            showToastMsg("Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
        }
    }

    private fun showLoggedInScreen() {
        lifecycleScope.launch {
            viewModel.loggedUserState.collect { state ->
                Log.d("ONE TAP", "Got ID token.")
                val successString =
                    String.format(getString(R.string.logged_in_successfully), state.userId)
                findViewById<TextView>(R.id.status_txt).text = successString
                setUpSignOutBtn()
            }
        }
    }

    private fun showToastMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showLoginScreen() {
        findViewById<TextView>(R.id.status_txt).text = getString(R.string.login)
        signInButton.setOnClickListener {
            setUpSignIn()
        }
        signInButton.visibility = View.VISIBLE
        signOutButton.visibility = View.GONE
    }


    private fun setUpSignOutBtn() {
        signInButton.visibility = View.GONE
        signOutButton.visibility = View.VISIBLE
        signOutButton.setOnClickListener {
            viewModel.saveUserDetails(isUserLoggedIn = false)
            showLoginScreen()
            oneTapClient.signOut()
        }
    }
}