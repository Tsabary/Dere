package co.getdere.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.groupieAdapters.FeedImage
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.registerLogin.LoginActivity
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class ProfileRandomUserFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    private var userProfile = Users()
    lateinit var currentUser: Users

    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    val galleryBucketAdapter = GroupAdapter<ViewHolder>()
    private lateinit var bucketBtn: TextView
    private lateinit var rollBtn: TextView
    lateinit var followButton: TextView
    lateinit var profileGallery: RecyclerView

    lateinit var activityName: String


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject


        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val myView = inflater.inflate(R.layout.fragment_profile_random_user, container, false)

//        val profileTagline = myView.findViewById<TextView>(R.id.profile_ru_tagline)
//        val profilePicture: ImageView = myView.findViewById(R.id.profile_ru_image)
//        val profileName: TextView = myView.findViewById(R.id.profile_ru_user_name)
//        profileGallery = myView.findViewById(R.id.profile_ru_gallery)
//        val profileReputation = myView.findViewById<TextView>(R.id.profile_ru_reputation_count)
//        val profilePhotos = myView.findViewById<TextView>(R.id.profile_ru_photos_count)
//        val profileFollowers = myView.findViewById<TextView>(R.id.profile_ru_followers_count)
//        followButton = myView.findViewById(R.id.profile_ru_follow_button)
//
//
//
//        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
//            it?.let { user ->
//                userProfile = user
//                Glide.with(this).load(it.image).into(profilePicture)
//                profileName.text = it.name
//                profileReputation.text = it.reputation
//                profileTagline.text = it.tagline
//                setUpGalleryAdapter(profileGallery, 0)
//                changeGalleryFeed("Roll")
//
//                executeFollow(0, followButton)
//
//
//                val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")
//
//                photosRef.addValueEventListener(object : ValueEventListener {
//
//                    override fun onCancelled(p0: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//
//                        profilePhotos.text = p0.childrenCount.toString()
//
//                    }
//
//                })
//
//
//                val followersRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/followers")
//
//                followersRef.addValueEventListener(object : ValueEventListener {
//
//                    override fun onCancelled(p0: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//
//                        profileFollowers.text = p0.childrenCount.toString()
//
//                    }
//
//                })
//
//
//            }
//        }
//        )
//



        return myView

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        val profileTagline = view.findViewById<TextView>(R.id.profile_ru_tagline)
        val profilePicture: ImageView = view.findViewById(R.id.profile_ru_image)
        val profileName: TextView = view.findViewById(R.id.profile_ru_user_name)
        profileGallery = view.findViewById(R.id.profile_ru_gallery)
        val profileReputation = view.findViewById<TextView>(R.id.profile_ru_reputation_count)
        val profilePhotos = view.findViewById<TextView>(R.id.profile_ru_photos_count)
        val profileFollowers = view.findViewById<TextView>(R.id.profile_ru_followers_count)
        followButton = view.findViewById(R.id.profile_ru_follow_button)



        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                userProfile = user
                Glide.with(this).load(it.image).into(profilePicture)
                profileName.text = it.name
                profileReputation.text = it.reputation
                profileTagline.text = it.tagline
                setUpGalleryAdapter(profileGallery, 0)
                changeGalleryFeed("Roll")

                executeFollow(0, followButton)


                val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")

                photosRef.addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        profilePhotos.text = p0.childrenCount.toString()

                    }

                })


                val followersRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/followers")

                followersRef.addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        profileFollowers.text = p0.childrenCount.toString()

                    }

                })


            }
        }
        )















        bucketBtn = view.findViewById(R.id.profile_ru_bucket_btn)
        rollBtn = view.findViewById(R.id.profile_ru_roll_btn)






        followButton.setOnClickListener {
            executeFollow(1, followButton)
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



        }

    }

    private fun executeFollow(case: Int, followButton: TextView) {

        val followerRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/following")
        val beingFollowedRef = FirebaseDatabase.getInstance().getReference("users/${userProfile.uid}/followers")

        followerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(userProfile.uid)) {

                    if (case == 1) {

                        val followerRefThisAccount = FirebaseDatabase.getInstance()
                            .getReference("/users/${currentUser.uid}/following/${userProfile.uid}")
                        val beingFollowedRefThisAccount = FirebaseDatabase.getInstance()
                            .getReference("users/${userProfile.uid}/followers/${currentUser.uid}")

                        followerRefThisAccount.removeValue()
                        beingFollowedRefThisAccount.removeValue()

                        followButton.setBackgroundResource(R.drawable.follow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                        followButton.text = "Follow"

                    } else {
                        followButton.visibility = View.VISIBLE
                        followButton.setBackgroundResource(R.drawable.unfollow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray300))
                        followButton.text = "Unfollow"

                    }

                } else {

                    if (case == 1) {

                        followerRef.setValue(mapOf(userProfile.uid to true))
                        beingFollowedRef.setValue(mapOf(currentUser.uid to true))

                        followButton.setBackgroundResource(R.drawable.unfollow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray300))
                        followButton.text = "Unfollow"

                        changeReputation(20,userProfile.uid, userProfile.uid, currentUser.uid, currentUser.name, userProfile.uid, TextView(context), "follow")

                    } else {
                        followButton.visibility = View.VISIBLE
                        followButton.setBackgroundResource(R.drawable.follow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                        followButton.text = "Follow"

                    }

                }

            }


        })

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
            rollBtn.setTextColor(resources.getColor(R.color.gray500))
            bucketBtn.setTextColor(resources.getColor(R.color.gray700))
            listenToImagesFromBucket()

        } else {
            rollBtn.setTextColor(resources.getColor(R.color.gray700))
            bucketBtn.setTextColor(resources.getColor(R.color.gray500))
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


//                val singleImageFromDB = p0.getValue(Images::class.java)
//
//                if (singleImageFromDB != null) {
//
//                    galleryAdapter.add(FeedImage(singleImageFromDB))
//
//                }
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

                val singleImageFromDBlink = p0.key


                val refForImageObjects =
                    FirebaseDatabase.getInstance().getReference("/images/$singleImageFromDBlink/body")

                refForImageObjects.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val singleImageFromDB = p0.getValue(Images::class.java)

                        if (singleImageFromDB != null) {

                            galleryBucketAdapter.add(FeedImage(singleImageFromDB))

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