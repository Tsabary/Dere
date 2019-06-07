package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_collection_gallery.*


class PurchasedItineraryGalleryFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings
    lateinit var sharedViewModelPurchasedItinerary: SharedViewModelPurchasedItinerary
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

    lateinit var purchasedItineraryObject: DataSnapshot

    var hasItineraryDataChanged = false
    var hasBucketDataChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        publish = collection_gallery_publish
        editButton = collection_gallery_edit
        mapButton = collection_gallery_show_map
        editableTitle = collection_gallery_title_editable
        fixedTitle = collection_gallery_title

        galleryViewPager = collection_gallery_viewpager
        val pagerAdapterBucket = FeedAndMapPagerAdapter(childFragmentManager)
        val pagerAdapterItinerary = ItineraryGalleryParentPagerAdapter(childFragmentManager)


        mapButton.setOnClickListener {
            switchImageAndMap()
        }


        editButton.setOnClickListener {
            switchEditableTitle()
        }

        activity.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelPurchasedItinerary = ViewModelProviders.of(it).get(SharedViewModelPurchasedItinerary::class.java)
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
            sharedViewModelItineraryDayStrings =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)


            sharedViewModelPurchasedItinerary.itinerary.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { collectionSnapshot ->

                    purchasedItineraryObject = collectionSnapshot

                    galleryViewPager.currentItem = 0

//                    fixedTitle.text = collectionSnapshot.child("title")
//                    editableTitle.setText(purchasedItineraryObject.title)
//                    collection_gallery_photos_count.text =
//                        purchasedItineraryObject.days.size.toString() + " days"

                    galleryViewPager.adapter = pagerAdapterItinerary
                    publish.visibility = View.GONE


                }
            })
        }


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

    private fun switchEditableTitle() {
        val activity = activity as MainActivity

        if (fixedTitle.visibility == View.VISIBLE) {
            fixedTitle.visibility = View.GONE
            editableTitle.visibility = View.VISIBLE
            editableTitle.requestFocus()
            editableTitle.setSelection(editableTitle.text.length)
            editButton.setImageResource(R.drawable.edit_active)
            showKeyboard(activity)
        } else {
            val newTitle = editableTitle.text.toString()
            fixedTitle.visibility = View.VISIBLE
            editableTitle.visibility = View.GONE
            editButton.setImageResource(R.drawable.edit)

            if (editableTitle.text.isNotEmpty()) {
                if (publish.visibility == View.VISIBLE) {
                    FirebaseDatabase.getInstance().getReference("/itineraries/${purchasedItineraryObject.key}/body/title")
                        .setValue(newTitle)
                    fixedTitle.text = newTitle
                    closeKeyboard(activity)
                    hasItineraryDataChanged = true
                } else {
                    FirebaseDatabase.getInstance()
                        .getReference("/users/${currentUser.uid}/buckets/${purchasedItineraryObject.key}/body/title")
                        .setValue(editableTitle.text.toString())
                    fixedTitle.text = newTitle
                    closeKeyboard(activity)
                    hasBucketDataChanged = true
                }
            } else {
                Toast.makeText(this.context, "Can't save an empty name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapButton.setImageResource(R.drawable.world)
        galleryViewPager.currentItem = 0
        if(hasItineraryDataChanged){
            (activity as MainActivity).profileLoggedInUserFragment.listenToItineraries()
            hasItineraryDataChanged = false
        } else {
            (activity as MainActivity).profileLoggedInUserFragment.listenToImagesFromBucket()
            hasBucketDataChanged = false
        }
    }


    companion object {
        fun newInstance(): PurchasedItineraryGalleryFragment = PurchasedItineraryGalleryFragment()
    }

}
