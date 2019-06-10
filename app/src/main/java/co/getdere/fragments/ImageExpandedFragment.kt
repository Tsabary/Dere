package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.adapters.ImageExpandedPagerAdapter
import co.getdere.interfaces.DereMethods
import co.getdere.models.Comments
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.otherClasses.SwipeLockableViewPager
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import co.getdere.viewmodels.SharedViewModelSecondRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import kotlinx.android.synthetic.main.comment_layout.view.*
import kotlinx.android.synthetic.main.fragment_image_expanded.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment(), DereMethods {

    lateinit var imageObject: Images

    lateinit var sharedViewModelForImage: SharedViewModelImage
    private lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users
    lateinit var randomUser: Users

    val commentsRecyclerAdapter = GroupAdapter<ViewHolder>()

    lateinit var layoutScroll: NestedScrollView

    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    private var viewPagerPosition = 0
    private lateinit var imageExpendedViewPager: SwipeLockableViewPager

    lateinit var actionsContainer: ConstraintLayout
    lateinit var optionsContainer: ConstraintLayout
    lateinit var deleteContainer: ConstraintLayout
    lateinit var deleteEditContainer: ConstraintLayout

    lateinit var showLocation: ImageButton

    lateinit var collectButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_expanded, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        val authorName = image_expended_author_name
        val authorReputation = image_expended_author_reputation
        val authorImage = image_expended_author_image

        val currentUserImage = image_expended_comments_current_user_image

        showLocation = image_expended_map
        val imageContent = image_expended_image_details
        val imageTimestamp = image_expended_timestamp
        val likeCount = image_expended_like_count
        val likeButton = image_expended_like_button
        val bucketCount = image_expended_bucket_count
        collectButton = image_expended_bucket_icon
        val commentCount = image_expended_comment_count
        val shareButton = image_expended_share
        val linkAddress = image_expended_link_address
        val tags = image_expended_tags

        val optionsButton = image_expended_options_button
        optionsContainer = image_expended_options_background
        val editButton = image_expended_edit
        val deleteButton = image_expended_remove
        deleteEditContainer = image_expended_options_edit_delete
        deleteContainer = image_expended_options_delete_container
        val removeButton = image_expended_delete_remove_button
        val cancelButton = image_expended_delete_cancel_button

        val postButton = image_expended_comments_post_button
        val commentInput = image_expended_comments_comment_input

        imageExpendedViewPager = image_expended_viewpager
        val pagerAdapter = ImageExpandedPagerAdapter(childFragmentManager)
        imageExpendedViewPager.adapter = pagerAdapter

        actionsContainer = image_expended_actions_container

        layoutScroll = image_expended_scrollView

        val divider = image_expended_divider
        val commentsRecycler = image_expended_comments_recycler

        commentsRecycler.adapter = commentsRecyclerAdapter
        commentsRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

        Glide.with(this).load(currentUser.image).into(currentUserImage)

        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                imageExpendedViewPager.currentItem = 0
                imageObject = image

                val imageRatioColonIndex = image.ratio.indexOf(":", 0)
                val imageRatioWidth = image.ratio.subSequence(0, imageRatioColonIndex)
                val imageRationHeight = image.ratio.subSequence(imageRatioColonIndex + 1, image.ratio.length)
                val imageRatioFinal: Double =
                    imageRatioWidth.toString().toDouble() / imageRationHeight.toString().toDouble()

                when {
                    imageRatioFinal > 1.25 -> (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        "5:4"
                    imageRatioFinal < 0.8 -> (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        "4:5"
                    else -> (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        image.ratio
                }

                listenToLikeCount(likeCount, image)
                executeLike(
                    image,
                    currentUser.uid,
                    likeCount,
                    likeButton,
                    0,
                    currentUser.name,
                    image.photographer,
                    authorReputation,
                    activity,
                    "expanded"
                )

                listenToBucketCount(bucketCount, image)


                if (image.photographer == currentUser.uid) {
                    checkIfInItinerary(collectButton, image, currentUser.uid)
//                    bucketCount.visibility = View.GONE
                } else {
                    checkIfBucketed(collectButton, image, currentUser.uid)
                }

                listenToCommentCount(commentCount, image)

                imageTimestamp.text = PrettyTime().format(Date(image.timestampUpload))
                tags.text = image.tags.joinToString()

                if (image.details.isNotEmpty()) {
                    imageContent.visibility = View.VISIBLE
                    imageContent.text = image.details
                } else {
                    imageContent.visibility = View.GONE
                }

                if (image.link.isNotEmpty()) {
                    linkAddress.text = image.link
                    linkAddress.visibility = View.VISIBLE
//                    linkIcon.visibility = View.VISIBLE

                    linkAddress.setOnClickListener {
                        activity.subFm.beginTransaction()
                            .add(R.id.feed_subcontents_frame_container, activity.webViewFragment, "webViewFragment")
                            .addToBackStack("webViewFragment").commit()
                        activity.subActive = activity.webViewFragment
                    }
                } else {
                    linkAddress.visibility = View.GONE
                }

                if (image.photographer == currentUser.uid || currentUser.uid == "hQ3KL1zqpsZIhY38IpSRW2G0wXJ2") {
                    optionsButton.visibility = View.VISIBLE

                    optionsButton.setOnClickListener {
                        optionsContainer.visibility = View.VISIBLE
                    }

                    optionsContainer.setOnClickListener {
                        optionsContainer.visibility = View.GONE
                    }

                    editButton.setOnClickListener {
                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.imagePostEditFragment,
                            "imagePostEditFragment"
                        ).addToBackStack("imagePostEditFragment")
                            .commit()
                        activity.subActive = activity.imagePostEditFragment
                        optionsContainer.visibility = View.GONE
                        deleteEditContainer.visibility = View.VISIBLE
                        deleteContainer.visibility = View.GONE
                    }

                    deleteButton.setOnClickListener {
                        deleteEditContainer.visibility = View.GONE
                        deleteContainer.visibility = View.VISIBLE
                    }

                    cancelButton.setOnClickListener {
                        deleteEditContainer.visibility = View.VISIBLE
                        deleteContainer.visibility = View.GONE
                    }

                    removeButton.setOnClickListener {

                        for (tag in image.tags) {
                            FirebaseDatabase.getInstance().getReference("/tags/$tag/${image.id}").removeValue()
                        }

                        FirebaseDatabase.getInstance().getReference("/images/${image.id}").removeValue()
                        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/images/${image.id}")
                            .removeValue()

                        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                        firebaseAnalytics.logEvent("image_removed", null)

                        activity.switchVisibility(0)

                        optionsContainer.visibility = View.GONE
                        deleteEditContainer.visibility = View.VISIBLE
                        deleteContainer.visibility = View.GONE

//                        activity.fm.beginTransaction().detach(activity.profileLoggedInUserFragment)
//                            .attach(activity.profileLoggedInUserFragment).commit()
                        activity.profileLoggedInUserFragment.listenToImagesFromRoll()
                    }
                } else {
                    optionsButton.visibility = View.GONE
                }

                actionsContainer.visibility = View.VISIBLE

                listenToImageComments(image, commentsRecyclerAdapter, commentsRecycler, divider, currentUser, activity)

                val contentDescription = if (image.details.isNotEmpty()) {
                    image.details
                } else {
                    "Save it. Get Dere."
                }

                sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
                    it?.let { user ->
                        randomUser = user
                        authorName.text = user.name
                        authorReputation.text = "(${numberCalculation(user.reputation)})"

                        Glide.with(context!!).load(
                            if (user.image.isNotEmpty()) {
                                user.image
                            } else {
                                R.drawable.user_profile
                            }
                        ).into(authorImage)

                        buo = BranchUniversalObject()
                            .setCanonicalIdentifier(image.id)
                            .setTitle("Photograph by ${user.name}")
                            .setContentDescription(contentDescription)
                            .setContentImageUrl(image.imageBig)
                            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                            .setContentMetadata(ContentMetadata().addCustomMetadata("type", "image"))

                        lp = LinkProperties()
                            .setFeature("sharing")
                            .setCampaign("content 123 launch")
                            .setStage("new user")
                    }
                })

            }
        })


        showLocation.setOnClickListener {
            switchImageAndMap()
        }


        shareButton.setOnClickListener {

            val ss = ShareSheetStyle(activity, "Check this place out!", "Save it to your purchasedItineraryObject list")
                .setCopyUrlStyle(resources.getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
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
                override fun onLinkShareResponse(sharedLink: String, sharedChannel: String, error: BranchError) {}
                override fun onChannelSelected(channelName: String) {
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                    firebaseAnalytics.logEvent("image_shared_$channelName", null)
                }
            })
        }


        likeButton.setOnClickListener {
            if (currentUser.uid != imageObject.photographer) {
                executeLike(
                    imageObject,
                    currentUser.uid,
                    likeCount,
                    likeButton,
                    1,
                    currentUser.name,
                    imageObject.photographer,
                    authorReputation,
                    activity,
                    "expanded"
                )
            }
        }

        collectButton.setOnClickListener {
            if (currentUser.uid != imageObject.photographer) {
                activity.subFm.beginTransaction()
                    .add(
                        R.id.feed_subcontents_frame_container,
                        activity.bucketAndSharedItineraryPagerFragment,
                        "bucketAndSharedItineraryPagerFragment"
                    ).addToBackStack("bucketAndSharedItineraryPagerFragment")
                    .commit()
                activity.subActive = activity.bucketAndSharedItineraryPagerFragment
            } else {
                activity.subFm.beginTransaction()
                    .add(
                        R.id.feed_subcontents_frame_container,
                        activity.addToItineraryFragment,
                        "addToItineraryFragment"
                    ).addToBackStack("addToItineraryFragment").commit()
                activity.subActive = activity.addToItineraryFragment
            }
        }

        postButton.setOnClickListener {
            if (commentInput.text.isNotEmpty()) {
                val ref =
                    FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/").push()

                val timestamp = System.currentTimeMillis()

                val comment =
                    Comments(
                        currentUser.uid,
                        commentInput.text.toString(),
                        timestamp,
                        imageObject.id,
                        ref.key!!
                    )

                commentInput.text.clear()

                ref.child("body").setValue(comment).addOnSuccessListener {

                    val refImageLastInteraction = FirebaseDatabase.getInstance()
                        .getReference("/images/${imageObject.id}/body/lastInteraction")

                    refImageLastInteraction.setValue(timestamp).addOnSuccessListener {
                        sendNotification(
                            3,
                            16,
                            currentUser.uid,
                            currentUser.name,
                            imageObject.id,
                            ref.key!!,
                            imageObject.photographer
                        )

                        closeKeyboard(activity)
                        listenToImageComments(
                            imageObject,
                            commentsRecyclerAdapter,
                            commentsRecycler,
                            divider,
                            currentUser,
                            activity
                        )
                        layoutScroll.fullScroll(View.FOCUS_DOWN)

                        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                        firebaseAnalytics.logEvent("image_comment_added", null)
                    }
                }
            }
        }

        authorName.setOnClickListener {
            goToRandomUserProfile()
        }

        authorImage.setOnClickListener {
            goToRandomUserProfile()
        }

        authorReputation.setOnClickListener {
            goToRandomUserProfile()
        }
    }

    private fun switchImageAndMap() {
        if (viewPagerPosition == 0) {
            imageExpendedViewPager.currentItem = 1
            viewPagerPosition = 1
            showLocation.setImageResource(R.drawable.location_map)
        } else {
            imageExpendedViewPager.currentItem = 0
            viewPagerPosition = 0
            showLocation.setImageResource(R.drawable.location)
        }
    }

    private fun goToRandomUserProfile() {
        val activity = activity as MainActivity

        if (currentUser.uid != randomUser.uid) {

            activity.subFm.beginTransaction().add(
                R.id.feed_subcontents_frame_container,
                activity.profileRandomUserFragment,
                "profileRandomUserFragment"
            ).addToBackStack("profileRandomUserFragment").commit()
            activity.subActive = activity.profileRandomUserFragment

            activity.isRandomUserProfileActive = true
        } else {
            activity.navigateToProfile()
        }
    }

    override fun onPause() {
        super.onPause()
        collectButton.setImageResource(R.drawable.bucket)
    }

    override fun onResume() {
        super.onResume()
        layoutScroll.fullScroll(View.FOCUS_UP)
    }
}


