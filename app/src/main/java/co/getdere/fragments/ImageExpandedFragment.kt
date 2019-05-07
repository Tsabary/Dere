package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.bumptech.glide.Glide
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
import kotlinx.android.synthetic.main.comment_layout.view.*
import kotlinx.android.synthetic.main.fragment_image_expanded.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ImageFullSizeFragment : androidx.fragment.app.Fragment(), DereMethods {

    lateinit var imageObject: Images
    lateinit var privacy: String

    lateinit var sharedViewModelForImage: SharedViewModelImage
    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users

    val commentsRecyclerAdapter = GroupAdapter<ViewHolder>()
    private val commentsRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

    lateinit var layoutScroll: NestedScrollView

    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    private var viewPagerPosition = 0
    private lateinit var imageExpendedViewPager: SwipeLockableViewPager

    lateinit var actionsContainer: ConstraintLayout
    lateinit var optionsContainer: ConstraintLayout
    lateinit var deleteContainer: ConstraintLayout
    lateinit var deleteEditContainer: ConstraintLayout

    lateinit var showLocation : ImageButton

    lateinit var bucketButton : ImageButton

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_expanded, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val authorName = image_expended_author_name
        val authorReputation = image_expended_author_reputation
        val authorImage = image_expended_author_image

        val currentUserImage = image_expended_comments_current_user_image

//        val mainImage = image_expended_image
        showLocation = image_expended_map
        val imageContent = image_expended_image_details
        val imageTimestamp = image_expended_timestamp


        val likeCount = image_expended_like_count
        val likeButton = image_expended_like_button
        val bucketCount = image_expended_bucket_count
        bucketButton = image_expended_bucket_icon
        val commentCount = image_expended_comment_count
        val shareButton = image_expended_share
        val linkIcon = image_expended_link_icon
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

        val commentsRecycler = image_expended_comments_recycler

        commentsRecycler.adapter = commentsRecyclerAdapter
        commentsRecycler.layoutManager = commentsRecyclerLayoutManager

        Glide.with(this).load(currentUser.image).into(currentUserImage)

        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                imageExpendedViewPager.currentItem = 0
                imageObject = image

                val imageRatioColonIndex = image.ratio.indexOf(":", 0)
                val imageRatioWidth = image.ratio.subSequence(0, imageRatioColonIndex)
                val imageRationHeight = image.ratio.subSequence(imageRatioColonIndex+1, image.ratio.length)

                val imageRatioFinal: Double = imageRatioWidth.toString().toDouble() / imageRationHeight.toString().toDouble()


                if (imageRatioFinal > 1.25) {
                    (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "5:4"
                } else if (imageRatioFinal < 0.8) {
                    (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "4:5"
                } else {
                    (imageExpendedViewPager.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = image.ratio
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
                    activity
                )

                listenToBucketCount(bucketCount, image)
                checkIfBucketed(bucketButton, image, currentUser.uid)

                listenToCommentCount(commentCount, image)

                imageTimestamp.text = PrettyTime().format(Date(image.timestampUpload))
                tags.text = image.tags.joinToString()

                if (image.details.isNotEmpty()) {
                    imageContent.visibility = View.VISIBLE
                    imageContent.text = image.details
                }

                if (image.link.isNotEmpty()) {
                    linkAddress.text = image.link
                    linkAddress.visibility = View.VISIBLE
//                    linkIcon.visibility = View.VISIBLE

                    linkAddress.setOnClickListener {
                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.webViewFragment)
                            .commit()
                        activity.subActive = activity.webViewFragment
                    }
                }

                if (image.photographer == currentUser.uid) {
                    optionsButton.visibility = View.VISIBLE

                    optionsButton.setOnClickListener {
                        optionsContainer.visibility = View.VISIBLE
                    }

                    optionsContainer.setOnClickListener {
                        optionsContainer.visibility = View.GONE
                    }

                    editButton.setOnClickListener {
                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imagePostEditFragment)
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
                        val imageRef = FirebaseDatabase.getInstance().getReference("/images/${image.id}")
                        imageRef.removeValue()
                        val imageAtUserRef =
                            FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/images/${image.id}")
                        imageAtUserRef.removeValue()
                    }

                } else {
                    optionsButton.visibility = View.GONE
                }



                actionsContainer.visibility = View.VISIBLE

                listenToComments(image)

                val contentDescription = if (image.details.isNotEmpty()) {
                    image.details
                } else {
                    "Save it. Get Dere."
                }

                sharedViewModelForRandomUser.randomUserObject.observe(this, Observer {
                    it?.let { user ->

                        authorName.text = user.name
                        authorReputation.text = "(${numberCalculation(user.reputation)})"
                        Glide.with(context!!).load(user.image).into(authorImage)


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

            val ss = ShareSheetStyle(activity, "Check this place out!", "Save it to your bucket list")
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
                override fun onChannelSelected(channelName: String) {}
            })
        }



        likeButton.setOnClickListener {

            if (currentUser.uid != imageObject.photographer){
                executeLike(
                    imageObject,
                    currentUser.uid,
                    likeCount,
                    likeButton,
                    1,
                    currentUser.name,
                    imageObject.photographer,
                    authorReputation,
                    activity
                )
            }
        }



        bucketButton.setOnClickListener {
            if (currentUser.uid != imageObject.photographer){
                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.bucketFragment).commit()
                activity.subActive = activity.bucketFragment
            }
        }

        postButton.setOnClickListener {
            val timestamp = System.currentTimeMillis()

            val comment =
                Comments(
                    currentUser.uid,
                    commentInput.text.toString(),
                    timestamp,
                    imageObject.id)

            commentInput.text.clear()

            val ref = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/").push()

            val commentBodyRef =
                FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/comments/${ref.key}/body")

            commentBodyRef.setValue(comment).addOnSuccessListener {

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

                    layoutScroll.fullScroll(View.FOCUS_DOWN)

                    Log.d("postCommentActivity", "Saved comment to Firebase Database")
                }.addOnFailureListener {
                    Log.d("postCommentActivity", "Failed to update image last interaction based on comment")
                }
            }.addOnFailureListener {
                Log.d("postCommentActivity", "Failed to save comment to database")
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

        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.profileRandomUserFragment).commit()
        activity.subActive = activity.profileRandomUserFragment

    }


    private fun listenToComments(image: Images) {

        commentsRecyclerAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                image_expended_comments_recycler.visibility = View.VISIBLE
                image_expended_divider.visibility = View.VISIBLE

                val singleCommentFromDB = p0.child("body").getValue(Comments::class.java)

                if (singleCommentFromDB != null) {
                    commentsRecyclerAdapter.add(
                        SingleComment(
                            singleCommentFromDB,
                            p0.key!!,
                            image,
                            currentUser,
                            activity as MainActivity
                        )
                    )
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


}


class SingleComment(
    var comment: Comments,
    val commentId: String,
    val image: Images,
    val currentUser: Users,
    val activity: Activity
) :
    Item<ViewHolder>(),
    DereMethods {

//    val uid =FirebaseAuth.getInstance().uid

    override fun getLayout(): Int {
        return R.layout.comment_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val commentContent = viewHolder.itemView.single_comment_content
        val commentTimestamp = viewHolder.itemView.single_comment_timestamp
        val commentLikeCount = viewHolder.itemView.single_comment_like_count
        val commentLikeButton = viewHolder.itemView.single_comment_like_button
        val commentAuthor = viewHolder.itemView.single_comment_author
        val commentEditButton = viewHolder.itemView.single_comment_edit
        val commentSaveButton = viewHolder.itemView.single_comment_save
        val commentDeleteButton = viewHolder.itemView.single_comment_delete
        val commentContentEditable = viewHolder.itemView.single_comment_content_editable
        val commentDeleteContainer =
            viewHolder.itemView.single_comment_delete_container
        val commentCancelButton = viewHolder.itemView.single_comment_cancel
        val commentRemoveButton = viewHolder.itemView.single_comment_remove

        val date = PrettyTime().format(Date(comment.timeStamp))

        commentContent.text = comment.content
        commentContentEditable.setText(comment.content)
        commentTimestamp.text = date

        listenToLikeCount(commentLikeCount)
        executeLike(0, commentLikeButton, commentLikeCount, image.id, currentUser, viewHolder, activity)


        commentLikeButton.setOnClickListener {

            if (currentUser.uid != comment.authorId) {
                executeLike(1, commentLikeButton, commentLikeCount, image.id, currentUser, viewHolder, activity)
            }
        }


        val refCommentUser = FirebaseDatabase.getInstance().getReference("/users/${comment.authorId}/profile")

        refCommentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                commentAuthor.text = user!!.name

                Glide.with(viewHolder.root.context).load(user.image).into(viewHolder.itemView.single_comment_photo)

            }

        })

        if (comment.authorId == currentUser.uid) {
            commentEditButton.visibility = View.VISIBLE
            commentDeleteButton.visibility = View.VISIBLE

            commentEditButton.setOnClickListener {
                commentEditButton.visibility = View.GONE
                commentSaveButton.visibility = View.VISIBLE
                commentContent.visibility = View.GONE
                commentContentEditable.visibility = View.VISIBLE
                commentContentEditable.requestFocus()
                commentContentEditable.setSelection(commentContentEditable.text.length)
            }

            commentSaveButton.setOnClickListener {
                val commentRef =
                    FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/$commentId/body/content")
                commentRef.setValue(commentContentEditable.text.toString())
                commentContent.text = commentContentEditable.text.toString()
                commentEditButton.visibility = View.VISIBLE
                commentSaveButton.visibility = View.GONE
                commentContent.visibility = View.VISIBLE
                commentContentEditable.visibility = View.GONE

                val newComment = Comments(
                    comment.authorId,
                    commentContentEditable.text.toString(),
                    comment.timeStamp,
                    comment.ImageId)
                comment = newComment
                closeKeyboard(activity)
            }

            commentDeleteButton.setOnClickListener {
                commentDeleteContainer.visibility = View.VISIBLE
            }

            commentCancelButton.setOnClickListener {
                commentDeleteContainer.visibility = View.GONE
            }

            commentRemoveButton.setOnClickListener {
                val commentRef = FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/$commentId")
                commentRef.removeValue()


                (activity as MainActivity).imageFullSizeFragment.commentsRecyclerAdapter.removeGroup(position)
                activity.imageFullSizeFragment.commentsRecyclerAdapter.notifyDataSetChanged()
                commentDeleteContainer.visibility = View.GONE
            }
        }


    }


    private fun executeLike(
        event: Int,
        likeButton: ImageButton,
        likeCount: TextView,
        imageId: String,
        currentUser: Users,
        viewHolder: ViewHolder,
        activity: Activity
    ) {
        val commentLikeRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(currentUser.uid)) {

                    if (event == 1) {
                        commentLikeRef.child(currentUser.uid).removeValue()

                        val likeCountNumber = likeCount.text.toString().toInt() - 1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart)

                        changeReputation(
                            13,
                            commentId,
                            imageId,
                            currentUser.uid,
                            currentUser.name,
                            comment.authorId,
                            TextView(viewHolder.root.context),
                            "photoCommentLike",
                            activity
                        )

                    } else {
                        likeButton.setImageResource(R.drawable.heart_active)
                    }
                } else {

                    if (event == 1) {
                        commentLikeRef.setValue(mapOf(currentUser.uid to 1))
                        val likeCountNumber = likeCount.text.toString().toInt() + 1
                        likeCount.text = likeCountNumber.toString()
                        likeButton.setImageResource(R.drawable.heart_active)

                        changeReputation(
                            12,
                            commentId,
                            imageId,
                            currentUser.uid,
                            currentUser.name,
                            comment.authorId,
                            TextView(viewHolder.root.context),
                            "photoCommentLike",
                            activity
                        )

                    } else {
                        likeButton.setImageResource(R.drawable.heart)
                    }
                }
            }
        })
    }

    private fun listenToLikeCount(commentLikeCount: TextView) {

        val commentLikeRef =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/$commentId/likes")

        commentLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                commentLikeCount.text = p0.childrenCount.toString()
            }
        })
    }
}
