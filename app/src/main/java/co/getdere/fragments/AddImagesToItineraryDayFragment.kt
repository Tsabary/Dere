package co.getdere.fragments


import android.os.Bundle
import android.util.Log
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
import co.getdere.viewmodels.*
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_add_image_to_collection.*

class AddImagesToItineraryDayFragment : Fragment() {


    lateinit var sharedViewModelItineraryDayImages: SharedViewModelItineraryDayImages
    val galleryAdapter = GroupAdapter<ViewHolder>()
    var myImageList = mutableListOf<MutableMap<String, Boolean>>()

    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var itineraryObject: ItineraryBody

    var day = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_image_to_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity


        val galleryRecycler = add_image_to_collection_recycler
        galleryRecycler.adapter = galleryAdapter
        val imagesRecyclerLayoutManager =
            GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)
        galleryRecycler.layoutManager = imagesRecyclerLayoutManager

        activity.let {

            sharedViewModelItineraryDayImages =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayImages::class.java)
            sharedViewModelItineraryDayImages.imageList.observe(activity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList
                }
            })

            sharedViewModelItinerary = ViewModelProviders.of(activity).get(SharedViewModelItinerary::class.java)
            sharedViewModelCollection = ViewModelProviders.of(activity).get(SharedViewModelCollection::class.java)
            sharedViewModelCollection.imageCollection.observe(this, Observer {
                it?.let { itinerary ->
                    galleryAdapter.clear()
                    itineraryObject = itinerary.child("body").getValue(ItineraryBody::class.java)!!

                    val imagesRef =
                        FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryObject.id}/body/images")

                    imagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            for (imagePath in p0.children) {

                                val singleImageRef =
                                    FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")

                                singleImageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        val imageObject = p0.getValue(Images::class.java)
                                        galleryAdapter.add(ImageSelector(imageObject!!, activity))
                                    }
                                })
                            }
                        }
                    })
                }
            })
        }



        galleryAdapter.setOnItemClickListener { item, _ ->

            Log.d("checks", "day $day")

            val image = item as ImageSelector
            if (myImageList.isNotEmpty()) {
                if (myImageList[day].contains(image.image.id)) {
                    myImageList[day].remove(image.image.id)
                    sharedViewModelItineraryDayImages.imageList.postValue(myImageList)
                } else {
//                    myImageList[day] = (mutableMapOf(image.image.id to true))
                    myImageList[day][image.image.id] = true
                    sharedViewModelItineraryDayImages.imageList.postValue(myImageList)
                }
            } else {
                myImageList.add(day, mutableMapOf(image.image.id to true))

//                myImageList[day][image.image.id] = true
                sharedViewModelItineraryDayImages.imageList.postValue(myImageList)
            }

            activity.subActive = activity.collectionGalleryFragment
            activity.subFm.popBackStack("addImagesToItineraryDayFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }


    companion object {
        fun newInstance(): AddImagesToItineraryDayFragment = AddImagesToItineraryDayFragment()
    }
}
