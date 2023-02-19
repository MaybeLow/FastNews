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
 * Activity that allows users to choose news countries preferences
 */
class ChipsCountriesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chips_countries)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Firestore
        db = Firebase.firestore

        // Call coroutine for creating and filling the chips
        val chipGroup = findViewById<ChipGroup>(R.id.countries_list)
        GlobalScope.launch {
            createChips(chipGroup)
        }

        val applyButton: Button = findViewById(R.id.button_apply_countries)
        applyButton.setOnClickListener {
            val ids = chipGroup.checkedChipIds
            if (ids.size <= 5) {
                val countries = mutableListOf<String>()

                // Create individual chips
                for (id in ids) {
                    val chip = chipGroup.findViewById<View>(id) as Chip
                    val country = chip.text.toString()
                    countries.add(country)
                }

                // Add preferences to the database
                db.collection("user")
                    .document(auth.currentUser!!.uid)
                    .update("countries", mapToCodes(countries))

                // Start the next preferences activity
                intent = Intent(this, ChipsLanguagesActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Method that creates chips and fills them with country names
     */
    private suspend fun createChips(chipGroup: ChipGroup) {
        // Call the database to get the selected preferences
        val database = db.collection("user").document(auth.currentUser!!.uid).get()
        database.await()

        runOnUiThread {
            val chosenCountries = database.result.data!!["countries"] as MutableList<String>

            // Select the chips of countries that are already chosen by the user
            val countries = resources.getStringArray(R.array.countries)
            val countryIds = resources.getStringArray(R.array.country_ids)
            for (country in countries) {
                val chip = Chip(this)
                chip.text = country
                chip.textSize = 20F
                chip.isCheckable = true
                if (chosenCountries.contains(countryIds[countries.indexOf(country)])) {
                    chip.isChecked = true
                }
                chipGroup.addView(chip)
            }
        }
    }

    /**
     * Method that maps the name of the countries to country codes
     * to be stored in the database
     */
    private fun mapToCodes(countries: MutableList<String>): MutableList<String> {
        val countryCodes = mutableListOf<String>()
        val countryIds = resources.getStringArray(R.array.country_ids)
        val countriesNames = resources.getStringArray(R.array.countries)
        for (country in countries) {
            val countryId = countryIds[countriesNames.indexOf(country)]
            countryCodes.add(countryId)
        }
        return countryCodes
    }
}
