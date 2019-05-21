package co.getdere.fragments


import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.RegisterLoginActivity
import co.getdere.groupieAdapters.FeedImage
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Itineraries
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.SharingHelper
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle
import kotlinx.android.synthetic.main.collection_box_recycler.view.*
import kotlinx.android.synthetic.main.collection_box_single_photo.view.*
import kotlinx.android.synthetic.main.fragment_profile_logged_in_user.*


class ProfileLoggedInUserFragment : Fragment(), DereMethods {

    lateinit var userProfile: Users
    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    val galleryBucketAdapter = GroupAdapter<ViewHolder>()
    val galleryItineraryAdapter = GroupAdapter<ViewHolder>()
    private lateinit var bucketBtn: TextView
    private lateinit var rollBtn: TextView
    private lateinit var itineraryBtn: TextView

    private lateinit var sharedViewModelForCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    private lateinit var sharedViewModelCollection: SharedViewModelCollection
    private lateinit var sharedViewModelItinerary: SharedViewModelItinerary


    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    var imageList = mutableListOf<FeedImage>()

    lateinit var userRef: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_logged_in_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val picture: ImageView = profile_li_image
        val name: TextView = profile_li_user_name
        val galleryRollRecycler = profile_li_gallery_roll
        val galleryBucketsRecycler = profile_li_gallery_buckets
        val galleryItinerariesRecycler = profile_li_gallery_itineraries
        val reputation = profile_li_reputation_count
        val followers = profile_li_followers_count
        val photos = profile_li_photos_count
        val tagline = profile_li_tagline
        val editButton = profile_li_edit_profile_button
        val instagramButton = profile_li_insta_icon
        var instaLink = ""
        val userMapButton = profile_li_map

        val toolbar = profile_li_toolbar
        setHasOptionsMenu(true)
        toolbar.inflateMenu(R.menu.profile_navigation)

        toolbar.overflowIcon!!.setColorFilter(resources.getColor(R.color.gray500), PorterDuff.Mode.SRC_ATOP)

