package com.example.newsapp.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.Profile
import com.example.newsapp.managers.ProfileDataRequestManager
import com.example.newsapp.recycleadapters.ChatAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

/**
 * The fragment of the chat window that shows the profiles the current user chats with
 */
class ChatFragment: Fragment(R.layout.fragment_chat) {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById<View>(R.id.chat_recycler) as RecyclerView // Bind to the recyclerview in the layout
        val layoutManager = LinearLayoutManager(this.context) // Get the layout manager
        recyclerView.layoutManager = layoutManager

        readFromDatabase()
        return view
    }

    /**
     * Read the chat history from the database
     */
    private fun readFromDatabase(): MutableList<Profile> {
        val chatProfiles = mutableListOf<Profile>()
        val auth = Firebase.auth
        val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val chatProfileData = dbRef.child("chat").child(auth.currentUser!!.uid).child("chatters")
        // Read from the database
        chatProfileData.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value,
                // and again whenever data at this location is updated.
                if (snapshot.value != null) {
                    val chatters = snapshot.getValue<HashMap<String, Any>>()
                    if (chatters != null) {
                        for (userId in chatters.keys) {
                            chatProfiles.add(ProfileDataRequestManager(userId).requestData())
                        }
                    }

                    val mAdapter = ChatAdapter(chatProfiles.distinct() as MutableList<Profile>)
                    recyclerView.adapter = mAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })
        return chatProfiles
    }

    override fun onResume() {
        readFromDatabase()
        super.onResume()
    }
}
