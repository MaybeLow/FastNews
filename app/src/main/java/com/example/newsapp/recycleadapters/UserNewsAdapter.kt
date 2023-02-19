package com.example.newsapp.recycleadapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activities.UserArticleActivity
import com.example.newsapp.items.UserArticle
import com.squareup.picasso.Picasso

/**
 * Recycler adapter that inflates the activity with user news articles data
 */
class UserNewsAdapter (private val userArticles: MutableList<UserArticle>) : RecyclerView.Adapter<UserNewsAdapter.ViewHolder>() {
    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_user_headline, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        userArticles.sortByDescending { it.articleId }
        val info = userArticles[position]

        holder.title.text = info.title
        holder.description.text = info.description
        holder.sourceName.text = info.username
        Picasso.get().load(info.articleImage).into(holder.image)

        println("Image debugging: " + info.articleImage)
    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return userArticles.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var image = itemView.findViewById<View>(R.id.user_article_image) as ImageView
        var title = itemView.findViewById<View>(R.id.user_item_title) as TextView
        var sourceName = itemView.findViewById<View>(R.id.user_item_source_name) as TextView
        var description = itemView.findViewById<View>(R.id.user_item_description) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition

            // Send the user to the user article activity that displays
            // the detailed article from a fast news user
            val intent = Intent(context, UserArticleActivity::class.java)

            intent.putExtra("article_id", userArticles[position].articleId)
            intent.putExtra("user_id", userArticles[position].userId)

            context.startActivity(intent)
        }
    }
}
