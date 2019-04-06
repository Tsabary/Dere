package co.getdere.groupieAdapters

import co.getdere.models.Images
import co.getdere.R
import com.bumptech.glide.Glide
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions


class FeedImage(val image: Images) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageSmall).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.feed_single_photo_photo)
    }
}
