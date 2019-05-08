package co.getdere.groupieAdapters

import android.app.Activity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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

    lateinit var user: Users


    override fun getLayout(): Int {
        return R.layout.linear_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity

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
        val imageTimestamp = viewHolder.itemView.linear_feed_timestamp
        val userImage = viewHolder.itemView.linear_feed_author_image
        val userName = viewHolder.itemView.linear_feed_author_name

        val imageRatioColonIndex = image.ratio.indexOf(":", 0)
        val imageRatioWidth = image.ratio.subSequence(0, imageRatioColonIndex)
        val imageRationHeight = image.ratio.subSequence(imageRatioColonIndex + 1, image.ratio.length)

        val imageRatioFinal: Double = imageRatioWidth.toString().toDouble() / imageRationHeight.toString().toDouble()


        when {
            imageRatioFinal > 1.25 -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "5:4"
            imageRatioFinal < 0.8 -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "4:5"
            else -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio
        }


        viewHolder.itemView.linear_feed_tags.text = image.tags.joinToString()

//        if (!image.verified) {
//            verifiedIcon.visibility = View.VISIBLE
//        }


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

        imageTimestamp.text = PrettyTime().format(Date(image.timestampUpload))


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
            activity,
            "linear"
        )

        listenToCommentCount(commentCount, image)


        val refAuthor = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                user = p0.getValue(Users::class.java)!!

                if (user != null) {

                    Glide.with(viewHolder.root.context).load(user.image)
                        .into(userImage)

                    userName.text = user.name
                    authorReputation.text = "(${numberCalculation(user.reputation)})"
                }
            }
        })


        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                if (uid != image.photographer) {
                    executeLike(
                        image,
                        uid,
                        TextView(viewHolder.root.context),
                        likeButton,
                        1,
                        currentUser.name,
                        image.photographer,
                        authorReputation,
                        activity,
                        "linear"
                    )
                }

                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                openImage()
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {
                if (uid != image.photographer) {
                    activity.isFeedActive = true
                    goToBucket()
                    super.onLongPress(e)
                }
            }
        })



        userName.setOnClickListener {
            goToUserProfile(user)
        }
        userImage.setOnClickListener {
            goToUserProfile(user)
        }

        authorReputation.setOnClickListener {
            goToUserProfile(user)
        }

        imageTimestamp.setOnClickListener {
            goToUserProfile(user)
        }

        imageView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
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
                    activity,
                    "linear"
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

    private fun goToUserProfile(user: Users) {

        sharedViewModelRandomUser.randomUserObject.postValue(user)
        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.profileRandomUserFragment).commit()
        activity.subActive = activity.profileRandomUserFragment
        activity.switchVisibility(1)
        activity.isFeedActive = true
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
