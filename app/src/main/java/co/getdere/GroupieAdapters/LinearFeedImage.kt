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
import co.getdere.Models.Users
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.linear_feed_post.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class LinearFeedImage(val image: Images) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.linear_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageBig).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.linear_feed_image)

        viewHolder.itemView.linear_feed_timestamp.text = PrettyTime().format(Date(image.timestamp))


        val refAuthor = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refAuthor.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                if (user!= null){

                    Glide.with(viewHolder.root.context).load(user.image).into(viewHolder.itemView.linear_feed__author_image)

                    viewHolder.itemView.linear_feed_author_name.text = user.name

                }

            }


        })
    }
}
