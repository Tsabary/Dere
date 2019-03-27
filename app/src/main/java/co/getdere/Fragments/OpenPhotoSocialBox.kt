package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import co.getdere.Models.Images
import co.getdere.Models.SimpleString
import co.getdere.Models.Users


import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenPhotoSocialBox : Fragment() {


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
        val followLink = view.findViewById<ImageButton>(R.id.photo_social_link_icon)
        val imageDetails = view.findViewById<TextView>(R.id.photo_social_image_details)
        bucketCount = view.findViewById<TextView>(R.id.photo_social_bucket_count)
        addToBucket = view.findViewById<ImageButton>(R.id.photo_social_bucket_icon)
        commentCount = view.findViewById<TextView>(R.id.photo_social_comment_count)


//        checkIfBucketed()

//        val refImageAuthor = FirebaseDatabase.getInstance().getReference("users/${imageObject.photographer}")

        Glide.with(this).load(currentUser.image).into(currentUserImage)

        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(user.image).into(authorUserImage)
                authorName.text = user.name
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

                checkIfBucketed(0)
                listenToBucketCount()
                listenToCommentCount()


            }
        }
        )


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

            checkIfBucketed(1)
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


        followLink.setOnClickListener {
            val action =
                ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToWebViewFragment()
            findNavController().navigate(action)
        }

    }


    private fun listenToCommentCount() {

        val refAllImagesComments = FirebaseDatabase.getInstance().getReference("/comments")

        refAllImagesComments.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(imageObject.id)) {

                    val refImageBucketingList =
                        FirebaseDatabase.getInstance().getReference("/comments/${imageObject.id}")

                    refImageBucketingList.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var count = 0

                            for (ds in p0.children) {
                                count += 1
                                commentCount.text = count.toString()
                            }
                        }

                    })


                } else {
                    commentCount.text = ""
                }

            }

        })


    }


    private fun listenToBucketCount() {

        val refAllImagesBuckets = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}")

        refAllImagesBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("buckets")) {

                    val refImageBucketingList =
                        FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/buckets")

                    refImageBucketingList.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var count = 0

                            for (ds in p0.children) {
                                count += 1
                                bucketCount.text = count.toString()
                            }
                        }

                    })


                } else {
                    bucketCount.text = ""
                }

            }

        })


    }


    private fun checkIfBucketed(ranNum: Int) {

        val refUserBucket = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")

        refUserBucket.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(imageObject.id)) {
                    if (ranNum == 1) {

                        if (currentUser.uid == imageObject.photographer) {
                            Toast.makeText(context, "You can't bucket your own photos", Toast.LENGTH_SHORT).show()
                        } else {
                            addToBucket.setImageResource(R.drawable.bucket)

                            refUserBucket.child(imageObject.id).removeValue().addOnCompleteListener {
                                val refImageBucketingList =
                                    FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/buckets")

                                refImageBucketingList.child(currentUser.uid).removeValue().addOnCompleteListener {
                                    listenToBucketCount()
                                }
                            }
                        }


                    } else {
                        addToBucket.setImageResource(R.drawable.bucket_saved)
                    }


                } else {
                    if (ranNum == 1) {

                        if (currentUser.uid == imageObject.photographer) {
                            Toast.makeText(context, "You can't bucket your own photos", Toast.LENGTH_SHORT).show()
                        } else {
                            addToBucket.setImageResource(R.drawable.bucket_saved)

                            val refCurrentUserBucket =
                                FirebaseDatabase.getInstance()
                                    .getReference("/buckets/users/${currentUser.uid}/${imageObject.id}")
                            val bucketedImage = SimpleString(imageObject.id)
                            refCurrentUserBucket.setValue(bucketedImage).addOnCompleteListener {
                                val refCurrentImageBucket =
                                    FirebaseDatabase.getInstance()
                                        .getReference("/buckets/images/${imageObject.id}/${currentUser.uid}")
                                val bucketingUser = SimpleString(currentUser.uid)
                                refCurrentImageBucket.setValue(bucketingUser).addOnCompleteListener {
                                    listenToBucketCount()
                                }
                            }
                        }

                    } else {
                        addToBucket.setImageResource(R.drawable.bucket)

                    }

                }
            }

        })


    }

    private fun goToComments() {
        val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToPhotoCommentsFragment()
        findNavController().navigate(action)
    }


    companion object {
        fun newInstance(): OpenPhotoSocialBox = OpenPhotoSocialBox()
    }

}
