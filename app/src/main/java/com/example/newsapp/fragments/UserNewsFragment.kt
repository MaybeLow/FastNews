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
import com.example.newsapp.items.UserArticle
import com.example.newsapp.managers.UserArticleDataRequestManager
import com.example.newsapp.recycleadapters.UserNewsAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * The fragment, displaying the articles, written by Fast News users
 */
class UserNewsFragment: Fragment(R.layout.fragment_user_news) {
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_user_news, container, false)
        recyclerView = view.findViewById<View>(R.id.user_recycler_view) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
        recyclerView.layoutManager = layoutManager

        fetchUserArticles()

        return view
    }

    /**
     * Start the corouting that takes the user article data from the Firebase database
     */
    private fun fetchUserArticles() {
        GlobalScope.launch {
            val response = async { getUserArticles() }
            response.join()
        }
    }

    /**
     * Get the user article data from the database
     */
    private suspend fun getUserArticles() {
        val db = Firebase.firestore

        val users = mutableListOf<String>()
        val articles = mutableListOf<UserArticle>()

        val databaseDocuments = db.collection("user").get()
        databaseDocuments.await()
        val userDocuments = databaseDocuments.result.documents

        for (userDocument in userDocuments) {
            val userId = userDocument.data!!["user_id"] as String
            users.add(userId)
        }

        // Get the articles from all the users of the platform
        val currentActivity = activity as AppCompatActivity
        currentActivity.runOnUiThread {
            for (user in users) {
                val userArticles = UserArticleDataRequestManager(user).requestData()
                for (userArticle in userArticles) {
                    articles.add(userArticle)
                }
            }

            val mAdapter = UserNewsAdapter(articles)
            recyclerView.adapter = mAdapter
        }
    }

    /**
     * Update the variables, when the user is back to this fragment
     */
    override fun onResume() {
        fetchUserArticles()
        super.onResume()
    }
}
