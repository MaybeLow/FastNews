package com.example.newsapp.tabadapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.fragments.FriendsFragment
import com.example.newsapp.fragments.HomeFragment
import com.example.newsapp.fragments.UserNewsFragment

/**
 * Tab layout for the news fragment
 */
class NewsTabAdapter (activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(index: Int): Fragment {
        when (index) {
            0 -> return UserNewsFragment()
            1 -> return HomeFragment()
            2 -> return FriendsFragment()
        }
        return HomeFragment()
    }

    // get item count - equal to number of tabs
    override fun getItemCount(): Int
    {
        return 3
    }
}
