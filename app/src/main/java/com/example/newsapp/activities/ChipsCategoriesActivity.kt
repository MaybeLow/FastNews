package com.example.newsapp.activities

import android.content.Intent
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
 * Activity that allows users to choose news topic preferences
 */
class ChipsCategoriesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chips_categories)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Firestore
        db = Firebase.firestore

        // Call coroutine for creating and filling the chips
        val chipGroup = findViewById<ChipGroup>(R.id.categories_list)
        GlobalScope.launch {
            createChips(chipGroup)
        }

        val applyButton: Button = findViewById(R.id.button_apply_categories)
        applyButton.setOnClickListener {
            val ids = chipGroup.checkedChipIds
            if (ids.size <= 5) {
                val categories = mutableListOf<String>()

                // Create individual chips
                for (id in ids) {
                    val chip = chipGroup.findViewById<View>(id) as Chip
                    val category = chip.text.toString()
                    categories.add(category)
                }

                // Add preferences to the database
                db.collection("user")
                    .document(auth.currentUser!!.uid)
                    .update("categories", categories)

                // Clear the notification service, as new preferences appear
                clearService(db)

                // Start the next preferences activity
                intent = Intent(this, ChipsCountriesActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Method that creates chips and fills them with topic names
     */
    private suspend fun createChips(chipGroup: ChipGroup) {
        // Call the database to get the selected preferences
        val database = db.collection("user").document(auth.currentUser!!.uid).get()
        database.await()

        runOnUiThread {
            val chosenCategories = database.result.data!!["categories"] as MutableList<String>

            // Select the chips of topics that are already chosen by the user
            val categories = resources.getStringArray(R.array.categories)
            for (category in categories) {
                val chip = Chip(this)
                chip.text = category
                chip.textSize = 20F
                chip.isCheckable = true
                if (chosenCategories.contains(category)) {
                    chip.isChecked = true
                }
                chipGroup.addView(chip)
            }
        }
    }

    /**
     * Clear the service that displays the notifications to the user
     * and clear the notification preferences
     */
    private fun clearService(db: FirebaseFirestore) {
        db.collection("user")
            .document(auth.currentUser!!.uid)
            .update("notifications", mutableListOf<String>())

        db.collection("user")
            .document(auth.currentUser!!.uid)
            .update("beNotified", false)
    }
}
