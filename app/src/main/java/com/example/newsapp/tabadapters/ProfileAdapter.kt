package com.example.newsapp.tabadapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.fragments.ReadFragment
import com.example.newsapp.fragments.WriteFragment

/**
 * Tab layout for the profile fragment
 */
class ProfileAdapter (activity: AppCompatActivity, private val userId: String) : FragmentStateAdapter(activity) {
    override fun createFragment(index: Int): Fragment {
        when (index) {
            0 -> return WriteFragment(userId)
            1 -> return ReadFragment(userId)
        }
        return WriteFragment(userId)
    }

    // get item count - equal to number of tabs
    override fun getItemCount(): Int
    {
        return 2
    }
}
