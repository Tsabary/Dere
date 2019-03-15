package co.getdere.Fragments


import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import co.getdere.Interfaces.SharedViewModelImage
import co.getdere.Interfaces.SharedViewModelUser
import co.getdere.Models.Images
import co.getdere.Models.Users


import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenPhotoSocialBox : Fragment() {


    lateinit var imageObject: Images
    lateinit var sharedViewModelUser: SharedViewModelUser
    val currentUserUid = FirebaseAuth.getInstance().uid
    val refCurrentUser = FirebaseDatabase.getInstance().getReference("/users/$currentUserUid")

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            val sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            imageObject = sharedViewModelForImage.sharedImageObject

            sharedViewModelUser = ViewModelProviders.of(it).get(SharedViewModelUser::class.java)
            sharedViewModelUser.usersProfileId = imageObject.photographer
        }

        activity?.let {

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

        val refImageAuthor = FirebaseDatabase.getInstance().getReference("users/${imageObject.photographer}")


        refCurrentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)!!

                val currentUserImageUri = Uri.parse(currentUser.image)
                Picasso.get().load(currentUserImageUri).into(currentUserImage)

            }

        })



        refImageAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val imageAuthor = p0.getValue(Users::class.java)!!

                authorName.text = imageAuthor.name
                Picasso.get().load(Uri.parse(imageAuthor.image)).into(authorUserImage)
            }

        })

        authorName?.setOnClickListener {
            val action =
                ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToDestinationProfile()
            findNavController(nav_host_fragment).navigate(action)
        }

        addToBucket.setOnClickListener {

            if (imageObject.photographer == currentUserUid) {
                Toast.makeText(this.context, "You can't bucket your own photos", Toast.LENGTH_LONG).show()
            } else {
                val refCurrentUserBucket = FirebaseDatabase.getInstance().getReference("/buckets/$currentUserUid")

                refCurrentUserBucket.setValue(imageObject.id)
            }
        }


        val stampMills = imageObject.timestamp
        val pretty = PrettyTime()
        val date = pretty.format(Date(stampMills))
        imageTimestamp.text = date
    }


    companion object {
        fun newInstance(): OpenPhotoSocialBox = OpenPhotoSocialBox()
    }

}
