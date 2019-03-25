package co.getdere.GroupieAdapters

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import co.getdere.Models.Images
import co.getdere.R
import com.bumptech.glide.Glide
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import androidx.fragment.app.Fragment
import co.getdere.Fragments.FeedFragment
import co.getdere.MainActivity
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
