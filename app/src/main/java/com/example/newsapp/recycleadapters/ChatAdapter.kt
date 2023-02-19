package com.example.newsapp.recycleadapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activities.MessagerActivity
import com.example.newsapp.data.Profile
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

/**
 * Recycler adapter that inflates the activity with chat data
 */
class ChatAdapter (private val chatProfiles: MutableList<Profile>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var currentContext: Context
    /*
     * Inflate our views using the defined layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        currentContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_chat, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = chatProfiles[position]

        holder.name.text = info.username

        val userId = Firebase.auth.currentUser!!.uid

        getLastMessage(holder, userId, info)
        GlobalScope.launch {
            getCompanionProfilePicture(holder, info)
        }

    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return chatProfiles.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        var profilePicture = itemView.findViewById<View>(R.id.friend_image) as ImageView
        var name = itemView.findViewById<View>(R.id.friend_username) as TextView
        var lastMessage = itemView.findViewById<View>(R.id.last_message) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition

            // Start the messager activity
            val intent = Intent(context, MessagerActivity::class.java)
            intent.putExtra("userId", chatProfiles[position].userId)

            context.startActivity(intent)
        }
    }

    /**
     * Get the last message, sent by either of the users to be displayed on the chatter card
     */
    private fun getLastMessage(holder: ViewHolder, userId: String, companion: Profile) {
        val dbRef = Firebase.database("https://newsnetwork-cfe31-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val userMessageData = dbRef.child("chat").child(userId).child("chatters").child(companion.userId).child("last_message")
        userMessageData.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    val result = snapshot.getValue<String>()
                    if (result != null) {
                        holder.lastMessage.text = result
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("cancelled")
            }
        })
    }

    /**
     * Get the profile picture of the chatter
     */
    private suspend fun getCompanionProfilePicture(holder: ViewHolder, companion: Profile) {
        val storage = Firebase.storage

        var storageRef: Task<Uri>?
        try {
            storageRef = storage.reference.child("ProfileImages/${companion.userId}").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            storageRef = null
        }
        if (storageRef != null) {
            val theActivity = currentContext as AppCompatActivity
            theActivity.runOnUiThread() {
                Picasso.get().load(storageRef.result).into(holder.profilePicture)
                println("Chat image debugging: " + storageRef.result)
            }
        }
    }
}
