package co.getdere.Fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import co.getdere.MainActivity
import co.getdere.Models.FeedImage
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.RegisterLogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    var userProfile: Users? = null
    val galleryAdapter = GroupAdapter<ViewHolder>()
    lateinit var bucketBtn : TextView
    lateinit var rollBtn : TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_profile, container, false)

        val profilePicture: ImageView = myView.findViewById(R.id.profile_image)
        val logoutBtn: Button = myView.findViewById<Button>(R.id.logout_btn)
        val profileName: TextView = myView.findViewById(R.id.profile_user_name)
        val profileGallery: RecyclerView = myView.findViewById(R.id.profile_gallery)
        bucketBtn = myView.findViewById(R.id.profile_bucket_btn)
        rollBtn = myView.findViewById(R.id.profile_roll_btn)

        userProfile = mainActivity.currentUser  // setup who's profile is that based on the value given to currentUser


        setUpGalleryAdapter(profileGallery)


        logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this.context, LoginActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        bucketBtn.setOnClickListener {
            changeGalleryFeed("bucket")
        }

        rollBtn.setOnClickListener {
            changeGalleryFeed("roll")
        }


        val uri = userProfile?.image
        Picasso.get().load(uri).into(profilePicture)
        profileName.text = userProfile?.name

        return myView
    }


    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    private fun changeGalleryFeed(source: String) {

        if (source == "bucket") {
            rollBtn.setTextColor(resources.getColor(R.color.gray500))
            bucketBtn.setTextColor(resources.getColor(R.color.gray700))

        } else {
            rollBtn.setTextColor(resources.getColor(R.color.gray700))
            bucketBtn.setTextColor(resources.getColor(R.color.gray500))
        }

    }

    private fun setUpGalleryAdapter(gallery: RecyclerView) {

        gallery.adapter = galleryAdapter
        val galleryLayoutManager = GridLayoutManager(this.context, 3)
        gallery.layoutManager = galleryLayoutManager

        val dummyUri =
            "https://firebasestorage.googleapis.com/v0/b/dere-3d530.appspot.com/o/20150923_100950.jpg?alt=media&token=97f4b02c-75d9-4d5d-bc86-a3ffaa3a0011"

        val imageUri = Uri.parse(dummyUri)
        if (imageUri != null) {
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))


        }
    }


}