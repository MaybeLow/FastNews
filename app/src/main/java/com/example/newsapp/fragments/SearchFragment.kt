package com.example.newsapp.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activities.SearchingNewsActivity
import com.example.newsapp.recycleadapters.SearchAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Fragment that allows the user to see his search history, as well as search for news articles
 * based on keywords he types
 */
class SearchFragment: Fragment(R.layout.fragment_search) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        inflateSearchHistory(view)
        createSearch(view)

        return view
    }

    /**
     * Start the coroutine that takes the search history from the Firebase firestore database
     */
    private fun inflateSearchHistory(view: View) {
        GlobalScope.launch {
            fetchSearchHistory(view)
        }
    }

    /**
     * Get the search data from the Firebase firestore database
     */
    private suspend fun fetchSearchHistory(view: View) {
        val db = Firebase.firestore
        val auth = Firebase.auth

        // Call the database and wait for result
        val userDocument = db.collection("user").document(auth.currentUser!!.uid)
        val userData = userDocument.get()
        userData.await()

        val searchHistory = userData.result.data!!["search_history"] as MutableList<String>

        val thisActivity = activity as AppCompatActivity
        thisActivity.runOnUiThread {
            val recyclerView = view.findViewById<View>(R.id.chat_recycler) as RecyclerView // Bind to the recyclerview in the layout
            val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
            recyclerView.layoutManager = layoutManager
            searchHistory.reverse()
            val mAdapter = SearchAdapter(searchHistory) // Use db to fetch search history
            recyclerView.adapter = mAdapter
        }
    }

    /**
     * Create search function inside the fragment
     */
    private fun createSearch(view: View) {
        val searchButton = view.findViewById(R.id.news_search_button) as Button

        searchButton.setOnClickListener {
            val searchText = view.findViewById(R.id.news_search) as TextView
            val searchInput = searchText.text.toString()
            if (searchInput != "") {
                updateSearchHistory(searchInput, view)

                val intent = Intent(view.context, SearchingNewsActivity::class.java)
                intent.putExtra("searchInput", searchInput)

                startActivity(intent)
            }
        }
    }

    /**
     * Update the search history whenever the user types a new keyword
     */
    private fun updateSearchHistory(searchInput: String, view: View) {
        GlobalScope.launch {
            accessAndUpdateSearchHistory(searchInput, view)
        }
    }

    /**
     * Take the list of searched keywords and update it
     */
    private suspend fun accessAndUpdateSearchHistory(searchInput: String, view: View) {
        val db = Firebase.firestore
        val auth = Firebase.auth

        val userDocument = db.collection("user").document(auth.currentUser!!.uid)
        val userData = userDocument.get()
        userData.await()

        // Take the search history keywords list, add the new keywords and update it
        val searchHistory = userData.result.data!!["search_history"] as MutableList<String>
        searchHistory.add(searchInput)
        userDocument
            .update("search_history", searchHistory)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "Successfully updated the search history: $searchHistory")
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Failed to update the search history: $searchHistory")
            }

        inflateSearchHistory(view)
    }
}
