package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.managers.UserArticleDataRequestManager
import com.example.newsapp.recycleadapters.UserNewsAdapter

/**
 * Fragment that displays the article the user is writing, inside his profile screen
 */
class WriteFragment (private val userId: String) : Fragment(R.layout.fragment_write) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_write, container, false)
        val recyclerView = view.findViewById<View>(R.id.recycler_view) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
        recyclerView.layoutManager = layoutManager
        val mAdapter = UserNewsAdapter(UserArticleDataRequestManager(userId).requestData())
        recyclerView.adapter = mAdapter
        return view
    }
}
