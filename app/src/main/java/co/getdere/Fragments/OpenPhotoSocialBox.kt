package co.getdere.Fragments


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import co.getdere.Interfaces.SharedViewModelCurrentUser
import co.getdere.Interfaces.SharedViewModelImage
import co.getdere.Interfaces.SharedViewModelRandomUser
import co.getdere.Interfaces.SharedViewModelRandomUserId
import co.getdere.Models.Images
import co.getdere.Models.SimpleString
import co.getdere.Models.Users


import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenPhotoSocialBox : Fragment() {


    private lateinit var imageObject: Images
    private lateinit var currentUser: Users

    private lateinit var sharedViewModelForImage: SharedViewModelImage
    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser


    val currentUserUid = FirebaseAuth.getInstance().uid
    val refCurrentUser = FirebaseDatabase.getInstance().getReference("/users/$currentUserUid")

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_open_photo_social_box, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val authorName = view.findViewById<TextView>(R.id.photo_social_author_name_and_text)
        val currentUserImage = view.findViewById<ImageView>(R.id.photo_social_current_user_image)
        val authorUserImage = view.findViewById<ImageView>(R.id.photo_social_author_image)
        val imageTimestamp = view.findViewById<TextView>(R.id.photo_social_timestamp)
        val addToBucket = view.findViewById<ImageButton>(R.id.photo_social_bucket_icon)
        val addComment = view.findViewById<TextView>(R.id.photo_social_comment_cta)
        val commentButton = view.findViewById<ImageButton>(R.id.photo_social_comment_icon)
        val commentCount = view.findViewById<TextView>(R.id.photo_social_comment_count)

//        val refImageAuthor = FirebaseDatabase.getInstance().getReference("users/${imageObject.photographer}")

        Glide.with(this).load(currentUser.image).into(currentUserImage)

        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let {
                Glide.with(this).load(it.image).into(authorUserImage)
                authorName.text = it.name
            }
        }
        )



        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let {image->
                imageObject = image

                val stampMills = imageObject.timestamp
                val pretty = PrettyTime()
                val date = pretty.format(Date(stampMills))
                imageTimestamp.text = date
            }
        }
        )


//        refCurrentUser.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                val currentUser = p0.getValue(Users::class.java)!!
//
//                val currentUserImageUri = Uri.parse(currentUser.image)
//                Picasso.get().load(currentUserImageUri).into(currentUserImage)
//
//            }
//
//        })


//        refImageAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                val imageAuthor = p0.getValue(Users::class.java)!!
//
//                authorName.text = imageAuthor.name
//                Picasso.get().load(Uri.parse(imageAuthor.image)).into(authorUserImage)
//            }
//
//        })





        authorName?.setOnClickListener {

            if (imageObject.photographer == currentUser.uid) {
                val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToDestinationProfile()
                findNavController(nav_host_fragment).navigate(action)

            } else {
                val action =
                    ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToProfileRandomUserFragment()
                findNavController().navigate(action)
            }

        }

        addToBucket.setOnClickListener {

            if (imageObject.photographer == currentUser.uid) {
                Toast.makeText(this.context, "You can't bucket your own photos", Toast.LENGTH_LONG).show()
            } else {
                val refCurrentUserBucket =
                    FirebaseDatabase.getInstance().getReference("/buckets/${currentUser.uid}").push()
                val bucketedImage = SimpleString(imageObject.id)
                refCurrentUserBucket.setValue(bucketedImage)
            }
        }

        addComment.setOnClickListener {
            goToComments()
        }

        currentUserImage.setOnClickListener {
            goToComments()
        }

        commentButton.setOnClickListener {
            goToComments()
        }

        commentCount.setOnClickListener {
            goToComments()
        }


    }

    private fun goToComments() {
        val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToPhotoCommentsFragment()
        findNavController().navigate(action)
    }


    companion object {
        fun newInstance(): OpenPhotoSocialBox = OpenPhotoSocialBox()
    }

}
