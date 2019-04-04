package co.getdere.Fragments


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.FeedActivity
import co.getdere.GroupieAdapters.FeedImage
import co.getdere.GroupieAdapters.LinearFeedImage
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.RegisterLogin.LoginActivity
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.bucket_roll.view.*
import kotlinx.android.synthetic.main.feed_single_photo.view.*


class ProfileLoggedInUserFragment : Fragment() {

    lateinit var userProfile: Users
    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    val galleryBucketAdapter = GroupAdapter<ViewHolder>()
    lateinit var bucketBtn: TextView
    lateinit var rollBtn: TextView

    lateinit var sharedViewModelForCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelImage: SharedViewModelImage



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

//        (activity as FeedActivity).mBottomNav.selectedItemId = R.id.destination_profile_logged_in_user

        return inflater.inflate(R.layout.fragment_profile_logged_in_user, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val profilePicture: ImageView = view.findViewById(R.id.profile_li_image)
        val profileName: TextView = view.findViewById(R.id.profile_li_user_name)
        val profileGallery: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.profile_li_gallery)
        val profileReputation = view.findViewById<TextView>(R.id.profile_li_reputation_count)
        val profileFollowers = view.findViewById<TextView>(R.id.profile_li_followers_count)
        val profilePhotos = view.findViewById<TextView>(R.id.profile_li_photos_count)
        val profileTagline = view.findViewById<TextView>(R.id.profile_li_tagline)
        val profileEditButton = view.findViewById<ImageButton>(R.id.profile_li_edit_profile_button)
        val instagramButton = view.findViewById<ImageButton>(R.id.profile_li_insta_icon)

        bucketBtn = view.findViewById(R.id.profile_li_bucket_btn)
        rollBtn = view.findViewById(R.id.profile_li_roll_btn)

        activity?.let {
            sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            userProfile = sharedViewModelForCurrentUser.currentUserObject

            Glide.with(this).load(userProfile.image).into(profilePicture)
            profileName.text = userProfile.name
            profileReputation.text = userProfile.reputation
            profileTagline.text = userProfile.tagline
        }


        val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")
        val followersRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/followers")


        photosRef.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                profilePhotos.text = p0.childrenCount.toString()
            }

        })

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                profileFollowers.text = p0.childrenCount.toString()

            }


        })


        val userRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("stax")) {
                    instagramButton.visibility = View.VISIBLE
                }
            }

        })

        setUpGalleryAdapter(profileGallery, 0)

        changeGalleryFeed("Roll")


        instagramButton.setOnClickListener {

            val userInstaRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/stax/instagram")
            userInstaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val instaLink = "https://www.instagram.com/${p0.value}"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instaLink)))
                }


            })

        }

        profileEditButton.setOnClickListener {

            val activity = activity as FeedActivity

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editProfileFragment).commit()
            activity.subActive = activity.editProfileFragment

            activity.switchVisibility(1)

