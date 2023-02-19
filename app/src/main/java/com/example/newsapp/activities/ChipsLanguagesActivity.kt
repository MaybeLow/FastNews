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
 * Activity that allows users to choose news language preferences
 */
class ChipsLanguagesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chips_languages)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Firestore
        db = Firebase.firestore

        // Call coroutine for creating and filling the chips
        val chipGroup = findViewById<ChipGroup>(R.id.languages_list)
        GlobalScope.launch {
            createChips(chipGroup)
        }

        val applyButton: Button = findViewById(R.id.button_apply_languages)
        applyButton.setOnClickListener {
            val ids = chipGroup.checkedChipIds
            if (ids.size <= 5) {
                val languages = mutableListOf<String>()

                // Create individual chips
                for (id in ids) {
                    val chip = chipGroup.findViewById<View>(id) as Chip
                    val language = chip.text.toString()
                    languages.add(language)
                }

                // Add preferences to the database
                db.collection("user")
                    .document(auth.currentUser!!.uid)
                    .update("languages", mapToCodes(languages))

                // Start the main activity
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Method that creates chips and fills them with language names
     */
    private suspend fun createChips(chipGroup: ChipGroup) {
        // Call the database to get the selected preferences
        val database = db.collection("user").document(auth.currentUser!!.uid).get()
        database.await()

        runOnUiThread {
            val chosenLanguages = database.result.data!!["languages"] as MutableList<String>

            // Select the chips of languages that are already chosen by the user
            val languages = resources.getStringArray(R.array.languages)
            val languageIds = resources.getStringArray(R.array.language_ids)
            for (language in languages) {
                val chip = Chip(this)
                chip.text = language
                chip.textSize = 20F
                chip.isCheckable = true
                if (chosenLanguages.contains(languageIds[languages.indexOf(language)])) {
                    chip.isChecked = true
                }
                chipGroup.addView(chip)
            }
        }
    }

    /**
     * Method that maps the name of the languages to language codes
     * to be stored in the database
     */
    private fun mapToCodes(languages: MutableList<String>): MutableList<String> {
        val languageCodes = mutableListOf<String>()
        val languageIds = resources.getStringArray(R.array.language_ids)
        val languagesNames = resources.getStringArray(R.array.languages)
        for (language in languages) {
            val languageId = languageIds[languagesNames.indexOf(language)]
            languageCodes.add(languageId)
        }
        return languageCodes
    }
}
