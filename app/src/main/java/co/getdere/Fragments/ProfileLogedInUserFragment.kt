package co.getdere.Fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.GroupieAdapters.FeedImage
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.Models.Images
import co.getdere.Models.SimpleString
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.RegisterLogin.LoginActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.bucket_roll.view.*
import kotlinx.android.synthetic.main.feed_single_photo.view.*


class ProfileLogedInUserFragment : Fragment() {


    lateinit var userProfile: Users
    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    val galleryBucketAdapter = GroupAdapter<ViewHolder>()
    lateinit var bucketBtn: TextView
    lateinit var rollBtn: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            val sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            userProfile = sharedViewModelForCurrentUser.currentUserObject
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_loged_in_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture: ImageView = view.findViewById(R.id.profile_li_image)
        val profileName: TextView = view.findViewById(R.id.profile_li_user_name)
        val profileGallery: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.profile_li_gallery)
        val profileReputation = view.findViewById<TextView>(R.id.profile_li_reputation_count)
        val profileFollowers = view.findViewById<TextView>(R.id.profile_li_followers_count)
        val profilePhotos = view.findViewById<TextView>(R.id.profile_li_photos_count)
        val profileTaglint = view.findViewById<TextView>(R.id.profile_li_tagline)
        val profileEditButton = view.findViewById<ImageButton>(R.id.profile_li_edit_profile_button)

        bucketBtn = view.findViewById(R.id.profile_li_bucket_btn)
        rollBtn = view.findViewById(R.id.profile_li_roll_btn)

        Glide.with(this).load(userProfile.image).into(profilePicture)
        profileName.text = userProfile.name
        profileReputation.text = userProfile.reputation
        profileTaglint.text = userProfile.tagline

        val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")

        photosRef.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var count = 0

                for (ds in p0.children) {
                    count += 1
                    profilePhotos.text = count.toString()
                }
            }

        })

        setUpGalleryAdapter(profileGallery, 0)

        changeGalleryFeed("Roll")


        profileEditButton.setOnClickListener {

            val action = ProfileLogedInUserFragmentDirections.actionDestinationProfileLogedInUserToEditProfileFragment(userProfile)
            findNavController().navigate(action)
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

            val row = item as FeedImage
//            val imageId = row.image
            val action =
                ProfileLogedInUserFragmentDirections.actionDestinationProfileLogedInUserToDestinationImageFullSize()
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

                val imagePath = p0.getValue(SimpleString::class.java)

                val imageObjectPath =
                    FirebaseDatabase.getInstance().getReference("/images/${imagePath!!.singleString}/body")

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

                Log.d("BucketGallery", p0.key)

                val getBucket = p0.getValue(SimpleString::class.java)
                Log.d("BucketGallery", getBucket!!.singleString)



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
        fun newInstance(): ProfileLogedInUserFragment = ProfileLogedInUserFragment()
    }


}

class SingleBucketRoll(val bucket: DataSnapshot, userId: String) : Item<ViewHolder>() {

    val imagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    val refImages = FirebaseDatabase.getInstance().getReference("/users/$userId/buckets/${bucket.key.toString()}")

    override fun getLayout(): Int {
        return R.layout.bucket_roll
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.bucket_roll_name.text = bucket.key.toString()

        val imagesRecycler = viewHolder.itemView.bucket_roll_image_recycler


        val imagesRecyclerLayoutManager =
            LinearLayoutManager(viewHolder.root.context, LinearLayoutManager.HORIZONTAL, true)
        imagesRecyclerLayoutManager.stackFromEnd = true

        imagesRecycler.layoutManager = imagesRecyclerLayoutManager
        imagesRecycler.adapter = imagesRecyclerAdapter


        refImages.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val imagePath = p0.getValue(SimpleString::class.java)

                val imageObjectPath =
                    FirebaseDatabase.getInstance().getReference("/images/${imagePath!!.singleString}/body")

                imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val imageObject = p0.getValue(Images::class.java)

                        imagesRecyclerAdapter.add(SingleImageToBucketRoll(imageObject!!))

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






        imagesRecyclerAdapter.setOnItemClickListener { item, _ ->

            val singleImage = item as SingleImageToBucketRoll

            val action =
                ProfileLogedInUserFragmentDirections.actionDestinationProfileLogedInUserToDestinationImageFullSize()
            action.imageId = singleImage.image.id
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