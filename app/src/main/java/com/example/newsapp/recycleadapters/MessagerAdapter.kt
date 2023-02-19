package com.example.newsapp.recycleadapters

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.items.MessageItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Recycler adapter that inflates the activity with messager data
 */
class MessagerAdapter (private val messages: MutableList<MessageItem>) : RecyclerView.Adapter<MessagerAdapter.ViewHolder>() {
    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_message, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        messages.sortBy { it.time }
        val messageInfo = messages[position]
        val auth = Firebase.auth

        holder.message.text = messageInfo.message
        println(auth.currentUser!!.uid != messageInfo.userId)
        if (auth.currentUser!!.uid != messageInfo.userId) {
            // Update parameters in case the message is sent by another user
            holder.messageCard.setCardBackgroundColor(Color.parseColor("#BBD1FF"))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
            }

            holder.messageCard.layoutParams = params
        }
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var message = itemView.findViewById<View>(R.id.message) as TextView
        var messageCard = itemView.findViewById<View>(R.id.message_card) as CardView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }
    }

    override fun getItemCount(): Int {
        return messages.count()
    }
}
