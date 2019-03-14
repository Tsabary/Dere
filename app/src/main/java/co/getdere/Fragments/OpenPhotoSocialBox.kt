package co.getdere.Fragments


import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment.findNavController
import co.getdere.Models.Users


import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenPhotoSocialBox : Fragment() {

    private val imageObject = (fragment as ImageFullSizeFragment).imageObject

    val currentUserUid = FirebaseAuth.getInstance().uid
    val refCurrentUser = FirebaseDatabase.getInstance().getReference("/users/$currentUserUid")
    lateinit var currentUserObject : Users

    lateinit var refImageAuthor: DatabaseReference
    lateinit var imageAuthorObject: Users


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_photo_social_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authorName = view.findViewById<TextView>(R.id.image_full_author_name_and_text)
        val currentUserImage = view.findViewById<ImageView>(R.id.image_full_current_user_image)
        val authorUserImage = view.findViewById<ImageView>(R.id.image_full_author_image)
        val imageTimestamp = view.findViewById<TextView>(R.id.image_full_timestamp)

        val stampMills = imageObject.timestamp
        val pretty = PrettyTime()
        val date = pretty.format(Date(stampMills))
        imageTimestamp.text = date

        authorName?.setOnClickListener {
            val action = ImageFullSizeFragmentDirections.actionDestinationImageFullSizeToDestinationProfile(imageAuthorObject.uid)
            findNavController(nav_host_fragment).navigate(action)
        }


        refImageAuthor = FirebaseDatabase.getInstance().getReference("users/${imageObject.photographer}")

        refImageAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val imageAuthor = p0.getValue(Users::class.java)!!

                setImageAuthorObjectFromListener(imageAuthor)

            }

        })

        refCurrentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)!!

                setCurrentUserFromListener(currentUser)

            }

        })

        authorName.text = imageAuthorObject.name
        Picasso.get().load(Uri.parse(imageAuthorObject.image)).into(authorUserImage)


        val currentUserImageUri = Uri.parse(currentUserObject.image)
        Picasso.get().load(currentUserImageUri).into(currentUserImage)

    }

    private fun setImageAuthorObjectFromListener(imageAuthor : Users){

        imageAuthorObject = imageAuthor

    }

    private fun setCurrentUserFromListener (currentUser : Users){

        currentUserObject = currentUser

    }

    companion object {
        fun newInstance(): OpenPhotoSocialBox = OpenPhotoSocialBox()
    }

}
