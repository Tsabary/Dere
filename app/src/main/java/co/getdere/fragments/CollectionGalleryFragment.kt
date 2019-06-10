package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.adapters.FeedAndMapPagerAdapter
import co.getdere.adapters.ItineraryGalleryParentPagerAdapter
import co.getdere.interfaces.DereMethods
import co.getdere.models.Users
import co.getdere.otherClasses.SwipeLockableViewPager
import co.getdere.viewmodels.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.SharingHelper
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle
import kotlinx.android.synthetic.main.fragment_collection_gallery.*


class CollectionGalleryFragment : Fragment(), DereMethods {
    val dummyPhoto =
        "https://images.unsplash.com/photo-1469827160215-9d29e96e72f4?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1000&q=60"
    lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings
    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users

    lateinit var publish: TextView
    lateinit var mapButton: ImageButton
    lateinit var editButton: ImageButton
    lateinit var editableTitle: EditText
    lateinit var fixedTitle: TextView
    lateinit var galleryViewPager: SwipeLockableViewPager

    var viewPagerPosition = 0

    lateinit var collection: DataSnapshot

    var hasItineraryDataChanged = false
    var hasBucketDataChanged = false

    var itineraryStartDay = MutableLiveData<Int>()

    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_collection_gallery, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        publish = collection_gallery_publish
        editButton = collection_gallery_edit
        mapButton = collection_gallery_show_map
        editableTitle = collection_gallery_title_editable
        fixedTitle = collection_gallery_title
        val shareButton = collection_gallery_share

        galleryViewPager = collection_gallery_viewpager
        val pagerAdapterBucket = FeedAndMapPagerAdapter(childFragmentManager)
        val pagerAdapterItinerary = ItineraryGalleryParentPagerAdapter(childFragmentManager)


        activity.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
            sharedViewModelItineraryDayStrings =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            sharedViewModelCollection.imageCollection.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { collectionSnapshot ->

                    collection = collectionSnapshot

                    galleryViewPager.currentItem = 0

                    fixedTitle.text = collectionSnapshot.child("/body/title").value.toString()
                    editableTitle.setText(collectionSnapshot.child("/body/title").value.toString())
                    collection_gallery_photos_count.text =
                        "${collectionSnapshot.child("/body/days").childrenCount} days"

                    if (collectionSnapshot.hasChild("/body/contributors")) {
                        mapButton.visibility = View.GONE
                        shareButton.visibility = View.VISIBLE
                        galleryViewPager.adapter = pagerAdapterItinerary
                        galleryViewPager.offscreenPageLimit = 2

                        collection_gallery_photos_count.text =
                            "${collectionSnapshot.child("/body/days").childrenCount} days"

                        if (!collectionSnapshot.child("/body/contributors").hasChild(currentUser.uid)) {
                            FirebaseDatabase.getInstance()
                                .getReference("sharedItineraries/${collectionSnapshot.key}/body/contributors/${currentUser.uid}")
                                .setValue(true).addOnSuccessListener {
                                    FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/sharedItineraries/${collectionSnapshot.key}").setValue(true).addOnSuccessListener {
                                        activity.marketplacePurchasedFragment.listenToItineraries()
                                    }
                                }
                        }

//                        oneFor@for(imagePath in collection.child("/body/images").children){
//
//                        }

                        buo = BranchUniversalObject()
                            .setCanonicalIdentifier(collection.key!!)
                            .setTitle("Itinerary by ${currentUser.name}")
                            .setContentDescription(collection.child("/body/title").value.toString())
                            .setContentImageUrl(dummyPhoto)
                            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                            .setContentMetadata(ContentMetadata().addCustomMetadata("type", "sharedItinerary"))

                        lp = LinkProperties()
                            .setFeature("sharing")
                            .setCampaign("content 123 launch")
                            .setStage("new user")


                    } else if (collectionSnapshot.hasChild("body/locationId")) {
                        mapButton.visibility = View.GONE
                        shareButton.visibility = View.GONE
                        galleryViewPager.adapter = pagerAdapterItinerary
                        galleryViewPager.offscreenPageLimit = 6

                        collection_gallery_photos_count.text =
                            "${collectionSnapshot.child("/body/days").childrenCount} days"

                        if (collectionSnapshot.child("body/creator").value == currentUser.uid) {
                            publish.visibility = View.VISIBLE
                        }

                    } else {
                        mapButton.visibility = View.VISIBLE
                        shareButton.visibility = View.GONE
                        publish.visibility = View.GONE
                        galleryViewPager.adapter = pagerAdapterBucket

                        collection_gallery_photos_count.text =
                            "${collectionSnapshot.child("/body/images").childrenCount} places"

                    }

                    publish.setOnClickListener {

                        sharedViewModelItinerary.itinerary.postValue(collectionSnapshot)

                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.itineraryEditFragment,
                            "itineraryEditFragment"
                        ).addToBackStack("itineraryEditFragment").commit()
                        activity.subActive = activity.itineraryEditFragment
                    }
                }
            })
        }

        mapButton.setOnClickListener {
            switchImageAndMap()
        }

        editableTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (editableTitle.text.toString() != collection.child("/body/title").value.toString()) {
                    editButton.visibility = View.VISIBLE
                }
            }
        })

        editButton.setOnClickListener {

            val newTitle = editableTitle.text.toString()

            if (editableTitle.text.isNotEmpty()) {
                if (collection.hasChild("body/contributors")) {
                    FirebaseDatabase.getInstance().getReference("/sharedItineraries/${collection.key}/body/title")
                        .setValue(newTitle)
                } else if (collection.hasChild("body/locationId")) {
                    FirebaseDatabase.getInstance().getReference("/itineraries/${collection.key}/body/title")
                        .setValue(newTitle)
                } else {
                    FirebaseDatabase.getInstance()
                        .getReference("/users/${currentUser.uid}/buckets/${collection.key}/body/title")
                        .setValue(editableTitle.text.toString())
                }
                fixedTitle.text = newTitle
                hasItineraryDataChanged = true
                editButton.visibility = View.GONE
                editableTitle.clearFocus()
                activity.dismissKeyboard()
            } else {
                Toast.makeText(this.context, "Can't save an empty name", Toast.LENGTH_SHORT).show()
            }
        }



        shareButton.setOnClickListener {

            val ss = ShareSheetStyle(activity, "Itinerary invitation", "Join this itinerary and help me plan our trip")
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

    }

    fun Context.dismissKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val windowToken = (this as? Activity)?.currentFocus?.windowToken
        windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
    }


    fun switchImageAndMap() {

        if (viewPagerPosition == 0) {
            galleryViewPager.currentItem = 1
            viewPagerPosition = 1
            mapButton.setImageResource(R.drawable.world_active)

        } else {
            galleryViewPager.currentItem = 0
            viewPagerPosition = 0
            mapButton.setImageResource(R.drawable.world)
        }
    }

    override fun onPause() {
        super.onPause()
        mapButton.setImageResource(R.drawable.world)
        galleryViewPager.currentItem = 0
        if (hasItineraryDataChanged) {
            (activity as MainActivity).profileLoggedInUserFragment.listenToItineraries()
            hasItineraryDataChanged = false
        } else {
            (activity as MainActivity).profileLoggedInUserFragment.listenToImagesFromBucket()
            hasBucketDataChanged = false
        }
        editButton.visibility = View.GONE
    }

    companion object {
        fun newInstance(): CollectionGalleryFragment = CollectionGalleryFragment()
    }
}
