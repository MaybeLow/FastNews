package com.example.newsapp.recycleadapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activities.SearchingNewsActivity

/**
 * Recycler adapter that inflates the activity with search history data
 */
class SearchAdapter (private val searchHistory: MutableList<String>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    /*
     * Inflate our views using the layout defined in text_row_item.xml
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_search, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = searchHistory[position]

        holder.search.text = info
    }

    /*
     * Get the maximum size of the array
     */
    override fun getItemCount(): Int {
        return searchHistory.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        var search = itemView.findViewById<View>(R.id.search_text) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = v.context
            val position = this.layoutPosition

            // Send the user to the activity, that displays the news articles
            // based on the chosen search keyword
            val intent = Intent(context, SearchingNewsActivity::class.java)

            intent.putExtra("searchInput", searchHistory[position])

            context.startActivity(intent)
        }
    }
}
