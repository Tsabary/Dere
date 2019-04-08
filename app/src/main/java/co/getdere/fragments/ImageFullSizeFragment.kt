package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Comments
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comment_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment(), DereMethods {


//    var viewPagerPosition = 0

    lateinit var imageObject: Images

    lateinit var sharedViewModelForImage: SharedViewModelImage
    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users

    val commentsRecyclerAdapter = GroupAdapter<ViewHolder>()
    val commentsRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

    lateinit var layoutScroll: NestedScrollView


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.image_with_comments, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authorName = view.findViewById<TextView>(R.id.photo_social_author_name)
        val authorReputation = view.findViewById<TextView>(R.id.photo_social_author_reputation)
        val authorImage = view.findViewById<CircleImageView>(R.id.photo_social_author_image)

        val currentUserImage = view.findViewById<CircleImageView>(R.id.photo_comments_current_user_image)

        val imageContent = view.findViewById<TextView>(R.id.photo_social_image_details)
        val imageTimestamp = view.findViewById<TextView>(R.id.photo_social_timestamp)

        val likeCount = view.findViewById<TextView>(R.id.photo_social_like_count)
        val likeButton = view.findViewById<ImageButton>(R.id.photo_social_like_button)
        val bucketCount = view.findViewById<TextView>(R.id.photo_social_bucket_count)
        val bucketButton = view.findViewById<ImageButton>(R.id.photo_social_bucket_icon)
        val commentCount = view.findViewById<TextView>(R.id.photo_social_comment_count)

        val postButton = view.findViewById<TextView>(R.id.photo_comments_post_button)
        val commentInput = view.findViewById<EditText>(R.id.photo_comments_comment_input)

        val mainImage = view.findViewById<ImageView>(R.id.image_full_image)

        layoutScroll = view.findViewById<NestedScrollView>(R.id.photo_social_scrollView)

        val commentsRecycler = view.findViewById<RecyclerView>(R.id.comments_recycler)

        commentsRecycler.adapter = commentsRecyclerAdapter
        commentsRecycler.layoutManager = commentsRecyclerLayoutManager

        Glide.with(this).load(currentUser.image).into(currentUserImage)



        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->

                imageObject = image

                if (image.id.isNotEmpty()) {

                    Glide.with(this).load(image.imageBig).into(mainImage)
                    imageTimestamp.text = PrettyTime().format(Date(image.timestampUpload))

                    if (image.details.isNotEmpty()) {
                        imageContent.visibility = View.VISIBLE
                        imageContent.text = image.details

                    }


                    checkIfBucketed(bucketButton, image, currentUser.uid)
                    listenToBucketCount(bucketCount, image)


                    listenToCommentCount(commentCount, image)

                    listenToLikeCount(likeCount, image)
                    executeLike(
                        image,
                        currentUser.uid,
                        likeCount,
                        likeButton,
                        0,
                        currentUser.name,
                        image.photographer,
                        authorReputation
                    )

                    listenToComments(image.id)
                }
            }
        })

        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->

                authorName.text = user.name
                authorReputation.text = "(${user.reputation})"
                Glide.with(context!!).load(user.image).into(authorImage)
            }
        })






        likeButton.setOnClickListener {
            executeLike(
                imageObject,
                currentUser.uid,
                likeCount,
                likeButton,
                1,
                currentUser.name,
                imageObject.photographer,
                authorReputation
            )
        }



        bucketButton.setOnClickListener {
            val activity = activity as MainActivity
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketFragment).commit()
            activity.subActive = activity.bucketFragment
        }

        postButton.setOnClickListener {
            val comment =
                Comments(currentUser.uid, commentInput.text.toString(), System.currentTimeMillis(), imageObject.id)

            commentInput.text.clear()

            val ref = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/").push()

            val commentBodyRef =
                FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/${ref.key}/body")

            commentBodyRef.setValue(comment).addOnSuccessListener {

                sendNotification(
                    3,
                    16,
                    currentUser.uid,
                    currentUser.name,
                    imageObject.id,
                    ref.key!!,
                    imageObject.photographer
                )

                layoutScroll.fullScroll(View.FOCUS_DOWN)

                Log.d("postCommentActivity", "Saved comment to Firebase Database")
            }.addOnFailureListener {
                Log.d("postCommentActivity", "Failed to save comment to database")
            }

        }

        authorName.setOnClickListener {
            goToRandomUserProfile()
        }

        authorImage.setOnClickListener {
            goToRandomUserProfile()
        }

        authorReputation.setOnClickListener {
            goToRandomUserProfile()
        }
    }

    private fun goToRandomUserProfile() {

        val activity = activity as MainActivity

        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.profileRandomUserFragment).commit()
        activity.subActive = activity.profileRandomUserFragment

    }


    private fun listenToComments(imageId: String) {

        commentsRecyclerAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleCommentFromDB = p0.child("body").getValue(Comments::class.java)

                if (singleCommentFromDB != null) {

                    commentsRecyclerAdapter.add(SingleComment(singleCommentFromDB, p0.key!!, imageId, currentUser))

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


}


class SingleComment(var comment: Comments, val commentId: String, val imageId: String, val currentUser: Users) :
    Item<ViewHolder>(),
    DereMethods {

//    val uid =FirebaseAuth.getInstance().uid

    override fun getLayout(): Int {
        return R.layout.comment_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val commentContent = viewHolder.itemView.findViewById<TextView>(R.id.single_comment_content)
        val commentTimestamp = viewHolder.itemView.findViewById<TextView>(R.id.single_comment_timestamp)
        val commentLikeCount = viewHolder.itemView.findViewById<TextView>(R.id.single_comment_like_count)
        val commentLikeButton = viewHolder.itemView.findViewById<ImageButton>(R.id.single_comment_like_button)
        val commentAuthor = viewHolder.itemView.findViewById<TextView>(R.id.single_comment_author)

        val stampMills = comment.timeStamp
        val pretty = PrettyTime()
        val date = pretty.format(Date(stampMills))

        commentContent.text = comment.content
        commentTimestamp.text = date

        listenToLikeCount(commentLikeCount)
        executeLike(0, commentLikeButton, commentLikeCount, imageId, currentUser, viewHolder)


        commentLikeButton.setOnClickListener {
            executeLike(1, commentLikeButton, commentLikeCount, imageId, currentUser, viewHolder)
        }


        val refCommentUser = FirebaseDatabase.getInstance().getReference("/users/${comment.authorId}/profile")

        refCommentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                commentAuthor.text = user!!.name

                Glide.with(viewHolder.root.context).load(user.image).into(viewHolder.itemView.single_comment_photo)

            }

        })


    }


    private fun executeLike(
        event: Int,
        likeButton: ImageButton,
        likeCount: TextView,
        imageId: String,
        currentUser: Users,
        viewHolder: ViewHolder
    ) {

        Log.d("doesitknow", comment.ImageId)
        Log.d("doesitknow", comment.content)

        val commentLikeRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(currentUser.uid)) {

                    if (event == 1) {
                        commentLikeRef.child(currentUser.uid).removeValue()

                        val likeCountNumber = likeCount.text.toString().toInt() - 1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart)

                        changeReputation(
                            13,
                            commentId,
                            imageId,
                            currentUser.uid,
                            currentUser.name,
                            comment.authorId,
                            TextView(viewHolder.root.context),
                            "photoCommentLike"
                        )

                    } else {
                        likeButton.setImageResource(R.drawable.heart_active)
                    }
                } else {

                    if (event == 1) {
                        commentLikeRef.setValue(mapOf(currentUser.uid to 1))
                        val likeCountNumber = likeCount.text.toString().toInt() + 1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart_active)

                        changeReputation(
                            12,
                            commentId,
                            imageId,
                            currentUser.uid,
                            currentUser.name,
                            comment.authorId,
                            TextView(viewHolder.root.context),
                            "photoCommentLike"
                        )

                    } else {
                        likeButton.setImageResource(R.drawable.heart)
                    }


                }

            }


        })


    }

    private fun listenToLikeCount(commentLikeCount: TextView) {

        val commentLikeRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                commentLikeCount.text = p0.childrenCount.toString()
            }
        })
    }
}
