package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.groupieAdapters.FeedImage
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.*
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile_random_user.*


class ProfileRandomUserFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelSecondImage: SharedViewModelSecondImage
    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser
    private lateinit var sharedViewModelForSecondRandomUser: SharedViewModelSecondRandomUser
    private lateinit var sharedViewModelCollection: SharedViewModelCollection

    lateinit var userProfile: Users
    lateinit var currentUser: Users
    lateinit var userRef: DatabaseReference

    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    lateinit var followButton: TextView
    lateinit var profileGallery: RecyclerView

    var imageList = mutableListOf<FeedImage>()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelSecondImage = ViewModelProviders.of(it).get(SharedViewModelSecondImage::class.java)
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelForSecondRandomUser =
                ViewModelProviders.of(it).get(SharedViewModelSecondRandomUser::class.java)
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_random_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val profileTagline = profile_ru_tagline
        val profilePicture: ImageView = profile_ru_image
        val profileName: TextView = profile_ru_user_name
        val instagramButton = profile_ru_insta_icon
        profileGallery = profile_ru_gallery
        val profileReputation = profile_ru_reputation_count
        val profilePhotos = profile_ru_photos_count
        val profileFollowers = profile_ru_followers_count
        followButton = profile_ru_follow_button
        var instaLink = ""
        val userMapButton = profile_ru_map


        instagramButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instaLink)))
            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("instagram_clicked_other_user", null)
        }


        sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                userProfile = user


                userRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}")

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.child("stax/instagram").exists()) {
                            instagramButton.visibility = View.VISIBLE
                            instaLink = "https://www.instagram.com/${p0.child("stax").child("instagram").value}"
                        } else {
                            instagramButton.visibility = View.GONE
                        }
                    }

                })


                if (it.image.isNotEmpty()) {
                    Glide.with(this).load(it.image).into(profilePicture)
                } else {
                    Glide.with(this).load(R.drawable.user_profile).into(profilePicture)
                }

                profileName.text = it.name
                profileReputation.text = numberCalculation(it.reputation)
                profileTagline.text = it.tagline

                galleryRollAdapter.clear()
                listenToImagesFromRoll()

                executeFollow(0, followButton, activity)


                val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")

                photosRef.addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        profilePhotos.text = numberCalculation(p0.childrenCount)
                    }
                })

                val followersRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/followers")

                followersRef.addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        profileFollowers.text = numberCalculation(p0.childrenCount)
                    }
                })

                galleryRollAdapter.setOnItemClickListener { item, view ->
                    val image = item as FeedImage

                    sharedViewModelSecondImage.sharedSecondImageObject.postValue(image.image)
                    sharedViewModelForSecondRandomUser.randomUserObject.postValue(userProfile)

                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.secondImageFullSizeFragment,
                        "secondImageFullSizeFragment"
                    ).addToBackStack("secondImageFullSizeFragment")
                        .commit()
                    activity.subActive = activity.secondImageFullSizeFragment
                }
            }
        }
        )


        followButton.setOnClickListener {
            executeFollow(1, followButton, activity)
        }


        userMapButton.setOnClickListener {
            activity.isRandomUserProfileActive = true
            userRef.child("images").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChildren()) {
                        sharedViewModelCollection.imageCollection.postValue(p0)
                        activity.subFm.beginTransaction()
                            .add(R.id.feed_subcontents_frame_container, activity.collectionMapView, "collectionMapView")
                            .addToBackStack("collectionMapView")
                            .commit()
                        activity.subActive = activity.collectionMapView
                        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                        firebaseAnalytics.logEvent("map_checked_user", null)
                    } else {
                        Toast.makeText(activity, "User has no photos to view on the map", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun executeFollow(case: Int, followButton: TextView, activity: Activity) {

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
//                        followButton.tag = "unfollowed"

                    } else {
                        followButton.setBackgroundResource(R.drawable.unfollow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray300))
                        followButton.text = "Unfollow"
//                        followButton.tag = "followed"

                    }

                } else {

                    if (case == 1) {

                        followerRef.child(userProfile.uid).setValue(true)
                        beingFollowedRef.child(currentUser.uid).setValue(true)

                        followButton.setBackgroundResource(R.drawable.unfollow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray300))
                        followButton.text = "Unfollow"

                        changeReputation(
                            20,
                            userProfile.uid,
                            userProfile.uid,
                            currentUser.uid,
                            currentUser.name,
                            userProfile.uid,
                            TextView(context),
                            "follow",
                            activity
                        )

                    } else {
                        followButton.setBackgroundResource(R.drawable.follow_button)
                        followButton.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                        followButton.text = "Follow"
                    }
                }
            }
        })
    }


    private fun listenToImagesFromRoll() {

        profileGallery.adapter = galleryRollAdapter
        profileGallery.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)

        galleryRollAdapter.clear()
        imageList.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                for (imagePath in p0.children) {

                    val imageObjectPath =
                        FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")

                    imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val imageObject = p0.getValue(Images::class.java)
                            if (imageObject != null) {
                                if (!imageObject.private) {
                                    imageList.add(FeedImage(imageObject, 1))
                                    galleryRollAdapter.clear()
                                    galleryRollAdapter.addAll(imageList.reversed())
                                }
                            }
                        }
                    })
                }
            }

//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//
//                val imagePath = p0.key
//
//                val imageObjectPath =
//                    FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")
//
//                imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//                        val imageObject = p0.getValue(Images::class.java)
//
//                        galleryRollAdapter.add(FeedImage(imageObject!!, 0))
//                    }
//                })
//            }

            override fun onCancelled(p0: DatabaseError) {

            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//            }

        })
    }


    companion object {
        fun newInstance(): ProfileRandomUserFragment = ProfileRandomUserFragment()
    }


}