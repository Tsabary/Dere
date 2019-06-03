package co.getdere.groupieAdapters

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
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
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.image_peek.view.*
import kotlinx.android.synthetic.main.staggered_feed_post.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class StaggeredFeedImage(val image: Images, val currentUser: Users, val activity: MainActivity) : Item<ViewHolder>(),
    DereMethods, FCMMethods {

    val uid = FirebaseAuth.getInstance().uid

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

//    lateinit var user: Users
    lateinit var peekView: View
    lateinit var peekAndPop: PeekAndPop


    override fun getLayout(): Int {
        return R.layout.staggered_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var pnpPosition = 0

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(activity).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(activity).get(SharedViewModelRandomUser::class.java)
        }

        val imageView = viewHolder.itemView.staggered_feed_image
        val likeButton = viewHolder.itemView.staggered_feed_like_button

        (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio

        peekAndPop = PeekAndPop.Builder(activity)
            .peekLayout(R.layout.image_peek)
            .longClickViews(imageView).animateFling(true)
            .build()

        peekView = peekAndPop.peekView

        val peekImageView = peekView.image_peek_image
        peekAndPop.setOnGeneralActionListener(object : PeekAndPop.OnGeneralActionListener {
            override fun onPop(p0: View?, p1: Int) {

            }

            override fun onPeek(p0: View?, p1: Int) {
                (peekImageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio

                Glide.with(viewHolder.root.context).load(image.imageBig).thumbnail(
                    Glide.with(viewHolder.root.context)
                        .load(image.imageSmall)
                )
                    .into(peekImageView)

                FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val user = p0.getValue(Users::class.java)
                        if (user != null){
                            Glide.with(viewHolder.root.context).load(if(user.image.isNotEmpty()){user.image}else{R.drawable.user_profile}).into(peekView.image_peek_author_image)
                            peekView.image_peek_author_name.text = user.name
                            peekView.image_peek_author_reputation.text = "(${numberCalculation(user.reputation)})"
                            peekView.image_peek_timestamp.text = PrettyTime().format(Date(image.timestampUpload))
                        }
                    }
                })
            }
        })

        imageView.setOnClickListener {
            openImage()
        }

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


//        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
//
//            override fun onDown(e: MotionEvent?): Boolean {
//                return true
//            }
//
//            override fun onDoubleTap(e: MotionEvent?): Boolean {
//                if (uid != image.photographer) {
//                    executeLike(
//                        image,
//                        uid,
//                        TextView(viewHolder.root.context),
//                        likeButton,
//                        1,
//                        currentUser.name,
//                        image.photographer,
//                        TextView(viewHolder.root.context),
//                        activity,
//                        "staggered"
//                    )
//                }
//
//                return super.onDoubleTap(e)
//            }
//
//            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
//                openImage()
//                return super.onSingleTapConfirmed(e)
//            }
//
//            override fun onLongPress(e: MotionEvent?) {
//                peekAndPop.triggerOnHoldEvent(imageView,pnpPosition)
////                if (uid != image.photographer) {
////                    activity.isFeedActive = true
////                    goToBucket()
////                } else {
////                    activity.isFeedActive = true
////                    goToItinerary()
////                }
//
//
//
//
//                super.onLongPress(e)
//            }
//        })

//        imageView.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//        }


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


    private fun openImage() {
        sharedViewModelImage.sharedImageObject.postValue(image)

        // meanwhile in the background it will load the random user object

        val refRandomUser =
            FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java)!!)

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
