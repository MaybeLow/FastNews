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

/**
 * Activity that shows the list of people, that the current user is searching for.
 * The activity is called from the friend list activity
 */
class SearchedFriendsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searched_friends)

        // Get the values from the intent. The intent contains the user ids of profiles
        // with the searched username
        val searchedFriends = intent.getStringArrayExtra("searchedUsers")
        val searchedProfiles = mutableListOf<Profile>()
        if (searchedFriends != null) {
            for (searchedFriend in searchedFriends) {
                searchedProfiles.add(ProfileDataRequestManager(searchedFriend).requestData())
            }
        }
        inflateSearchedFriends(searchedProfiles)
    }

    /**
     * Inflate the recycler view with the searched profiles
     */
    private fun inflateSearchedFriends(friendProfiles: MutableList<Profile>) {
        val recyclerView = findViewById<View>(R.id.searched_friends_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = FriendsAdapter(friendProfiles)
        recyclerView.adapter = mAdapter
    }
}
