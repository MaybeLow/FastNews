package com.example.newsapp.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.managers.NewsDataRequestManager
import com.example.newsapp.recycleadapters.NewsAdapter

/**
 * Activity that allows the users to search for news articles, based on specific key words
 */
class SearchingNewsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searching_news)

        val searchInput = intent.getStringExtra("searchInput")

        val drm = NewsDataRequestManager()
        // Call the news data request manager with the parameters, that removes bias from the search,
        // besides the language of the news and with the keywords
        val headlines = drm.requestData(keywords = searchInput?.split(" ") as MutableList<String>,
            forCountry = false,
            forCategory = false)

        // Inflate the recycler view with the searched articles
        val recyclerView = findViewById<View>(R.id.search_result_list) as RecyclerView // Bind to the recyclerview in the layout

        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = NewsAdapter(headlines)
        recyclerView.adapter = mAdapter
    }
}
