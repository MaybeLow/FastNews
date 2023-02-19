package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.ApiData
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.recycleadapters.NewsAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * A fragment that shows the news articles, being read by the current users' friends
 */
class FriendsFragment: Fragment(R.layout.fragment_friends) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendlist: MutableList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_headlines, container, false)
        recyclerView = view.findViewById<View>(R.id.recycler_view) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val profile = ProfileDataRequestManager().requestData()
        friendlist = profile.friends

        if (friendlist.isNotEmpty()) {
            GlobalScope.launch {
                getFriendsHeadlines()
            }
        }

        return view
    }

    /**
     * Get the news headlines, the current users' friends are interested in
     */
    private suspend fun getFriendsHeadlines() {
        val db = Firebase.firestore

        var friendsCategories = mutableListOf<String>()
        var friendsCountries = mutableListOf<String>()

        for (friend in friendlist) {
            val database = db.collection("user").document(friend).get()
            database.await()

            val categories = database.result.data!!["categories"] as MutableList<String>
            val countries = database.result.data!!["countries"] as MutableList<String>

            // Get the list of all friends' preferences
            for (category in categories) {
                friendsCategories.add(category)
            }
            for (country in countries) {
                friendsCountries.add(country)
            }
        }

        // Select distinct preferences from the list
        friendsCategories = friendsCategories.distinct() as MutableList<String>
        friendsCountries = friendsCountries.distinct() as MutableList<String>

        // Shuffle the lists from randomisation. This is needed, due to
        // limitations from the news api being used for this app,
        // that only allows us to submit 5 parameters at a time
        friendsCategories.shuffle()
        friendsCountries.shuffle()

        // Take the first 5 preferences from the randomised list of
        // friends' preferences
        friendsCategories = friendsCategories.take(5) as MutableList<String>
        friendsCountries = friendsCountries.take(5) as MutableList<String>

        var categories = ""
        if (friendsCategories.isNotEmpty()) {
            categories = "&category=" + friendsCategories.joinToString(separator = ",")
        }
        var countries = ""
        if (friendsCountries.isNotEmpty()) {
            countries = "&country=" + friendsCountries.joinToString(separator = ",")
        }

        requestNewsData(categories, countries)
    }

    /**
     * Call the api with the provided friends' preferences data
     */
    private fun requestNewsData(categories: String, countries: String) {
        val url = URL("https://newsdata.io/api/1/news?apikey=pub_14178cd08bf1a5c4fcf7d03e3c2f3be0f78fc$categories$countries")
        val connection = url.openConnection() as HttpsURLConnection

        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val apiData = Gson().fromJson(inputStreamReader, ApiData::class.java)
            inputStreamReader.close()
            inputStream.close()

            val mAdapter = NewsAdapter(apiData!!.results)
            val currentActivity = activity as AppCompatActivity
            currentActivity.runOnUiThread {
                recyclerView.adapter = mAdapter
            }
        } else {
            println(connection.responseCode)
        }
    }

    override fun onResume() {
        if (friendlist.isNotEmpty()) {
            GlobalScope.launch {
                getFriendsHeadlines()
            }
        }
        super.onResume()
    }
}
