package com.example.newsapp.fragments

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.items.CommentItem
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.recycleadapters.CommentSectionAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * A fragment that shows the comment section of a user news article
 */
class CommentSectionFragment(private val articleId: String) : Fragment(R.layout.fragment_comment_section) {
    private lateinit var recyclerView: RecyclerView
    val currentUser = Firebase.auth.currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_comment_section, container, false)
        recyclerView = view.findViewById<View>(R.id.comment_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
        recyclerView.layoutManager = layoutManager

        GlobalScope.launch {
            getProfileImage(view, currentUser)
        }
        getComments()
        writeCommentSection(view)

        return view
    }

    /**
     * Get the comment history for the particular user news article
     */
    private fun getComments() {
        val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val userMessageData = dbRef.child("comment").child(articleId).child("commenters")

        val comments = mutableListOf<CommentItem>()
        userMessageData.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value,
                // and again whenever data at this location is updated.
                if (snapshot.value != null) {
                    val commenters = snapshot.getValue<HashMap<String, HashMap<String, CommentItem>>>()
                    Log.d(ContentValues.TAG, "commenters value is: $commenters")

                    // Read the database call
                    if (commenters != null) {
                        for (commenter in commenters) {
                            for (comment in commenter.value) {
                                comments.add(comment.value)
                            }
                        }
                    }

                    val mAdapter = CommentSectionAdapter(comments)
                    recyclerView.adapter = mAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })
    }

    /**
     * Get and display the profile pictures of the commenters
     */
    private suspend fun getProfileImage(view: View, currentUser: String) {
        val profilePictureView = view.findViewById<ImageView>(R.id.comment_profile_picture)

        // Access the article cover image from the firebase storage
        val storage = Firebase.storage
        var storageRef: Task<Uri>?

        try {
            storageRef = storage.reference.child("ProfileImages/$currentUser").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            storageRef = null
        }
        if (storageRef != null) {
            val currentActivity = activity as AppCompatActivity
            currentActivity.runOnUiThread() {
                Picasso.get().load(storageRef.result).into(profilePictureView)
                println("comment section profile storage firebase: " + storageRef.result)
            }
        }
    }

    /**
     * The current user can write his comments and submit them
     */
    private fun writeCommentSection(view: View) {
        val commentButton = view.findViewById<Button>(R.id.comment_button)
        commentButton.setOnClickListener {
            val commentView = view.findViewById<TextView>(R.id.enter_comment)
            val comment = commentView.text.toString()
            if (comment != "") {
                val auth = Firebase.auth
                val userId = auth.currentUser!!.uid
                val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference

                // Create a unique id for the comment
                val formatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss", Locale.GERMANY)
                val currentDate = LocalDateTime.now().format(formatter)

                val userMessageDatabase = dbRef.child("comment").child(articleId).child("commenters").child(userId).child(currentDate)
                userMessageDatabase.setValue(CommentItem(comment, currentDate, userId, articleId, ProfileDataRequestManager().requestData().username))

                getComments()
            }
        }
    }
}
