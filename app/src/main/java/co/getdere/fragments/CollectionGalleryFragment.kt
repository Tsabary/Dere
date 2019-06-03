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
import co.getdere.adapters.BucketGalleryPagerAdapter
import co.getdere.adapters.ItineraryGalleryParentPagerAdapter
import co.getdere.interfaces.DereMethods
import co.getdere.models.Users
import co.getdere.otherClasses.SwipeLockableViewPager
import co.getdere.viewmodels.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_collection_gallery.*


class CollectionGalleryFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelItineraryDayImages: SharedViewModelItineraryDayImages
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
        val pagerAdapterBucket = BucketGalleryPagerAdapter(childFragmentManager)
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
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
            sharedViewModelItineraryDayImages =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayImages::class.java)


            sharedViewModelCollection.imageCollection.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { collectionSnapshot ->

                    collection = collectionSnapshot

                    if (collection.child("body/creator").value != currentUser.uid || collection.key == "AllBuckets") {
                        editButton.visibility = View.GONE
                    } else {
                        editButton.visibility = View.VISIBLE
                    }



                    galleryViewPager.currentItem = 0

                    fixedTitle.text = collectionSnapshot.child("/body/title").value.toString()
                    editableTitle.setText(collectionSnapshot.child("/body/title").value.toString())
                    collection_gallery_photos_count.text =
                        collectionSnapshot.child("/body/images").childrenCount.toString() + " photos"

                    if (collectionSnapshot.hasChild("body/locationId")) {
                        galleryViewPager.adapter = pagerAdapterItinerary

                        if (collectionSnapshot.child("body/creator").value == currentUser.uid) {
                            publish.visibility = View.VISIBLE
                        }

                    } else {
                        publish.visibility = View.GONE
                        galleryViewPager.adapter = pagerAdapterBucket

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


    }

    private fun switchImageAndMap() {

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
                    FirebaseDatabase.getInstance().getReference("/itineraries/${collection.key}/body/title")
                        .setValue(newTitle)
                    fixedTitle.text = newTitle
                    closeKeyboard(activity)
                } else {
                    FirebaseDatabase.getInstance()
                        .getReference("/users/${currentUser.uid}/buckets/${collection.key}/body/title")
                        .setValue(editableTitle.text.toString())
                    fixedTitle.text = newTitle
                    closeKeyboard(activity)

                }
            } else {
                Toast.makeText(this.context, "Can't save an empty name", Toast.LENGTH_SHORT).show()

            }


        }
    }


    companion object {
        fun newInstance(): CollectionGalleryFragment = CollectionGalleryFragment()
    }

}
