package com.example.newsapp.recycleadapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activities.ArticleActivity
import com.example.newsapp.data.Headline
import com.squareup.picasso.Picasso

/**
 * Recycler adapter that inflates the activity with official news articles data
 */
class NewsAdapter (private val headlines: MutableList<Headline>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_headline, parent, false)
        print("headlines $headlines")
        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = headlines[position]
        // Bind image
//        if (info.image_url != "" && info.image_url != " " && info.image_url != null && info.image_url != "null") {
            try {
                Picasso.get().load(info.image_url).into(holder.image)
            } catch (e: java.lang.Exception) {
                println("image exception " + info.image_url)
            }
//        }
        // Bind title
        if (info.title != null && info.title != "" && info.title != " " && info.title != "null") {
            var title = info.title.split(" ")
            if (title.size > 10) {
                title = title.subList(0,10) + "..."
            }
            holder.title.text = title.joinToString(separator=" ")
        }
        // Bind source name
        if (info.source_id != null && info.source_id != "" && info.source_id != " " && info.source_id != "null") {
            var sourceName = info.source_id.split(" ")
            if (sourceName.size > 5) {
                sourceName = sourceName.subList(0,5) + "..."
            }
            holder.sourceName.text = sourceName.joinToString(separator=" ")
        }

        // Bind description
        if (info.description != null && info.description != "" && info.description != " " && info.description != "null") {
            var description = info.description.split(" ")
            if (description.size > 15) {
                description = description.subList(0,15) + "..."
            }
            holder.description.text = description.joinToString(separator=" ")
        }
    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return headlines.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var image = itemView.findViewById<View>(R.id.friend_image) as ImageView
        var title = itemView.findViewById<View>(R.id.item_title) as TextView
        var sourceName = itemView.findViewById<View>(R.id.item_source_name) as TextView
        var description = itemView.findViewById<View>(R.id.item_description) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition

            val intent = Intent(context, ArticleActivity::class.java)

            // Send the variables to the article activity that displays the entire article
            intent.putExtra("title", headlines[position].title)
            intent.putExtra("source_id", headlines[position].source_id)
            intent.putExtra("pubDate", headlines[position].pubDate)
            intent.putExtra("image_url", headlines[position].image_url)
            intent.putExtra("content", headlines[position].content)
            intent.putExtra("description", headlines[position].description)
            intent.putExtra("article_url", headlines[position].link)

            context.startActivity(intent)
        }
    }
}
