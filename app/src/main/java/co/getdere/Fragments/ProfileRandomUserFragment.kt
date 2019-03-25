package co.getdere.Fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.GroupieAdapters.FeedImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import co.getdere.Models.Images
import co.getdere.Models.SimpleString
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.RegisterLogin.LoginActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class ProfileRandomUserFragment : Fragment() {

    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    private var userProfile = Users()

    val galleryAdapter = GroupAdapter<ViewHolder>()
    private lateinit var bucketBtn: TextView
    private lateinit var rollBtn: TextView


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_random_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture: ImageView = view.findViewById(R.id.profile_ru_image)
        val profileName: TextView = view.findViewById(R.id.profile_ru_user_name)
        val profileGallery: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.profile_ru_gallery)
        bucketBtn = view.findViewById(R.id.profile_ru_bucket_btn)
        rollBtn = view.findViewById(R.id.profile_ru_roll_btn)


        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let {user ->
                userProfile = user
                Glide.with(this).load(it.image).into(profilePicture)
                profileName.text = it.name
                setUpGalleryAdapter(profileGallery)
                changeGalleryFeed("Bucket")
            }
        }
        )


        bucketBtn.setOnClickListener {
            changeGalleryFeed("bucket")
        }

        rollBtn.setOnClickListener {
            changeGalleryFeed("roll")
        }

        activity!!.title = "Profile"

        setHasOptionsMenu(true)


        galleryAdapter.setOnItemClickListener { item, _ ->

            val row = item as FeedImage
//            val imageId = row.image
            val action = ProfileRandomUserFragmentDirections.actionDestinationProfileRandomUserToDestinationImageFullSize()
            action.imageId = row.image.id
            findNavController().navigate(action)

        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.profile_logout -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this.context, LoginActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun changeGalleryFeed(source: String) {

        Log.d("CHekingSequence",  userProfile.email)

        if (source == "bucket") {
            rollBtn.setTextColor(resources.getColor(R.color.gray500))
            bucketBtn.setTextColor(resources.getColor(R.color.gray700))
            listenToImagesFromBucket()

        } else {
            rollBtn.setTextColor(resources.getColor(R.color.gray700))
            bucketBtn.setTextColor(resources.getColor(R.color.gray500))
            listenToImagesFromRoll()
        }
    }

    private fun setUpGalleryAdapter(gallery: androidx.recyclerview.widget.RecyclerView) {

        Log.d("CHekingSequence", userProfile.uid)


        gallery.adapter = galleryAdapter
        val galleryLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 4)
        gallery.layoutManager = galleryLayoutManager
    }


    private fun listenToImagesFromRoll() { //This needs to be fixed to not update in real time. Or should it?

        galleryAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/images/byuser/${userProfile.uid}")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleImageFromDB = p0.getValue(Images::class.java)

                if (singleImageFromDB != null) {

                    galleryAdapter.add(FeedImage(singleImageFromDB))

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


    private fun listenToImagesFromBucket() { //This needs to be fixed to not update in real time. Or should it?

        galleryAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/buckets/users/${userProfile.uid}")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleImageFromDBlink = p0.getValue(SimpleString::class.java)


                val refForImageObjects =
                    FirebaseDatabase.getInstance().getReference("images/feed/${singleImageFromDBlink!!.singleString}")

                refForImageObjects.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val singleImageFromDB = p0.getValue(Images::class.java)

                        if (singleImageFromDB != null) {

                            galleryAdapter.add(FeedImage(singleImageFromDB))

                        }
                    }


                })
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


    companion object {
        fun newInstance(): ProfileRandomUserFragment = ProfileRandomUserFragment()
    }


}