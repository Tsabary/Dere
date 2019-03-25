package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import co.getdere.Models.Comments
import co.getdere.Models.Images
import co.getdere.Models.Users

import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comment_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class PhotoCommentsFragment : Fragment() {

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
                listenToComments()
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

            val comment = Comments(currentUser.uid, commentInput.text.toString(), System.currentTimeMillis())

            commentInput.text = ""

            val ref = FirebaseDatabase.getInstance().getReference("/comments/${imageObject.id}").push()

            ref.setValue(comment).addOnSuccessListener {
                Log.d("postCommentActivity", "Saved comment to Firebase Database")
//                commentsRecyclerAdapter.add(singleComment(comment))
            }.addOnFailureListener {
                Log.d("postCommentActivity", "Failed to save comment to database")
            }

        }

    }

    private fun listenToComments() {

        commentsRecyclerAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/comments/${imageObject.id}")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleCommentFromDB = p0.getValue(Comments::class.java)

                if (singleCommentFromDB != null) {

                    commentsRecyclerAdapter.add(singleComment(singleCommentFromDB))

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

class singleComment(var comment: Comments) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.comment_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val stampMills = comment.timeStamp
        val pretty = PrettyTime()
        val date = pretty.format(Date(stampMills))

        viewHolder.itemView.single_comment_content.text = comment.content
        viewHolder.itemView.single_comment_timestamp.text = date

        val refCommentUser = FirebaseDatabase.getInstance().getReference("/users/${comment.authorId}/profile")

        refCommentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                viewHolder.itemView.single_comment_author.text = user!!.name

                Glide.with(viewHolder.root.context).load(user.image).into(viewHolder.itemView.single_comment_photo)

            }

        })


    }

}
