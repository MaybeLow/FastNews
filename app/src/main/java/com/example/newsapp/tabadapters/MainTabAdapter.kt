package com.example.newsapp.tabadapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.fragments.ChatFragment
import com.example.newsapp.fragments.NewsFragment
import com.example.newsapp.fragments.ProfileFragment
import com.example.newsapp.fragments.SearchFragment

/**
 * Tab layout for the main activity
 */
class MainTabAdapter (activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(index: Int): Fragment {
        when (index) {
            0 -> return NewsFragment()
            1 -> return SearchFragment()
            2 -> return ChatFragment()
            3 -> return ProfileFragment()
        }
        return NewsFragment()
    }

    override fun getItemCount(): Int
    {
        return 4
    }
}
