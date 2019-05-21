package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.adapters.BucketGalleryPagerAdapter
import co.getdere.models.Users
import co.getdere.otherClasses.SwipeLockableViewPager
import co.getdere.viewmodels.SharedViewModelCollection
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_collection_gallery.*


class CollectionGalleryFragment : Fragment() {

    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users

    lateinit var publish : TextView
    lateinit var mapButton: ImageButton
    lateinit var editButton: ImageButton
    lateinit var editableTitle : EditText
    lateinit var fixedTitle : TextView
    lateinit var galleryViewPager: SwipeLockableViewPager
    lateinit var pagerAdapter: BucketGalleryPagerAdapter

    var viewPagerPosition = 0

    lateinit var collection : DataSnapshot

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        publish = collection_gallery_publish
        editButton = collection_gallery_edit
        mapButton = collection_gallery_show_map
        editableTitle = collection_gallery_title_editable
        fixedTitle = collection_gallery_title

        galleryViewPager = collection_gallery_viewpager
        pagerAdapter = BucketGalleryPagerAdapter(childFragmentManager)
        galleryViewPager.adapter = pagerAdapter


        mapButton.setOnClickListener {
            switchImageAndMap()
        }

        editButton.setOnClickListener {
            switchEditableTitle()
        }

        activity?.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)



            sharedViewModelCollection.imageCollection.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { collectionSnapshot ->

                    collection = collectionSnapshot

                    galleryViewPager.currentItem = 0

                    if (collectionSnapshot.hasChild("body")) {
                        collection_gallery_title.text = collectionSnapshot.child("/body/title").value.toString()
                        collection_gallery_photos_count.text =
                            collectionSnapshot.child("/body/images").childrenCount.toString() + " photos"
                        publish.visibility = View.VISIBLE
                    } else {
                        collection_gallery_title.text = collectionSnapshot.key
                        collection_gallery_photos_count.text = collectionSnapshot.childrenCount.toString() + " photos"
                        publish.visibility = View.GONE
                    }
                }
            })
        }

        publish.setOnClickListener {

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

    private fun switchEditableTitle(){
        if (fixedTitle.visibility == View.VISIBLE){
            fixedTitle.visibility == View.GONE
            editableTitle.visibility == View.VISIBLE
            editButton.setImageResource(R.drawable.edit_active)

        } else {
            fixedTitle.visibility == View.VISIBLE
            editableTitle.visibility == View.GONE
            editButton.setImageResource(R.drawable.edit)

            if (publish.visibility == View.VISIBLE){
                FirebaseDatabase.getInstance().getReference("/itineraries/${collection.key}/body/title").setValue(editableTitle.text.toString())
            } else {
             FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/${collection.key}/body/title").setValue(editableTitle.text.toString())
            }

        }
    }


    companion object {
        fun newInstance(): CollectionGalleryFragment = CollectionGalleryFragment()
    }

}
