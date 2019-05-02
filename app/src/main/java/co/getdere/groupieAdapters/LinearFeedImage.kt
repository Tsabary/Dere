package co.getdere.groupieAdapters

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.otherClasses.FCMMethods
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pedromassango.doubleclick.DoubleClick
import com.pedromassango.doubleclick.DoubleClickListener
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.linear_feed_post.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class LinearFeedImage(val image: Images, val currentUser: Users, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods, FCMMethods {

    val uid = FirebaseAuth.getInstance().uid

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser


    override fun getLayout(): Int {
        return R.layout.linear_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity as MainActivity

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(activity).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }


        val bucketCount = viewHolder.itemView.linear_feed_bucket_count
        val bucketButton = viewHolder.itemView.linear_feed_bucket

        val likeButton = viewHolder.itemView.linear_feed_like
        val likeCount = viewHolder.itemView.linear_feed_like_count

        val commentCount = viewHolder.itemView.linear_feed_comment_count

        val imageDescription = viewHolder.itemView.linear_feed_image_details

        val verifiedIcon = viewHolder.itemView.linear_feed_verified
        val verifiedInfoText = viewHolder.itemView.linear_feed_verified_info
        val verifiedInfoBox = viewHolder.itemView.linear_feed_verified_box

        val imageView = viewHolder.itemView.linear_feed_image


        viewHolder.itemView.linear_feed_tags.text = image.tags.joinToString()

//        if (image.details.isNotEmpty()){
//            imageDescription.visibility = View.VISIBLE
//            imageDescription.text = image.details
//        }

        if (image.verified) {
            verifiedIcon.visibility = View.VISIBLE
        }


        verifiedIcon.setOnClickListener {
            if (verifiedInfoBox.visibility == View.GONE) {
                verifiedInfoBox.visibility = View.VISIBLE
                verifiedInfoText.visibility = View.VISIBLE
            } else {
                verifiedInfoBox.visibility = View.GONE
                verifiedInfoText.visibility = View.GONE
            }
        }

        verifiedInfoBox.setOnClickListener {
            verifiedInfoBox.visibility = View.GONE
            verifiedInfoText.visibility = View.GONE
        }
        verifiedInfoText.setOnClickListener {
            verifiedInfoBox.visibility = View.GONE
            verifiedInfoText.visibility = View.GONE
        }

        Glide.with(viewHolder.root.context).load(currentUser.image)
            .into(viewHolder.itemView.linear_feed_current_user_photo)

        val authorReputation = viewHolder.itemView.linear_feed_author_reputation

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageBig).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(imageView)

        viewHolder.itemView.linear_feed_timestamp.text = PrettyTime().format(Date(image.timestampUpload))


        listenToBucketCount(bucketCount, image)

        checkIfBucketed(bucketButton, image, uid!!)

        listenToLikeCount(likeCount, image)

        executeLike(
            image,
            uid,
            likeCount,
            likeButton,
            0,
            currentUser.name,
            image.photographer,
            authorReputation,
            activity
        )

        listenToCommentCount(commentCount, image)


        val refAuthor = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                if (user != null) {

                    Glide.with(viewHolder.root.context).load(user.image)
                        .into(viewHolder.itemView.linear_feed_author_image)

                    viewHolder.itemView.linear_feed_author_name.text = user.name
                    authorReputation.text = "(${numberCalculation(user.reputation)})"

                }
            }
        })


        imageView.setOnLongClickListener {
            goToBucket()
            return@setOnLongClickListener true
        }



        imageView.setOnClickListener {
            object : DoubleClick((object : DoubleClickListener {
                override fun onDoubleClick(view: View?) {

                    Log.d("trytrytry", "double click")
                    if (image.photographer != currentUser.uid) {

                        executeLike(
                            image,
                            uid,
                            likeCount,
                            likeButton,
                            1,
                            currentUser.name,
                            image.photographer,
                            authorReputation,
                            activity
                        )
                    }
                }

                override fun onSingleClick(view: View?) {

                    Log.d("trytrytry", "single click")

                    openImage()
                }

            })) {

            }

        }

        likeButton.setOnClickListener {

            if (image.photographer != currentUser.uid) {

                executeLike(
                    image,
                    uid,
                    likeCount,
                    likeButton,
                    1,
                    currentUser.name,
                    image.photographer,
                    authorReputation,
                    activity
                )
            }
        }

        bucketButton.setOnClickListener {
            goToBucket()
        }

        viewHolder.itemView.setOnClickListener {
            openImage()
        }
    }

    private fun openImage() {

        sharedViewModelImage.sharedImageObject.postValue(image)


        // meanwhile in the background it will load the random user object

        val refRandomUser =
            FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val randomUserObject = p0.getValue(Users::class.java)!!

                sharedViewModelRandomUser.randomUserObject.postValue(randomUserObject)

                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()

                activity.switchVisibility(1)

                activity.subActive = activity.imageFullSizeFragment
            }

        })
    }

    private fun goToBucket() {

        sharedViewModelImage.sharedImageObject.postValue(image)

        val randomUserRef = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")
        randomUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users::class.java)
                sharedViewModelRandomUser.randomUserObject.postValue(user)

                activity as MainActivity
                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketFragment).commit()
                activity.subActive = activity.bucketFragment
                activity.switchVisibility(1)
            }

        })

    }
}
