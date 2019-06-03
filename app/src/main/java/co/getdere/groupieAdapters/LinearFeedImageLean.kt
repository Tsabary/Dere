package co.getdere.groupieAdapters

import android.view.GestureDetector
import android.view.MotionEvent
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
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.linear_feed_post_lean.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class LinearFeedImageLean(val image: Images, val currentUser: Users, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods, FCMMethods {


    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var user: Users


    override fun getLayout(): Int {
        return R.layout.linear_feed_post_lean
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(activity).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }


        val bucketCount = viewHolder.itemView.linear_feed_lean_bucket_count
        val bucketButton = viewHolder.itemView.linear_feed_lean_bucket

        val likeButton = viewHolder.itemView.linear_feed_lean_like
        val likeCount = viewHolder.itemView.linear_feed_lean_like_count

        val commentCount = viewHolder.itemView.linear_feed_lean_comment_count


        val imageView = viewHolder.itemView.linear_feed_lean_image
        val imageTimestamp = viewHolder.itemView.linear_feed_lean_timestamp
        val userImage = viewHolder.itemView.linear_feed_lean_author_image
        val userName = viewHolder.itemView.linear_feed_lean_author_name

        val imageRatioColonIndex = image.ratio.indexOf(":", 0)
        val imageRatioWidth = image.ratio.subSequence(0, imageRatioColonIndex)
        val imageRationHeight = image.ratio.subSequence(imageRatioColonIndex + 1, image.ratio.length)

        val imageRatioFinal: Double = imageRatioWidth.toString().toDouble() / imageRationHeight.toString().toDouble()


        when {
            imageRatioFinal > 1.25 -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "5:4"
            imageRatioFinal < 0.8 -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "4:5"
            else -> (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio
        }


        Glide.with(viewHolder.root.context).load(currentUser.image)
            .into(viewHolder.itemView.linear_feed_lean_current_user_photo)

        val authorReputation = viewHolder.itemView.linear_feed_lean_author_reputation

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageBig).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(imageView)

        imageTimestamp.text = PrettyTime().format(Date(image.timestampUpload))


        listenToBucketCount(bucketCount, image)

        checkIfBucketed(bucketButton, image, currentUser.uid)

        listenToLikeCount(likeCount, image)

        executeLike(
            image,
            currentUser.uid,
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

                if (p0.getValue(Users::class.java) != null) {
                    user = p0.getValue(Users::class.java)!!

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
                if (currentUser.uid != image.photographer) {
                    executeLike(
                        image,
                        currentUser.uid,
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
                if (currentUser.uid != image.photographer) {
                    activity.isFeedActive = true
                    goToBucket()
                } else {
                    goToItinerary()
                }

                super.onLongPress(e)

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

        imageView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        likeButton.setOnClickListener {

            if (image.photographer != currentUser.uid) {
                executeLike(
                    image,
                    currentUser.uid,
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
            if (image.photographer != currentUser.uid) {
                goToBucket()
            } else {
                goToItinerary()
            }
        }
    }

    private fun goToUserProfile(user: Users) {
        if (user.uid != currentUser.uid){
            sharedViewModelRandomUser.randomUserObject.postValue(user)
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.profileRandomUserFragment).commit()
            activity.subActive = activity.profileRandomUserFragment
            activity.switchVisibility(1)
            activity.isFeedActive = true
        } else {
            activity.navigateToProfile()
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

                activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.imageFullSizeFragment, "imageFullSizeFragment").addToBackStack("imageFullSizeFragment").commit()

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

                activity.subFm.beginTransaction().hide(activity.subActive)
                    .add(R.id.feed_subcontents_frame_container, activity.addToBucketFragment, "addToBucketFragment")
                    .commit()
                activity.subActive = activity.addToBucketFragment
                activity.switchVisibility(1)
            }

        })

    }

    private fun goToItinerary() {
        sharedViewModelImage.sharedImageObject.postValue(image)
        activity.subFm.beginTransaction().hide(activity.subActive)
            .add(R.id.feed_subcontents_frame_container, activity.addToItineraryFragment, "addToItineraryFragment")
            .commit()
        activity.subActive = activity.addToItineraryFragment
        activity.switchVisibility(1)
    }
}
