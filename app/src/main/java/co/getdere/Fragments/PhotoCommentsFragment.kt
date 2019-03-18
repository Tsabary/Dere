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
import co.getdere.Interfaces.SharedViewModelCurrentUser
import co.getdere.Interfaces.SharedViewModelImage
import co.getdere.Interfaces.SharedViewModelRandomUser
import co.getdere.Models.Comments
import co.getdere.Models.Images
import co.getdere.Models.Users

import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView


class PhotoCommentsFragment : Fragment() {

    private lateinit var currentUser: Users
    private lateinit var randomUser : Users
    private lateinit var imageObject: Images

    private lateinit var sharedViewModelForRandomUser : SharedViewModelRandomUser
    private lateinit var sharedViewModelForImage : SharedViewModelImage


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            val sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            currentUser = sharedViewModelForCurrentUser.currentUserObject

            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
                it?.let { user ->

                    randomUser = user
                }
            }
            )


            sharedViewModelForImage.sharedImageObject.observe(this, Observer {
                it?.let { image ->

                    imageObject = image
                }
            }
            )
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

        authorName.text =""

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

            }.addOnFailureListener {
                Log.d("postCommentActivity", "Failed to save comment to database")
            }

        }

    }

}
