package com.example.newsapp.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.ApiData
import com.example.newsapp.recycleadapters.NewsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Activity that gets displayed when a user chooses to continue as a guest
 * or when a user cannot be logged in in the main activity
 */
class GuestActivity: AppCompatActivity() {
    private var apiData: ApiData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest)

        val mToolbar = findViewById<Toolbar>(R.id.guest_toolbar)
        setSupportActionBar(mToolbar)

        GlobalScope.launch {
            requestNewsData()
        }

        // Wait for the coroutine to complete the job
        // or until the counter reaches 7 seconds
        while (apiData == null) {
            Thread.sleep(10)
            Log.w(ContentValues.TAG, "Waiting for API call")
        }

        // Inflate the recycler view if the counter did not exceed the deadline,
        // finish otherwise
        val recyclerView = findViewById<View>(R.id.guest_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = NewsAdapter(apiData!!.results)
        recyclerView.adapter = mAdapter
    }

    /**
     * Request news articles from the news api
     */
    private fun requestNewsData() {
        val url = URL(getString(R.string.api_key) + "&language=en")
        val connection = url.openConnection() as HttpsURLConnection

        // On success, assign the values of the result to the ApiData class
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            apiData = Gson().fromJson(inputStreamReader, ApiData::class.java)
            inputStreamReader.close()
            inputStream.close()
        } else {
            println(connection.responseCode)
        }
    }

    // Create and inflate guest toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate((R.menu.guest_toolbar_layout), menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Guest toolbar login button. The button sends the user to the authentication activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login -> {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
