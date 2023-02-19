package com.example.newsapp.activities

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity that allows the user to log in, register or continue as a guest
 */
class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    // Intent that sends the user onto the register screen
    // where the user can create a new account
    private val startRegisterForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Start the activity where the user can select preferences
                startChipsActivity()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // Restart this activity
                onRestart()
            }
        }
    // Intent that sends the user onto the login screen
    // where the user can sign in if he is already registered
    private val startLoginForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                startMainActivity()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // Restart this activity
                onRestart()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize activity buttons
        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            intent = Intent(this, SigninActivity::class.java)
            startLoginForResult.launch(intent)
        }

        val signupButton: Button = findViewById(R.id.signup_button)
        signupButton.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startRegisterForResult.launch(intent)
        }

        val guest: Button = findViewById(R.id.guest_button)
        guest.setOnClickListener {
            startGuestActivity()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Send the logged in user to the main activity
            startMainActivity()
        }
    }

    /**
     * Method that starts the main activity
     */
    private fun startMainActivity() {
        intent = Intent(this, MainActivity::class.java)
        Log.w(ContentValues.TAG, "main activity started")
        startActivity(intent)
        finish()
    }

    /**
     * Method that starts a series of chips activities where user can choose
     * news preferences
     */
    private fun startChipsActivity() {
        intent = Intent(this, ChipsCategoriesActivity::class.java)
        Log.w(ContentValues.TAG, "ChipsCategoriesActivity started")
        startActivity(intent)
        finish()
    }

    private fun startGuestActivity() {
        intent = Intent(this, GuestActivity::class.java)
        Log.w(ContentValues.TAG, "GuestActivity started")
        startActivity(intent)
        finish()
    }
}
