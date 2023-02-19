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
import com.example.newsapp.items.CommentItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Recycler adapter that inflates the activity with comment section data
 */
class CommentSectionAdapter (private val comments: MutableList<CommentItem>) : RecyclerView.Adapter<CommentSectionAdapter.ViewHolder>() {
    private lateinit var currentContext: Context
    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        currentContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_comment, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        comments.sortByDescending { it.time }

        val info = comments[position]

        holder.name.text = info.username

        holder.lastMessage.text = info.comment

        GlobalScope.launch {
            getCommenterProfilePicture(holder, info)
        }

    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return comments.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        var profilePicture = itemView.findViewById<View>(R.id.comment_image) as ImageView
        var name = itemView.findViewById<View>(R.id.commenter_username) as TextView
        var lastMessage = itemView.findViewById<View>(R.id.comment) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition

            // When the comment is clicked, send the user to the commenter's profile
            val intent = Intent(context, OtherProfileActivity::class.java)
            intent.putExtra("userId", comments[position].userId)

            context.startActivity(intent)
        }
    }

    /**
     * Get the profile picture of the commenter
     */
    private suspend fun getCommenterProfilePicture(holder: ViewHolder, comment: CommentItem) {
        val storage = Firebase.storage

        var storageRef: Task<Uri>?
        try {
            storageRef = storage.reference.child("ProfileImages/${comment.userId}").downloadUrl
            storageRef.await()
        } catch (e: StorageException) {
            println("storage exception $e")
            storageRef = null
        }
        if (storageRef != null) {
            val context = currentContext as AppCompatActivity
            context.runOnUiThread {
                Picasso.get().load(storageRef.result).into(holder.profilePicture)
                println("Comment image debugging: " + storageRef.result)
            }
        }
    }
}
