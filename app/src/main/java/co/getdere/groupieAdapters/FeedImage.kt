package co.getdere.groupieAdapters

import android.view.View
import co.getdere.models.Images
import co.getdere.R
import com.bumptech.glide.Glide
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import android.util.TypedValue
import co.getdere.MainActivity


class FeedImage(val image: Images, private val case : Int) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageSmall).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.feed_single_photo_photo)

        val dip = 8f
        val r = viewHolder.root.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )

        viewHolder.itemView.feed_single_photo_card.radius =  if (case == 1){
            px
        } else {
            0f
        }



    }
}
