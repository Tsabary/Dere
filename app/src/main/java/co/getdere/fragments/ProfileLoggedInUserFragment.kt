package co.getdere.fragments


import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.registerLogin.LoginActivity
import co.getdere.viewmodels.SharedViewModelBucket
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
import kotlinx.android.synthetic.main.bucket_box_recycler.view.*
import kotlinx.android.synthetic.main.bucket_box_single_photo.view.*
import kotlinx.android.synthetic.main.fragment_profile_logged_in_user.*


class ProfileLoggedInUserFragment : Fragment(), DereMethods {

    lateinit var userProfile: Users
    val galleryRollAdapter = GroupAdapter<ViewHolder>()
    val galleryBucketAdapter = GroupAdapter<ViewHolder>()
    lateinit var bucketBtn: TextView
    lateinit var rollBtn: TextView

    lateinit var sharedViewModelForCurrentUser: SharedViewModelCurrentUser
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelBucket: SharedViewModelBucket


    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_logged_in_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val profilePicture: ImageView = profile_li_image
        val profileName: TextView = profile_li_user_name
        val profileGalleryRoll = profile_li_gallery_roll
        val profileGalleryBuckets = profile_li_gallery_buckets
        val profileReputation = profile_li_reputation_count
        val profileFollowers = profile_li_followers_count
        val profilePhotos = profile_li_photos_count
        val profileTagline = profile_li_tagline
        val profileEditButton = profile_li_edit_profile_button
        val instagramButton = profile_li_insta_icon
        var instaLink = ""

        val toolbar = profile_li_toolbar
        setHasOptionsMenu(true)
        toolbar.inflateMenu(R.menu.profile_navigation)

        toolbar.overflowIcon!!.setColorFilter(resources.getColor(R.color.gray500), PorterDuff.Mode.SRC_ATOP)

