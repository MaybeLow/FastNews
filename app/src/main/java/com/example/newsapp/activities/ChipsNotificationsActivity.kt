package com.example.newsapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity that allows users to choose which topics he wants to receive notifications for
 */
class ChipsNotificationsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chips_notifications)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Firestore
        db = Firebase.firestore

        val chipGroup = findViewById<ChipGroup>(R.id.notification_list)

        // Call coroutine for creating and filling the chips
        GlobalScope.launch {
            createChips(chipGroup)
        }

        val applyButton: Button = findViewById(R.id.button_apply_notifications)
        applyButton.setOnClickListener {

            val notifications = mutableListOf<String>()
            val ids = chipGroup.checkedChipIds

            // Create individual chips
            for (id in ids) {
                val chip = chipGroup.findViewById<View>(id) as Chip
                val notification = chip.text.toString()
                notifications.add(notification)
            }

            // Add preferences to the database
            db.collection("user")
                .document(auth.currentUser!!.uid)
                .update("notifications", notifications)

            // Start the notification service
            startService()

            finish()
        }

        // Button that removes the notifications entirely
        val cancelButton: Button = findViewById(R.id.button_stop_notifications)
        cancelButton.setOnClickListener {
            stopNotifications()
            finish()
        }
    }

    /**
     * Method that creates chips and fills them with category names
     */
    private suspend fun createChips(chipGroup: ChipGroup) {
        // Call the database to get the selected notification preferences
        val database = db.collection("user").document(auth.currentUser!!.uid).get()
        database.await()

        runOnUiThread {
            val categories = database.result.data!!["categories"] as MutableList<String>

            for (category in categories) {
                val chip = Chip(this)
                chip.text = category
                chip.textSize = 20F
                chip.isCheckable = true
                chipGroup.addView(chip)
            }
        }
    }


    /**
     * Start the notification service, that will run in the background
     * and display new news article every hour
     */
    private fun startService() {
        db.collection("user")
            .document(auth.currentUser!!.uid)
            .update("beNotified", true)
    }

    /**
     * Stop receiving notifications
     */
    private fun stopNotifications() {
        db.collection("user")
            .document(auth.currentUser!!.uid)
            .update("beNotified", false)
    }
}
