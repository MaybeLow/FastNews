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
import com.example.newsapp.activities.OtherProfileActivity
import com.example.newsapp.data.Profile
import com.example.newsapp.items.CommentItem
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Recycler adapter that inflates the activity with friends' profile data
 */
class FriendsAdapter (private val friendProfile: MutableList<Profile>) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    private lateinit var currentContext: Context

    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        currentContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_friend, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendInfo = friendProfile[position]

        holder.friendUsername.text = friendInfo.username
        GlobalScope.launch {
            getFriendProfilePicture(holder, friendInfo)
        }
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        var friendProfilePicture = itemView.findViewById<View>(R.id.friend_image) as ImageView
        var friendUsername = itemView.findViewById<View>(R.id.friend_username) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition
            val intent = Intent(context, OtherProfileActivity::class.java)
            intent.putExtra("userId", friendProfile[position].userId)
            context.startActivity(intent)
        }
    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return friendProfile.count()
    }

    private suspend fun getFriendProfilePicture(holder: FriendsAdapter.ViewHolder, friend: Profile) {
        val storage = Firebase.storage

        var storageRef: Task<Uri>?
        try {
            storageRef = storage.reference.child("ProfileImages/${friend.userId}").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            println("storage exception $e")
            storageRef = null
        }
        if (storageRef != null) {
            val context = currentContext as AppCompatActivity
            context.runOnUiThread {
                Picasso.get().load(storageRef.result).into(holder.friendProfilePicture)
                println("Comment image debugging: " + storageRef.result)
            }
        }
    }
}