        toolbar.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.profile_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this.context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    return@setOnMenuItemClickListener true
                }

                R.id.profile_invite_a_friend -> {


                    val ss =
                        ShareSheetStyle(this.context!!, "Check this out!", "Get Dere and start collecting destinations")
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

                    buo.showShareSheet(activity as MainActivity, lp, ss, object : Branch.BranchLinkShareListener {
                        override fun onShareLinkDialogLaunched() {}
                        override fun onShareLinkDialogDismissed() {}
                        override fun onLinkShareResponse(
                            sharedLink: String,
                            sharedChannel: String,
                            error: BranchError
                        ) {
                        }

                        override fun onChannelSelected(channelName: String) {}


                    })

                    return@setOnMenuItemClickListener true


                }
                else -> return@setOnMenuItemClickListener false


            }


        }


        profileGalleryRoll.adapter = galleryRollAdapter
        val rollGalleryLayoutManager = GridLayoutManager(this.context, 3)
        profileGalleryRoll.layoutManager = rollGalleryLayoutManager

        profileGalleryBuckets.adapter = galleryBucketAdapter
        val bucketsGalleryLayoutManager = GridLayoutManager(this.context, 2)
        profileGalleryBuckets.layoutManager = bucketsGalleryLayoutManager



        bucketBtn = view.findViewById(R.id.profile_li_bucket_btn)
        rollBtn = view.findViewById(R.id.profile_li_roll_btn)

        activity.let {
            sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelBucket = ViewModelProviders.of(it).get(SharedViewModelBucket::class.java)
            userProfile = sharedViewModelForCurrentUser.currentUserObject

            listenToImagesFromBucket()
            listenToImagesFromRoll()

            Glide.with(this).load(userProfile.image).into(profilePicture)
            profileName.text = userProfile.name
            profileReputation.text = numberCalculation(userProfile.reputation)
            profileTagline.text = userProfile.tagline


            buo = BranchUniversalObject()
                .setCanonicalIdentifier(userProfile.uid)
                .setTitle("Get Dere and join traveler communities")
                .setContentDescription("")
                .setContentImageUrl("https://img1.10bestmedia.com/Images/Photos/352450/GettyImages-913753556_55_660x440.jpg")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(ContentMetadata().addCustomMetadata("type", "invite"))

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

                profilePhotos.text = numberCalculation(p0.childrenCount)
            }

        })

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                profileFollowers.text = numberCalculation(p0.childrenCount)

            }


        })


        val userRef = FirebaseDatabase.getInstance().getReference("/users/${userProfile.uid}")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("stax")) {
                    instagramButton.visibility = View.VISIBLE
                    instaLink = "https://www.instagram.com/${p0.child("stax").child("instagram").value}"
                }
            }

        })

        instagramButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instaLink)))
        }

        profileEditButton.setOnClickListener {

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.editProfileFragment).commit()
            activity.subActive = activity.editProfileFragment

            activity.switchVisibility(1)

        }


        bucketBtn.setOnClickListener {
            profileGalleryRoll.visibility = View.GONE
            profileGalleryBuckets.visibility = View.VISIBLE
            changeGalleryFeed("bucket")
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketGalleryFragment).commit()
            activity.subActive = activity.bucketGalleryFragment
        }

        rollBtn.setOnClickListener {
            profileGalleryRoll.visibility = View.VISIBLE
            profileGalleryBuckets.visibility = View.GONE
            changeGalleryFeed("roll")
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
            activity.subActive = activity.imageFullSizeFragment
        }


        galleryRollAdapter.setOnItemClickListener { item, _ ->
            //            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
//            activity.subActive = activity.imageFullSizeFragment

            val image = item as FeedImage
            sharedViewModelImage.sharedImageObject.postValue(image.image)
            sharedViewModelRandomUser.randomUserObject.postValue(userProfile)

            activity.switchVisibility(1)
        }


        galleryBucketAdapter.setOnItemClickListener { item, _ ->
            val bucket = item as SingleBucketBox
            sharedViewModelBucket.sharedBucketId.postValue(bucket.bucket)
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketGalleryFragment).commit()
            activity.subActive = activity.bucketGalleryFragment
            activity.isBucketGalleryActive = true
            activity.switchVisibility(1)
        }

    }


    private fun changeGalleryFeed(source: String) {

        if (source == "bucket") {
            rollBtn.setTextColor(resources.getColor(R.color.gray300))
            bucketBtn.setTextColor(resources.getColor(R.color.gray700))
        } else {
            rollBtn.setTextColor(resources.getColor(R.color.gray700))
            bucketBtn.setTextColor(resources.getColor(R.color.gray300))
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

                galleryBucketAdapter.add(SingleBucketBox(p0!!, userProfile.uid, activity as MainActivity))

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


class SingleBucketBox(val bucket: DataSnapshot, val userUid: String, val activity: MainActivity) : Item<ViewHolder>() {


    val imagesRecyclerAdapter = GroupAdapter<ViewHolder>()
    private val sharedViewModelBucket = ViewModelProviders.of(activity).get(SharedViewModelBucket::class.java)

    override fun getLayout(): Int {
        return R.layout.bucket_box_recycler
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        val imagesRecycler = viewHolder.itemView.bucket_box_recycler_recycler

        val imagesRecyclerLayoutManager =
            GridLayoutManager(viewHolder.root.context, 2, RecyclerView.VERTICAL, false)

        imagesRecycler.layoutManager = imagesRecyclerLayoutManager
        imagesRecycler.adapter = imagesRecyclerAdapter

        val bucketName = viewHolder.itemView.bucket_box_recycler_name
        bucketName.text = bucket.key
        viewHolder.itemView.bucket_box_recycler_photo_count.text = "${bucket.childrenCount} photos"


        var count = 0

        for (i in bucket.children) {

            if (count < 4) {


                val imagePath = i.key

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



        imagesRecyclerAdapter.setOnItemClickListener { _, _ ->

            goToBucketGallery(viewHolder.root.context)
        }

        val card = viewHolder.itemView.cardView

        card.setOnClickListener {
            goToBucketGallery(viewHolder.root.context)
        }

        val cardBackground = viewHolder.itemView.cardViewBackground

        cardBackground.setOnClickListener {
            goToBucketGallery(viewHolder.root.context)
        }


    }

    fun goToBucketGallery(context : Context) {
        sharedViewModelBucket.sharedBucketId.postValue(bucket)
        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketGalleryFragment).commit()
        activity.subActive = activity.bucketGalleryFragment
        activity.isBucketGalleryActive = true
        activity.switchVisibility(1)

    }


}


class SingleImageToBucketRoll(val image: Images) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.bucket_box_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.root.context).load(image.imageSmall).into(viewHolder.itemView.bucket_box_single_photo)
    }


}