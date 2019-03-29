package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.xwray.groupie.ViewHolder
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenPhotoSocialBox : Fragment(), DereMethods {


    private lateinit var imageObject: Images
    private lateinit var currentUser: Users

    private lateinit var sharedViewModelForImage: SharedViewModelImage
    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    lateinit var addToBucket: ImageButton
    lateinit var bucketCount: TextView
    lateinit var commentCount: TextView

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
        val addComment = view.findViewById<TextView>(R.id.photo_social_comment_cta)
        val commentButton = view.findViewById<ImageButton>(R.id.photo_social_comment_icon)
        val imageDetails = view.findViewById<TextView>(R.id.photo_social_image_details)
        val likeCount = view.findViewById<TextView>(R.id.photo_social_like_count)
        val likeButton = view.findViewById<ImageButton>(R.id.photo_social_like_button)
        val authorReputation = view.findViewById<TextView>(R.id.photo_social_author_reputation)

        bucketCount = view.findViewById(R.id.photo_social_bucket_count)
        addToBucket = view.findViewById(R.id.photo_social_bucket_icon)
        commentCount = view.findViewById(R.id.photo_social_comment_count)



        Glide.with(this).load(currentUser.image).into(currentUserImage)

        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(user.image).into(authorUserImage)
                authorName.text = user.name
                authorReputation.text = "(${user.reputation})"
            }
        }
        )



        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                imageObject = image

                val stampMills = imageObject.timestamp
                val pretty = PrettyTime()
                val date = pretty.format(Date(stampMills))
                imageTimestamp.text = date

                imageDetails.text = imageObject.details

                checkIfBucketed(addToBucket, imageObject, currentUser.uid)
                listenToBucketCount(bucketCount, imageObject)


                listenToCommentCount(commentCount, imageObject)

                listenToLikeCount(likeCount, image)
                executeLike(imageObject, currentUser.uid, likeCount, likeButton, 0, currentUser.name, imageObject.photographer, authorReputation)



            }
        }
        )

        likeButton.setOnClickListener {
            executeLike(imageObject, currentUser.uid, likeCount, likeButton, 1, currentUser.name, imageObject.photographer, authorReputation)

        }


        authorName?.setOnClickListener {

            if (imageObject.photographer == currentUser.uid) {
                val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToDestinationProfile()
                findNavController().navigate(action)

            } else {
                val action =
                    ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToProfileRandomUserFragment()
                findNavController().navigate(action)
            }

        }

        addToBucket.setOnClickListener {

            if (imageObject.photographer == currentUser.uid){
                Toast.makeText(this.context, "You can't bucket your own photos", Toast.LENGTH_LONG).show()
            } else {
                val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToDestinationAddToBucket(imageObject, currentUser)
                findNavController().navigate(action)
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

        imageDetails.setOnClickListener {
            goToComments()
        }

//
//        followLink.setOnClickListener {
//            val action =
//                ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToWebViewFragment()
//            findNavController().navigate(action)
//        }

    }



    private fun goToComments() {
        val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToPhotoCommentsFragment()
        findNavController().navigate(action)
    }


    companion object {
        fun newInstance(): OpenPhotoSocialBox = OpenPhotoSocialBox()
    }
}
