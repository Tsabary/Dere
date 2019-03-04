package co.getdere.Models

import android.net.Uri
import android.widget.ImageView
import co.getdere.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*

class FeedImage(private val image: Uri) : Item<ViewHolder>() {


    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        Picasso.get().load(image).into(viewHolder.itemView.feed_single_photo_photo)

    }

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return super.getSpanSize(spanCount, position)
    }
}