package com.example.newsapp.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.Profile
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.recycleadapters.FriendsAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity that displays friend requests sent to the user
 */
class NotificationsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        GlobalScope.launch {
            getRequestingProfiles()
        }
    }

    /**
     * Get the profile data of the users that sent a friend request
     */
    private suspend fun getRequestingProfiles() {
        val userId = Firebase.auth.currentUser!!.uid
        val db = Firebase.firestore
        val requestingProfiles: MutableList<Profile> = mutableListOf()

        val database = db.collection("user").document(userId).get()
        database.await()

        // Get the list of pending request from the database
        val profiles = database.result.data!!["pending_friends"] as MutableList<String>
        runOnUiThread {
            for (profile in profiles) {
                if (profile != "") {
                    requestingProfiles.add(ProfileDataRequestManager(profile).requestData())
                }
            }
            inflateRequests(requestingProfiles)
        }
    }

    /**
     * Inflate the friend requesting profiles into the recycler view
     */
    private fun inflateRequests(requestingProfiles: MutableList<Profile>) {
        val recyclerView = findViewById<View>(R.id.notifications_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = FriendsAdapter(requestingProfiles)
        recyclerView.adapter = mAdapter
    }

    /**
     * Update the activity when the user comes back after observing pending user profiles
     */
    override fun onResume() {
        GlobalScope.launch {
            getRequestingProfiles()
        }
        super.onResume()
    }
}
