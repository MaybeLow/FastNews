package com.example.newsapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.R
import com.example.newsapp.activities.EditProfileActivity
import com.example.newsapp.activities.FriendlistActivity
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.tabadapters.ProfileAdapter
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Fragment that displays the profile of the current user
 */
class ProfileFragment: Fragment(R.layout.fragment_profile) {
    lateinit var myView: View
    lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        myView = inflater.inflate(R.layout.fragment_profile, container, false)
        // Tab layout and pager
        val tabLayout = myView.findViewById<TabLayout>(R.id.profile_tab_layout)
        val viewPager = myView.findViewById<ViewPager2>(R.id.profile_pager)

        auth = Firebase.auth

        val profileTitles = resources.getStringArray(R.array.profileTitles)
        viewPager.adapter = ProfileAdapter(activity as AppCompatActivity, auth.currentUser!!.uid)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = profileTitles[0]
                1 -> tab.text = profileTitles[1]
            }
        }.attach()

        setEditButton()
        inflateProfile()
        inflateCards()

        return myView
    }

    /**
     * Inflate the profile fragment with the database data
     */
    private fun inflateProfile() {
        val username = myView.findViewById<TextView>(R.id.username)
        val profilePicture = myView.findViewById<ImageView>(R.id.profile_picture)
        val description = myView.findViewById<TextView>(R.id.description)

        val profile = ProfileDataRequestManager(userId = auth.currentUser!!.uid).requestData()

        username.text = profile.username

        // If the description is empty, display the default message
        if (profile.description != "") {
            description.text = profile.description
        } else {
            description.text = getString(R.string.default_description)
        }

        GlobalScope.launch {
            getProfileImage(profilePicture)
        }
    }

    /**
     * Inflate the friend list card that sends the current user to his friend list
     */
    private fun inflateCards() {
        val writeCard: CardView = myView.findViewById(R.id.friendlist_card)
        writeCard.setOnClickListener {
            val intent = Intent(myView.context, FriendlistActivity::class.java)
            intent.putExtra("userId", auth.currentUser!!.uid)
            startActivity(intent)
        }
    }

    /**
     * Get the profile picture of the user from the Firebase storage
     */
    private suspend fun getProfileImage(profilePicture: ImageView) {
        val storage = Firebase.storage
        val currentUser = Firebase.auth.currentUser!!.uid
        var storageRef: Task<Uri>?

        try {
            storageRef = storage.reference.child("ProfileImages/$currentUser").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            println("storage exception $e")
            storageRef = null
        }
        if (storageRef != null) {
            val currentActivity = activity as AppCompatActivity
            currentActivity.runOnUiThread {
                Picasso.get().load(storageRef.result).into(profilePicture)
                println("picaso image content profile storage firebase: " + storageRef.result)
            }
        }
    }

    /**
     * Edit button allows the users to change their personal information, such as profile description,
     * profile picture and username
     */
    private fun setEditButton() {
        val button = myView.findViewById<ImageView>(R.id.edit_button)
        button.setOnClickListener {
            val intent = Intent(myView.context, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Update all the variables when the user comes back to the activity
     */
    override fun onResume() {
        inflateProfile()
        super.onResume()
    }
}
