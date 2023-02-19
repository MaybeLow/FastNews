package com.example.newsapp.managers

import android.content.ContentValues
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.data.Profile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Data request manager that receives information about the given profile, based on a user id
 * The class has the current user id as the default configuration
 */
class ProfileDataRequestManager(val userId: String = Firebase.auth.currentUser!!.uid,
                                private val db: FirebaseFirestore = Firebase.firestore) : AppCompatActivity() {
    private var profile: Profile? = null

    /**
     * Start the coroutine that attempts to get the profile data from the database
     */
    fun requestData(): Profile {
        GlobalScope.launch {
            val response = async {requestProfileData()}
            profile = response.await()
            response.join()
        }

        // Wait for the profile call for 10 seconds
        while (profile == null) {
            Thread.sleep(10)
            Log.w(ContentValues.TAG, "Waiting for profile call")
        }
        return profile as Profile
    }

    /**
     * Request the information about a user profile from the Firebase database
     */
    private suspend fun requestProfileData(): Profile {
        val database = db.collection("user").document(userId).get()
        database.await()

        val username = database.result.data!!["username"] as String
        val profilePicture = database.result.data!!["profile_picture"] as String
        val description = database.result.data!!["description"] as String
        val friendList = database.result.data!!["friend_list"] as MutableList<String>
        val userId = database.result.data!!["user_id"] as String

        return Profile(username, profilePicture, description, friendList, userId)
    }
}
