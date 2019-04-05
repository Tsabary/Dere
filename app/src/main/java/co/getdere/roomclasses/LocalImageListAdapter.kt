package co.getdere.roomclasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.getdere.R

class LocalImageListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<LocalImageListAdapter.LocalImageViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var imagePosts = emptyList<LocalImagePost>() // Cached copy of words

    inner class LocalImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordItemView: TextView = itemView.findViewById(R.id.local_image_title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalImageViewHolder {
        val itemView = inflater.inflate(R.layout.local_image_item, parent, false)
        return LocalImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LocalImageViewHolder, position: Int) {
        val current = imagePosts[position]
        holder.wordItemView.text = current.imageUri
    }

    internal fun seImagePosts(imagePosts: List<LocalImagePost>) {
        this.imagePosts = imagePosts
        notifyDataSetChanged()
    }

    override fun getItemCount() = imagePosts.size
}