class SingleComment(
    var comment: Comments,
    val image: Images,
    val currentUser: Users,
    val activity: MainActivity
) :
    Item<ViewHolder>(),
    DereMethods {

    lateinit var sharedViewModelSecondRandomUser: SharedViewModelSecondRandomUser

    private val commentUserRef = FirebaseDatabase.getInstance().getReference("/users/${comment.authorId}/profile")

    override fun getLayout(): Int = R.layout.comment_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelSecondRandomUser = ViewModelProviders.of(it).get(SharedViewModelSecondRandomUser::class.java)
        }

        val content = viewHolder.itemView.single_comment_content
        val timestamp = viewHolder.itemView.single_comment_timestamp
        val likeCount = viewHolder.itemView.single_comment_like_count
        val likeButton = viewHolder.itemView.single_comment_like_button
        val author = viewHolder.itemView.single_comment_author
        val authorImage = viewHolder.itemView.single_comment_photo
        val authorReputation = viewHolder.itemView.single_comment_author_reputation
        val editButton = viewHolder.itemView.single_comment_edit
        val saveButton = viewHolder.itemView.single_comment_save
        val deleteButton = viewHolder.itemView.single_comment_delete
        val contentEditable = viewHolder.itemView.single_comment_content_editable
        val deleteContainer = viewHolder.itemView.single_comment_delete_container
        val cancelButton = viewHolder.itemView.single_comment_cancel
        val removeButton = viewHolder.itemView.single_comment_remove

        val date = PrettyTime().format(Date(comment.timeStamp))

        content.text = comment.content
        contentEditable.setText(comment.content)
        timestamp.text = date

        listenToCommentLikeCount(likeCount)

        executeCommentLike(
            image,
            currentUser.uid,
            likeCount,
            likeButton,
            0,
            currentUser.name,
            image.photographer,
            authorReputation,
            activity,
            comment
        )

        likeButton.setOnClickListener {

            if (currentUser.uid != comment.authorId) {
                executeCommentLike(
                    image,
                    currentUser.uid,
                    likeCount,
                    likeButton,
                    1,
                    currentUser.name,
                    image.photographer,
                    authorReputation,
                    activity,
                    comment
                )
            }
        }

        authorImage.setOnClickListener {
            goToProfile()
        }
        author.setOnClickListener {
            goToProfile()
        }
        authorReputation.setOnClickListener {
            goToProfile()
        }

        commentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                if (user != null) {
                    author.text = user.name
                    authorReputation.text = "(${user.reputation})"

                    Glide.with(viewHolder.root.context).load(
                        if (user.image.isNotEmpty()) {
                            user.image
                        } else {
                            R.drawable.user_profile
                        }
                    ).into(authorImage)
                }
            }
        })

        if (comment.authorId == currentUser.uid) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE

            editButton.setOnClickListener {
                editButton.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                content.visibility = View.GONE
                contentEditable.visibility = View.VISIBLE
                contentEditable.requestFocus()
                contentEditable.setSelection(contentEditable.text.length)
            }

            saveButton.setOnClickListener {
                FirebaseDatabase.getInstance()
                    .getReference("/images/${image.id}/comments/${comment.id}/body/content")
                    .setValue(contentEditable.text.toString())
                content.text = contentEditable.text.toString()
                editButton.visibility = View.VISIBLE
                saveButton.visibility = View.GONE
                content.visibility = View.VISIBLE
                contentEditable.visibility = View.GONE

                val newComment = Comments(
                    comment.authorId,
                    contentEditable.text.toString(),
                    comment.timeStamp,
                    comment.ImageId,
                    comment.id
                )
                comment = newComment
                closeKeyboard(activity)
            }

            deleteButton.setOnClickListener {
                deleteContainer.visibility = View.VISIBLE
            }

            cancelButton.setOnClickListener {
                deleteContainer.visibility = View.GONE
            }

            removeButton.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/${comment.id}").removeValue()

                activity.imageFullSizeFragment.commentsRecyclerAdapter.removeGroup(position)
                activity.imageFullSizeFragment.commentsRecyclerAdapter.notifyDataSetChanged()
                deleteContainer.visibility = View.GONE

                val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)
                firebaseAnalytics.logEvent("image_comment_removed", null)
            }
        }
    }

    private fun goToProfile() {
        if (currentUser.uid != comment.authorId) {
            commentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    sharedViewModelSecondRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                    activity.subFm.beginTransaction()
                        .add(
                            R.id.feed_subcontents_frame_container,
                            activity.profileSecondRandomUserFragment,
                            "profileSecondRandomUserFragment"
                        ).addToBackStack("profileSecondRandomUserFragment")
                        .commit()
                    activity.subActive = activity.profileSecondRandomUserFragment
                    activity.isSecondRandomUserProfileActive = true
                }
            })
        } else {
            activity.navigateToProfile()
        }
    }

    private fun listenToCommentLikeCount(commentLikeCount: TextView) {

        FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/${comment.id}/likes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    commentLikeCount.text = numberCalculation(p0.childrenCount)
                }
            })
    }
}