//            val action =
//                ProfileLoggedInUserFragmentDirections.actionDestinationProfileLoggedInUserToEditProfileFragment(
//                    userProfile
//                )
//            findNavController().navigate(action)
        }


        bucketBtn.setOnClickListener {
            changeGalleryFeed("bucket")
            setUpGalleryAdapter(profileGallery, 1)

        }

        rollBtn.setOnClickListener {
            changeGalleryFeed("roll")
            setUpGalleryAdapter(profileGallery, 0)
        }


        activity!!.title = "Profile"

        setHasOptionsMenu(true)


        galleryRollAdapter.setOnItemClickListener { item, _ ->

            val activity = activity as FeedActivity

            if (activity.subActive != activity.imageFullSizeFragment) {

                activity.subFm.beginTransaction()
                    .add(R.id.feed_subcontents_frame_container, activity.imageFullSizeFragment, "imageFullSizeFragment")
                    .commit()
                activity.subActive = activity.imageFullSizeFragment
            }

            val image = item as FeedImage

            sharedViewModelImage.sharedImageObject.postValue(image.image)

            activity.switchVisibility(1)





//            val row = item as FeedImage
////            val imageId = row.image
//            val action =
//                ProfileLoggedInUserFragmentDirections.actionDestinationProfileLoggedInUserToDestinationImageFullSize(
//                    row.image.id,
//                    "ProfileActivity"
//                )
//
//            findNavController().navigate(action)

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

        if (source == "bucket") {
            rollBtn.setTextColor(resources.getColor(R.color.gray300))
            bucketBtn.setTextColor(resources.getColor(R.color.gray700))
            listenToImagesFromBucket()

        } else {
            rollBtn.setTextColor(resources.getColor(R.color.gray700))
            bucketBtn.setTextColor(resources.getColor(R.color.gray300))
            listenToImagesFromRoll()
        }

    }

    private fun setUpGalleryAdapter(gallery: androidx.recyclerview.widget.RecyclerView, type: Int) {


        if (type == 0) {
            gallery.adapter = galleryRollAdapter
            val galleryLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)
            gallery.layoutManager = galleryLayoutManager
        } else {
            gallery.adapter = galleryBucketAdapter
            val galleryLayoutManager = LinearLayoutManager(this.context)
            gallery.layoutManager = galleryLayoutManager
        }


    }


    private fun listenToImagesFromRoll() { //This needs to be fixed to not update in real time. Or should it?

        galleryRollAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val imagePath = p0.key

                val imageObjectPath =
                    FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")

                imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val imageObject = p0.getValue(Images::class.java)

                        galleryRollAdapter.add(FeedImage(imageObject!!))
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


    private fun listenToImagesFromBucket() { //This needs to be fixed to not update in real time. Or should it?

        galleryBucketAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/buckets")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                galleryBucketAdapter.add(SingleBucketRoll(p0, userProfile.uid))
//                val singleBucketFromDB = p0.getValue(SimpleString::class.java)


//                val refForImageObjects =
//                    FirebaseDatabase.getInstance().getReference("images/feed/${singleImageFromDBlink!!.singleString}")
//
//                refForImageObjects.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//                        val singleImageFromDB = p0.getValue(Images::class.java)
//
//                        if (singleImageFromDB != null) {
//
//                            galleryRollAdapter.add(FeedImage(singleImageFromDB))
//
//                        }
//                    }
//
//
//                })
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
        fun newInstance(): ProfileLoggedInUserFragment = ProfileLoggedInUserFragment()
    }


}

class SingleBucketRoll(val bucket: DataSnapshot, userId: String) : Item<ViewHolder>() {

    val imagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    var recyclerState = 0

    val refImages = FirebaseDatabase.getInstance().getReference("/users/$userId/buckets/${bucket.key.toString()}")

    override fun getLayout(): Int {
        return R.layout.bucket_roll
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val bucketName = viewHolder.itemView.bucket_roll_name
        bucketName.text = bucket.key.toString()

        val imagesRecycler = viewHolder.itemView.bucket_roll_image_recycler

        val imagesRecyclerLayoutManager =
            GridLayoutManager(viewHolder.root.context, 1, GridLayoutManager.HORIZONTAL, false)
        imagesRecycler.layoutManager = imagesRecyclerLayoutManager
        imagesRecycler.adapter = imagesRecyclerAdapter


        refImages.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                val imagePath = p0.key

                val imageObjectPath =
                    FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")

                imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val imageObject = p0.getValue(Images::class.java)

                        imagesRecyclerAdapter.add(SingleImageToBucketRoll(imageObject!!))

                    }

                })

            }


            override fun onChildRemoved(p0: DataSnapshot) {
            }


        })

        bucketName.setOnClickListener {
            if (recyclerState == 0) {
                val imagesRecyclerLayoutManager3 =
                    GridLayoutManager(viewHolder.root.context, 3, GridLayoutManager.HORIZONTAL, false)
                imagesRecycler.layoutManager = imagesRecyclerLayoutManager3
                imagesRecycler.animate().setDuration(2000)
                    .scaleX(3.0f)
                    .scaleY(3.0f)

            } else {
                val imagesRecyclerLayoutManager3 =
                    GridLayoutManager(viewHolder.root.context, 1, GridLayoutManager.HORIZONTAL, false)
                imagesRecycler.layoutManager = imagesRecyclerLayoutManager3
                imagesRecycler.layoutParams.height = 120

            }

        }









        imagesRecyclerAdapter.setOnItemClickListener { item, _ ->

            val singleImage = item as SingleImageToBucketRoll

            val action =
                ProfileLoggedInUserFragmentDirections.actionDestinationProfileLoggedInUserToDestinationImageFullSize(
                    singleImage.image.id,
                    "ProfileActivity"
                )
            viewHolder.root.findNavController().navigate(action)

        }

    }


}


class SingleImageToBucketRoll(val image: Images) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.bucket_row_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.root.context).load(image.imageSmall).into(viewHolder.itemView.feed_single_photo_photo)
    }


}