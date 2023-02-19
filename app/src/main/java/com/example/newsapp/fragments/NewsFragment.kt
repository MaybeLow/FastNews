package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.R
import com.example.newsapp.tabadapters.NewsTabAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Fragment that stores other fragments in the view pager.
 * The other fragments include, user news articles,
 * official news articles and friends' preferred articles
 */
class NewsFragment: Fragment(R.layout.fragment_news) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        // Tab layout and pager
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = view.findViewById<ViewPager2>(R.id.news_pager)

        val newsTitles = resources.getStringArray(R.array.newsTitles)
        viewPager.adapter = NewsTabAdapter(activity as AppCompatActivity)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = newsTitles[0]
                1 -> tab.text = newsTitles[1]
                2 -> tab.text = newsTitles[2]
            }
        }.attach()
        return view
    }
}
