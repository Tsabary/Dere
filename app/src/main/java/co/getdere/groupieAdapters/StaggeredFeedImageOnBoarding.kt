package co.getdere.groupieAdapters

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.otherClasses.FCMMethods
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.staggered_feed_post.view.*


class StaggeredFeedImageOnBoarding(val image: Images, val currentUser: Users, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods, FCMMethods {

    val uid = FirebaseAuth.getInstance().uid
//    val timestmap = image.timestampUpload

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var user: Users


    override fun getLayout(): Int {
        return R.layout.staggered_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(activity).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }


        val imageView = viewHolder.itemView.staggered_feed_image
        val likeButton = viewHolder.itemView.staggered_feed_like_button

        (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio

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
            TextView(viewHolder.root.context),
            activity,
            "staggered"
        )

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
                        TextView(viewHolder.root.context),
                        activity,
                        "staggered"
                    )
                }

                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                goToBucket()
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {

                    goToBucket()
                    super.onLongPress(e)

            }
        })

        imageView.setOnTouchListener { _, event ->
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
                    TextView(viewHolder.root.context),
                    activity,
                    "staggered"
                )
            }
        }
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

                activity.onBoardingFragment.viewPager.currentItem = 5
            }

        })

    }
}
