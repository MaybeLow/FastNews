package com.example.newsapp.activities

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.R
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.tabadapters.ProfileAdapter
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity that displays the profiles of users, that are not the current user
 */
class OtherProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        // Get the id of the user from the caller activity
        val userId: String = intent.getStringExtra("userId").toString()

        // Tab layout and pager
        val tabLayout = findViewById<TabLayout>(R.id.profile_tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.profile_pager)

        viewPager.adapter = ProfileAdapter(this, userId)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "write"
                1 -> tab.text = "read"
            }
        }.attach()

        // Button that sends the current user to the messager activity
        // that will allow him to chat with the user
        val messageButton = findViewById<CardView>(R.id.message_view)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessagerActivity::class.java)
            intent.putExtra("userId", userId)

            startActivity(intent)
        }

        // Inflate the profile with information from the database
        inflateProfile(userId)
        inflateCards(userId)
        updateAddFriendCard(userId)
    }

    /**
     * Call coroutine to get data from the database
     */
    private fun updateAddFriendCard(userId: String) {
        GlobalScope.launch {
            createAddFriendCard(userId)
        }
    }

    /**
     * Inflate the profile information
     */
    private fun inflateProfile(userId: String) {
        val username = findViewById<TextView>(R.id.username)
        GlobalScope.launch {
            inflateProfilePicture(userId)
        }
        val description = findViewById<TextView>(R.id.description)

        val profile = ProfileDataRequestManager(userId = userId).requestData()

        username.text = profile.username
        // If the profile description is empty, display the standard one
        if (profile.description != "") {
            description.text = profile.description
        } else {
            description.text = getString(R.string.default_description)
        }
    }

    /**
     * Call the firebase storage to get the profile picture of the user
     */
    private suspend fun inflateProfilePicture(userId: String) {
        val profilePicture = findViewById<ImageView>(R.id.profile_picture)

        val storage = Firebase.storage
        var storageRef: Task<Uri>?

        try {
            storageRef = storage.reference.child("ProfileImages/$userId").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            println("database exception $e")
            storageRef = null
        }
        if (storageRef != null) {
            runOnUiThread() {
                Picasso.get().load(storageRef.result).into(profilePicture)
                Log.w(ContentValues.TAG, "Picasso image content profile storage firebase: " + storageRef.result)
            }
        }
    }

    /**
     * Inflate the friend list card
     */
    private fun inflateCards(userId: String) {
        val writeCard: CardView = findViewById(R.id.friendlist_card)
        writeCard.setOnClickListener {
            val intent = Intent(this, FriendlistActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    /**
     * Create the card that allows the current user to add the other user to hit friend list.
     * The button will change the label based on the state of the friendship.
     * If the request is pending, the button will say "pending".
     * The user can cancel a friend request at any time.
     * Also, the user can unfriend the other user at any time.
     */
    private suspend fun createAddFriendCard(userId: String) {
        // Button initialisation
        val addFriendButton = findViewById<CardView>(R.id.friendship_status_view)
        val friendButtonText = findViewById<TextView>(R.id.friendship_status)

        // Firebase data calls
        val auth = Firebase.auth
        val db = Firebase.firestore
        val userDocument = db.collection("user").document(auth.currentUser!!.uid)
        val friendDocument = db.collection("user").document(userId)
        val userData = userDocument.get()
        val friendData = friendDocument.get()

        // Wait for the database calls
        userData.await()
        friendData.await()

        val requestedFriends = userData.result.data!!["requested_friends"] as MutableList<String>
        val friendList = userData.result.data!!["friend_list"] as MutableList<String>
        val pendingFriends = userData.result.data!!["pending_friends"] as MutableList<String>

        val friendRequestedFriends = friendData.result.data!!["requested_friends"] as MutableList<String>
        val friendFriendList = friendData.result.data!!["friend_list"] as MutableList<String>
        val friendPendingFriends = friendData.result.data!!["pending_friends"] as MutableList<String>

        // If the other user is not in any of the data array, the current user is be able to
        // request a friendship from the other user
        if (!requestedFriends.contains(userId)
            && !friendList.contains(userId)
            && !pendingFriends.contains(userId)) {
            this.runOnUiThread {
                friendButtonText.text = getString(R.string.add_friend)
                addFriendButton.setOnClickListener {
                    // On success, update the data arrays accordingly
                    requestedFriends.add(userId)
                    friendPendingFriends.add(auth.currentUser!!.uid)
                    userDocument
                        .update("requested_friends", requestedFriends)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend request sent to: $userId")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to send friend request to: $userId")
                        }
                    friendDocument
                        .update("pending_friends", friendPendingFriends)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend pending sent to: $userId")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to send friend pending to: $userId")
                        }
                    updateAddFriendCard(userId)
                }
            }
        // If the other user is already in the list of requested users, the current user
        // is able to cancel his friend request
        } else if (requestedFriends.contains(userId)) {
            this.runOnUiThread {
                friendButtonText.text = getString(R.string.requested)
                addFriendButton.setOnClickListener {
                    requestedFriends.remove(userId)
                    friendPendingFriends.remove(auth.currentUser!!.uid)

                    userDocument
                        .update("requested_friends", requestedFriends)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend request successfully deleted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to delete friend request")
                        }
                    friendDocument
                        .update("pending_friends", friendPendingFriends)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend pending successfully deleted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to delete pending friend request")
                        }
                    updateAddFriendCard(userId)
                }
            }
        // If the other user is in the friend list, the current user can unfriend him
        } else if (friendList.contains(userId)) {
            this.runOnUiThread {
                friendButtonText.text = getString(R.string.unfriend)
                addFriendButton.setOnClickListener {
                    friendList.remove(userId)
                    friendFriendList.remove(auth.currentUser!!.uid)
                    userDocument
                        .update("friend_list", friendList)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend successfully deleted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to delete a friend")
                        }
                    friendDocument
                        .update("friend_list", friendFriendList)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend successfully deleted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to delete a friend")
                        }
                    updateAddFriendCard(userId)
                }
            }
        // In case the other user is already in the requested array, the current user is able to
        // accept the friend invitation
        } else if (pendingFriends.contains(userId)) {
            this.runOnUiThread {
                friendButtonText.text = getString(R.string.accept)
                addFriendButton.setOnClickListener {
                    friendList.add(userId)
                    friendFriendList.add(auth.currentUser!!.uid)
                    pendingFriends.remove(userId)
                    friendRequestedFriends.remove(auth.currentUser!!.uid)

                    val userUpdate = hashMapOf(
                        "friend_list" to friendList,
                        "pending_friends" to pendingFriends
                    )
                    val friendUpdate = hashMapOf(
                        "friend_list" to friendFriendList,
                        "requested_friends" to friendRequestedFriends
                    )

                    userDocument
                        .update(userUpdate as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend request successfully accepted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to accept friend request")
                        }
                    friendDocument
                        .update(friendUpdate as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Friend request successfully accepted")
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Fail to accept friend request")
                        }
                    updateAddFriendCard(userId)
                }
            }
        }
    }
}
