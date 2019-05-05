package co.getdere.groupieAdapters

import android.app.Activity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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
import kotlinx.android.synthetic.main.staggered_feed_post.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class StaggeredFeedImage(val image: Images, val currentUser: Users, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods, FCMMethods {

    val uid = FirebaseAuth.getInstance().uid
    val timestmap = image.timestampUpload

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var user: Users


    override fun getLayout(): Int {
        return R.layout.staggered_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(activity).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }

        val verifiedIcon = viewHolder.itemView.staggered_feed_verified
        val verifiedInfoText = viewHolder.itemView.staggered_feed_verified_info
        val verifiedInfoBox = viewHolder.itemView.staggered_feed_verified_box

        val imageView = viewHolder.itemView.staggered_feed_image
        val userName = viewHolder.itemView.staggered_feed_author_name

        val likeButton = viewHolder.itemView.staggered_feed_like_button

        (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio

        if (image.verified) {
            verifiedIcon.visibility = View.VISIBLE
        }


        verifiedIcon.setOnClickListener {
            if (verifiedInfoBox.visibility == View.GONE) {
                verifiedInfoBox.visibility = View.VISIBLE
            } else {
                verifiedInfoBox.visibility = View.GONE
            }
        }

        verifiedInfoBox.setOnClickListener {
            verifiedInfoBox.visibility = View.GONE
        }
        verifiedInfoText.setOnClickListener {
            verifiedInfoBox.visibility = View.GONE
        }

        val authorReputation = viewHolder.itemView.staggered_feed_author_reputation

        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageBig).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(imageView)


        executeLike(
            image,
            uid!!,
            TextView(viewHolder.root.context),
            likeButton,
            0,
            currentUser.name,
            image.photographer,
            authorReputation,
            activity
        )

        val refAuthor = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                user = p0.getValue(Users::class.java)!!

                userName.text = user.name
                authorReputation.text = "(${numberCalculation(user.reputation)})"
            }
        })

        userName.setOnClickListener {
            goToUserProfile(user)
        }

        authorReputation.setOnClickListener {
            goToUserProfile(user)
        }

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
                        activity
                    )
                }

                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                openImage()
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {
                activity.isFeedActive = true
                goToBucket()
                super.onLongPress(e)
            }
        })

        imageView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }


        likeButton.setOnClickListener {

            if (image.photographer != currentUser.uid) {

                executeLike(
                    image,
                    uid,
                    TextView(viewHolder.root.context),
                    likeButton,
                    1,
                    currentUser.name,
                    image.photographer,
                    authorReputation,
                    activity
                )
            }
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
