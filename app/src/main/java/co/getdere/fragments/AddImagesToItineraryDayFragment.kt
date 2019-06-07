package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.ImageSelector
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.SharedItineraryBody
import co.getdere.viewmodels.*
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_add_image_to_collection.*

class AddImagesToItineraryDayFragment : Fragment() {


    private lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings
    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    lateinit var sharedViewModelCollection: SharedViewModelCollection

    val galleryAdapter = GroupAdapter<ViewHolder>()
    var myImageList = mutableListOf<MutableMap<String, Boolean>>()
    lateinit var imagesRef : DatabaseReference

    private lateinit var itineraryObject: ItineraryBody
    private lateinit var purchasedItineraryObject: SharedItineraryBody

    var day = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_image_to_collection, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val galleryRecycler = add_image_to_collection_recycler
        galleryRecycler.adapter = galleryAdapter
        galleryRecycler.layoutManager = GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)

        activity.let {

            sharedViewModelItineraryDayStrings =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)
            sharedViewModelItineraryDayStrings.daysList.observe(activity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList
                }
            })

            sharedViewModelItinerary = ViewModelProviders.of(activity).get(SharedViewModelItinerary::class.java)
            sharedViewModelCollection = ViewModelProviders.of(activity).get(SharedViewModelCollection::class.java)
            sharedViewModelCollection.imageCollection.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { itinerary ->
                    galleryAdapter.clear()

                    if (itinerary.hasChild("body/contributors")){
                        purchasedItineraryObject = itinerary.child("body").getValue(SharedItineraryBody::class.java)!!
                        imagesRef = FirebaseDatabase.getInstance().getReference("/sharedItineraries/${purchasedItineraryObject.id}/body/images")
                    } else {
                        itineraryObject = itinerary.child("body").getValue(ItineraryBody::class.java)!!
                        imagesRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryObject.id}/body/images")
                    }

                    imagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            for (imagePath in p0.children) {

                                    FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val imageObject = p0.getValue(Images::class.java)
                                        if(imageObject != null){
                                            galleryAdapter.add(ImageSelector(imageObject, activity, "itineraryDay", day))
                                        }
                                    }
                                })
                            }
                        }
                    })
                }
            })
        }


        galleryAdapter.setOnItemClickListener { item, _ ->

            val image = item as ImageSelector
            if (myImageList.isNotEmpty()) {
                if (myImageList[day].contains(image.image.id)) {
                    myImageList[day].remove(image.image.id)
                    sharedViewModelItineraryDayStrings.daysList.postValue(myImageList)
                } else {
                    myImageList[day][image.image.id] = true
                    sharedViewModelItineraryDayStrings.daysList.postValue(myImageList)
                }
            } else {
                myImageList.add(day, mutableMapOf(image.image.id to true))
                sharedViewModelItineraryDayStrings.daysList.postValue(myImageList)
            }

            activity.subActive = activity.collectionGalleryFragment
            activity.subFm.popBackStack("addImagesToItineraryDayFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity.collectionGalleryFragment.hasItineraryDataChanged = true
        }
    }


    companion object {
        fun newInstance(): AddImagesToItineraryDayFragment = AddImagesToItineraryDayFragment()
    }
}
