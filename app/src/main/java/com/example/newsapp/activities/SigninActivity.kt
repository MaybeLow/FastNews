package com.example.newsapp.activities

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity, that allows the user to sign in, in case he already has an account
 */
class SigninActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Button, that compares the provided info with the info, stored in Firebase
        val registerButton: Button = findViewById(R.id.signin_button)
        registerButton.setOnClickListener {
            val (name, password) = returnNameAndPassword()
            if (name != "" && password != "") {
                signInUser(name, password)
            }
        }
    }

    /**
     * Get the name and the password, provided by the user
     */
    private fun returnNameAndPassword(): Pair<String, String> {
        val name = findViewById<View>(R.id.email_input) as TextView
        val password = findViewById<View>(R.id.password_input) as TextView
        return Pair(name.text.toString(), password.text.toString())
    }

    /**
     * Sign in the user if the email and password are correct
     */
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInUserWithEmail:success")
                    Toast.makeText(baseContext, "SignIn succeeded.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "signInUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "SignIn failed.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    /**
     * Send the authentication result back to the caller activity
     */
    override fun finish() {
        if (auth.currentUser != null) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        super.finish()
    }
}