        toolbar.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.profile_edit_interests -> {
                    activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editInterestsFragment)
                        .commit()
                    activity.subActive = activity.editInterestsFragment
                    activity.switchVisibility(1)
                    return@setOnMenuItemClickListener true
                }

                R.id.profile_logout -> {

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                    val uid = FirebaseAuth.getInstance().uid
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(uid).addOnSuccessListener {
                        FirebaseAuth.getInstance().signOut()
                        GoogleSignIn.getClient(activity, gso).signOut().addOnSuccessListener {
                            val intent = Intent(this.context, RegisterLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }

                    return@setOnMenuItemClickListener true
                }

                R.id.profile_invite_a_friend -> {

                    val ss =
                        ShareSheetStyle(this.context!!, "Check out my profile", "Follow me around the world")
                            .setCopyUrlStyle(
                                resources.getDrawable(android.R.drawable.ic_menu_send),
                                "Copy",
                                "Added to clipboard"
                            )
                            .setMoreOptionStyle(resources.getDrawable(android.R.drawable.ic_menu_search), "Show more")
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                            .setAsFullWidthStyle(true)
                            .setSharingTitle("Share With")

                    buo.showShareSheet(activity, lp, ss, object : Branch.BranchLinkShareListener {
                        override fun onShareLinkDialogLaunched() {}
                        override fun onShareLinkDialogDismissed() {}
                        override fun onLinkShareResponse(
                            sharedLink: String,
                            sharedChannel: String,
                            error: BranchError
                        ) {
                        }

                        override fun onChannelSelected(channelName: String) {
                            val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                            firebaseAnalytics.logEvent("profile_shared_$channelName", null)
                        }


                    })

                    return@setOnMenuItemClickListener true


                }
                else -> return@setOnMenuItemClickListener false


            }


        }


        galleryRollRecycler.adapter = galleryRollAdapter
        val rollGalleryLayoutManager = GridLayoutManager(this.context, 3)
        galleryRollRecycler.layoutManager = rollGalleryLayoutManager

        galleryBucketsRecycler.adapter = galleryBucketAdapter
        val bucketsGalleryLayoutManager = GridLayoutManager(this.context, 2)
        galleryBucketsRecycler.layoutManager = bucketsGalleryLayoutManager

        galleryItinerariesRecycler.adapter = galleryItineraryAdapter
        val itinerariesGalleryLayoutManager = GridLayoutManager(this.context, 2)
        galleryItinerariesRecycler.layoutManager = itinerariesGalleryLayoutManager

        bucketBtn = profile_li_bucket_btn
        rollBtn = profile_li_roll_btn
        itineraryBtn = profile_li_itineraries_btn

        activity.let {
            sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)

            userProfile = sharedViewModelForCurrentUser.currentUserObject
            userRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}")

            listenToImagesFromBucket()
            listenToImagesFromRoll()
            listenToItineraries()
            if (userProfile.image.isNotEmpty()) {
                Glide.with(this).load(userProfile.image).into(picture)
            } else {
                Glide.with(this).load(R.drawable.user_profile).into(picture)
            }
            name.text = userProfile.name
            reputation.text = numberCalculation(userProfile.reputation)
            tagline.text = userProfile.tagline


            buo = BranchUniversalObject()
                .setCanonicalIdentifier(userProfile.uid)
                .setTitle("Get Dere and join traveler communities")
                .setContentDescription("")
                .setContentImageUrl(userProfile.image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(ContentMetadata().addCustomMetadata("type", "user"))

            lp = LinkProperties()
                .setFeature("inviting")
                .setCampaign("content 123 launch")
                .setStage("new user")

        }


        val photosRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/images")
        val followersRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/followers")


        photosRef.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                photos.text = numberCalculation(p0.childrenCount)
            }

        })

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                followers.text = numberCalculation(p0.childrenCount)
            }
        })



        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("stax")) {
                    instagramButton.visibility = View.VISIBLE
                    instaLink = "https://www.instagram.com/${p0.child("stax").child("instagram").value}"
                } else {
                    instagramButton.visibility = View.GONE
                }
            }

        })

        instagramButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instaLink)))
            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("instagram_clicked_self", null)
        }

        editButton.setOnClickListener {

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editProfileFragment).commit()
            activity.subActive = activity.editProfileFragment

            activity.switchVisibility(1)

        }


        bucketBtn.setOnClickListener {
            galleryItinerariesRecycler.visibility = View.GONE
            galleryRollRecycler.visibility = View.GONE
            galleryBucketsRecycler.visibility = View.VISIBLE
            changeGalleryFeed("collection")
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment).commit()
            activity.subActive = activity.collectionGalleryFragment
        }

        rollBtn.setOnClickListener {
            galleryItinerariesRecycler.visibility = View.GONE
            galleryRollRecycler.visibility = View.VISIBLE
            galleryBucketsRecycler.visibility = View.GONE
            changeGalleryFeed("roll")
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
            activity.subActive = activity.imageFullSizeFragment
        }

        itineraryBtn.setOnClickListener {
            galleryItinerariesRecycler.visibility = View.VISIBLE
            galleryRollRecycler.visibility = View.GONE
            galleryBucketsRecycler.visibility = View.GONE
            changeGalleryFeed("itinerary")
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment).commit()
            activity.subActive = activity.collectionGalleryFragment
        }


        galleryRollAdapter.setOnItemClickListener { item, _ ->
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
            activity.subActive = activity.imageFullSizeFragment

            val image = item as FeedImage
            sharedViewModelImage.sharedImageObject.postValue(image.image)
            sharedViewModelRandomUser.randomUserObject.postValue(userProfile)

            activity.switchVisibility(1)
        }


        galleryBucketAdapter.setOnItemClickListener { item, _ ->
            val bucket = item as SingleCollectionBox
            sharedViewModelCollection.imageCollection.postValue(bucket.collection)
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment).commit()
            activity.subActive = activity.collectionGalleryFragment
            activity.isBucketGalleryActive = true
            activity.switchVisibility(1)
        }

        userMapButton.setOnClickListener {
            userRef.child("images").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChildren()) {
                        sharedViewModelCollection.imageCollection.postValue(p0)
                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionMapView)
                            .commit()
                        activity.subActive = activity.collectionMapView
                        activity.switchVisibility(1)
                        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                        firebaseAnalytics.logEvent("map_checked_self", null)

                    } else {
                        Toast.makeText(activity, "You have no photos to view on the map", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }


    private fun changeGalleryFeed(source: String) {

        when (source) {
            "collection" -> {
                rollBtn.setTextColor(resources.getColor(R.color.gray300))
                bucketBtn.setTextColor(resources.getColor(R.color.gray700))
                itineraryBtn.setTextColor(resources.getColor(R.color.gray300))
            }
            "roll" -> {
                rollBtn.setTextColor(resources.getColor(R.color.gray700))
                bucketBtn.setTextColor(resources.getColor(R.color.gray300))
                itineraryBtn.setTextColor(resources.getColor(R.color.gray300))
            }
            else -> {
                rollBtn.setTextColor(resources.getColor(R.color.gray300))
                bucketBtn.setTextColor(resources.getColor(R.color.gray300))
                itineraryBtn.setTextColor(resources.getColor(R.color.gray700))
            }
        }

    }


    fun listenToImagesFromRoll() {

        Log.d("populatingRoll", "called")

        galleryRollAdapter.clear()

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

                            imageList.add(FeedImage(imageObject!!, 1))
                            galleryRollAdapter.clear()
                            galleryRollAdapter.addAll(imageList.reversed())
                        }
                    })

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


    fun listenToImagesFromBucket() {

        galleryBucketAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/buckets")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (bucket in p0.children) {
                    galleryBucketAdapter.add(
                        SingleCollectionBox(
                            bucket,
                            userProfile.uid,
                            activity as MainActivity,
                            "bucket"
                        )
                    )
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    fun listenToItineraries() {

        galleryItineraryAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}/itineraries")
        val itineraryRef = FirebaseDatabase.getInstance().getReference("/itineraries")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (path in p0.children) {
                    if (path != null) {
                        itineraryRef.child(path.key!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                galleryItineraryAdapter.add(
                                    SingleCollectionBox(
                                        p0,
                                        userProfile.uid,
                                        activity as MainActivity,
                                        "itinerary"
                                    )
                                )

                            }

                        })
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }


    companion object {
        fun newInstance(): ProfileLoggedInUserFragment = ProfileLoggedInUserFragment()
    }


}


