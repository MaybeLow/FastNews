package com.example.newsapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.data.Profile
import com.example.newsapp.recycleadapters.FriendsAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity that displays the list of friends of the user in a recycler view
 */
class FriendlistActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendlist)

        inflateFriends()

        val searchButton = findViewById<Button>(R.id.news_search_button)
        searchButton.setOnClickListener {
            addSearch()
        }
    }

    /**
     * Add the friend cards into the list of friends in a recycler view
     */
    private fun inflateFriends() {
        val userId: String = intent.getStringExtra("userId").toString()

        val profile = ProfileDataRequestManager(userId).requestData()
        val friendlist = profile.friends

        val friendProfiles: MutableList<Profile> = mutableListOf()
        for (friendId in friendlist) {
            val friendProfile = ProfileDataRequestManager(friendId).requestData()
            friendProfiles.add(friendProfile)
        }

        // Add friend cards to the recycler view
        val recyclerView = findViewById<View>(R.id.friend_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = FriendsAdapter(friendProfiles)
        recyclerView.adapter = mAdapter
    }

    /**
     * Method that allows the user to search for new friends
     */
    private fun addSearch() {
        GlobalScope.launch {
            val response = async {friendSearch()}
            response.await()
            response.join()
        }
    }

    /**
     * The user can type a username and get the list of cards of all matching people
     */
    private suspend fun friendSearch() {
        val db = Firebase.firestore
        val searcher = findViewById<TextView>(R.id.search_friend)
        val input = searcher.text.toString()

        // Get all user profile information from the database based on the given name
        val users = mutableListOf<String>()
        val databaseDocuments = db.collection("user").get()
        databaseDocuments.await()

        val userDocuments = databaseDocuments.result.documents
        for (userDocument in userDocuments) {
            val username = userDocument.data!!["username"] as String
            val userId = userDocument.data!!["user_id"] as String
            println("Searching username $username")
            println("Searching userId $userId")
            println("Input username $input")

            println("$input $username " + (input == username))
            if (input == username) {
                users.add(userId)
            }
        }
        println("List of all user ids $users")

        // Start a new activity that displays the searched friends
        val intent = Intent(this, SearchedFriendsActivity::class.java)
        intent.putExtra("searchedUsers", users.toTypedArray())

        startActivity(intent)
    }

    /**
     * Update the list of friends when the user resumes this activity
     */
    override fun onResume() {
        inflateFriends()
        super.onResume()
    }
}
