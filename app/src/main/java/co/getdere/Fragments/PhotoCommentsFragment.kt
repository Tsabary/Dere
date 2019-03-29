package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.Interfaces.DereMethods
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import co.getdere.Models.Comments
import co.getdere.Models.Images
import co.getdere.Models.Users

import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comment_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class PhotoCommentsFragment : Fragment(), DereMethods {

    private lateinit var currentUser: Users
    private lateinit var randomUser: Users
    private lateinit var imageObject: Images

    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser
    private lateinit var sharedViewModelForImage: SharedViewModelImage

    val commentsRecyclerAdapter = GroupAdapter<ViewHolder>()
    val commentsRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            val sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            currentUser = sharedViewModelForCurrentUser.currentUserObject

            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_photo_comments, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authorImage = view.findViewById<CircleImageView>(R.id.photo_comments_author_image)
        val authorName = view.findViewById<TextView>(R.id.photo_comments_author_name)
        val currentUserPhoto = view.findViewById<CircleImageView>(R.id.photo_comments_current_user_image)
        val commentInput = view.findViewById<TextView>(R.id.photo_comments_comment_input)
        val postButton = view.findViewById<TextView>(R.id.photo_comments_post_button)
        val commentRecyclerView = view.findViewById<RecyclerView>(R.id.photo_comments_recycler)
        val imageDetails = view.findViewById<TextView>(R.id.photo_comments_photo_description)



        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->

                randomUser = user
            }
        }
        )


        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->

                imageObject = image
                imageDetails.text = imageObject.details
                listenToComments(image.id)
            }
        }
        )



        commentRecyclerView.adapter = commentsRecyclerAdapter
        commentRecyclerView.layoutManager = commentsRecyclerLayoutManager



        Glide.with(this).load(currentUser.image).into(currentUserPhoto)

        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(user.image).into(authorImage)
                authorName.text = user.name
            }
        }
        )


        postButton.setOnClickListener {

            val comment = Comments(currentUser.uid, commentInput.text.toString(), System.currentTimeMillis(), imageObject.id)

            commentInput.text = ""

            val ref = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/").push()

            val commentBodyRef = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/${ref.key}/body")

            commentBodyRef.setValue(comment).addOnSuccessListener {

                sendNotification(3, 16, currentUser.uid, currentUser.name, imageObject.id, ref.key!!, imageObject.photographer)

                Log.d("postCommentActivity", "Saved comment to Firebase Database")
            }.addOnFailureListener {
                Log.d("postCommentActivity", "Failed to save comment to database")
            }

        }

    }

    private fun listenToComments(imageId : String) {

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

class SingleComment(var comment: Comments, val commentId : String, val imageId: String, val currentUser: Users) : Item<ViewHolder>(), DereMethods {

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


    private fun executeLike(event : Int, likeButton : ImageButton, likeCount : TextView, imageId: String, currentUser: Users, viewHolder: ViewHolder){

        Log.d("doesitknow", comment.ImageId)
        Log.d("doesitknow", comment.content)

        val commentLikeRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(currentUser.uid)){

                    if (event == 1){
                        commentLikeRef.child(currentUser.uid).removeValue()

                        val likeCountNumber = likeCount.text.toString().toInt() -1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart)

                        changeReputation(
                            21,
                            commentId,
                            imageId,
                            currentUser.uid,
                            currentUser.name,
                            comment.authorId,
                            TextView(viewHolder.root.context),
                            "photoCommentLike"
                        )

                    } else {
                        likeButton.setImageResource(R.drawable.heart_active)                    }
                } else {

                    if (event == 1){
                        commentLikeRef.setValue(mapOf(currentUser.uid to 1))
                        val likeCountNumber = likeCount.text.toString().toInt() +1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart_active)

                        changeReputation(
                            20,
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

    private fun listenToLikeCount(commentLikeCount : TextView){

        val commentLikeRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                commentLikeCount.text = p0.childrenCount.toString()

            }


        })

    }


}