class SingleCollectionBox(
    val collection: DataSnapshot,
    val userUid: String,
    val activity: MainActivity,
    val type: String
) :
    Item<ViewHolder>() {


    val imagesRecyclerAdapter = GroupAdapter<ViewHolder>()
    private val sharedViewModelBucket = ViewModelProviders.of(activity).get(SharedViewModelCollection::class.java)
    private val sharedViewModelItinerary = ViewModelProviders.of(activity).get(SharedViewModelItinerary::class.java)

    override fun getLayout(): Int {
        return R.layout.collection_box_recycler
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        val imagesRecycler = viewHolder.itemView.collection_box_recycler_recycler

        val imagesRecyclerLayoutManager =
            GridLayoutManager(viewHolder.root.context, 2, RecyclerView.VERTICAL, false)

        imagesRecycler.layoutManager = imagesRecyclerLayoutManager
        imagesRecycler.adapter = imagesRecyclerAdapter

        val bucketName = viewHolder.itemView.collection_box_recycler_name
        bucketName.text = if (type == "bucket") {
            collection.key
        } else {
            collection.child("body/title").getValue(String::class.java)
        }
        viewHolder.itemView.collection_box_recycler_photo_count.text = if (type == "bucket") {
            "${collection.childrenCount} photos"
        } else {
            "${collection.child("body/images").childrenCount} photos"
        }

        var count = 0

        if (type == "bucket") {


            for (imageSnapshot in collection.children) {

                if (count < 4) {


                    val imagePath = imageSnapshot.key

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

                    count += 1
                }

            }
        } else {

            for (imageSnapshot in collection.child("body/images").children) {

                if (count < 4) {

                    val imagePath = imageSnapshot.key
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

                    count += 1
                }

            }
        }



        imagesRecyclerAdapter.setOnItemClickListener { _, _ ->
            if (type == "bucket") {
                goToBucketGallery()
            } else {
                goToItineraryGallery()
            }
        }

        viewHolder.itemView.cardView.setOnClickListener {
            if (type == "bucket") {
                goToBucketGallery()
            } else {
                goToItineraryGallery()
            }
        }

        viewHolder.itemView.cardViewBackground.setOnClickListener {
            if (type == "bucket") {
                goToBucketGallery()
            } else {
                goToItineraryGallery()
            }
        }


    }

    private fun goToItinerary() {
        val itineraryRef = FirebaseDatabase.getInstance().getReference("/itineraries/${collection.key}/body")

        itineraryRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                sharedViewModelItinerary.itinerary.postValue(p0.getValue(Itineraries::class.java))

                activity.subFm.beginTransaction().hide(activity.subActive).add(R.id.feed_subcontents_frame_container, activity.itineraryFragment, "itineraryFragment").commit()
                activity.subActive = activity.itineraryFragment
                activity.isItineraryActive = true
                activity.switchVisibility(1)
            }

        })


    }


    private fun goToItineraryGallery() {
        sharedViewModelBucket.imageCollection.postValue(collection)
        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment).commit()
        activity.subActive = activity.collectionGalleryFragment
        activity.isBucketGalleryActive = true
        activity.switchVisibility(1)
    }

    private fun goToBucketGallery() {
        sharedViewModelBucket.imageCollection.postValue(collection)
        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment).commit()
        activity.subActive = activity.collectionGalleryFragment
        activity.isBucketGalleryActive = true
        activity.switchVisibility(1)
    }


}


class SingleImageToBucketRoll(val image: Images) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.collection_box_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.root.context).load(image.imageSmall).into(viewHolder.itemView.bucket_box_single_photo)
    }


}