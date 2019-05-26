package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.ImageSelector
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_add_image_to_collection.*

class AddImagesToItineraryFragment : Fragment() {


    lateinit var sharedViewModelItineraryImages: SharedViewModelItineraryImages
    lateinit var currentUser: Users
    val galleryAdapter = GroupAdapter<ViewHolder>()
    var myImageList = mutableListOf<Images>()

    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    lateinit var itineraryObject: ItineraryBody

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
        activity.let {
            sharedViewModelItinerary = ViewModelProviders.of(activity).get(SharedViewModelItinerary::class.java)
            sharedViewModelItinerary.itinerary.observe(this, Observer {
                it?.let { itinerary ->
                    itineraryObject = itinerary.child("body").getValue(ItineraryBody::class.java)!!

                    val imagesRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryObject.id}/body/images")

                    imagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            for (imagePath in p0.children) {

                                val singleImageRef = FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")

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

        val galleryRecycler = add_image_to_answer_recycler

        val imagesRecyclerLayoutManager =
            GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)

        galleryRecycler.adapter = galleryAdapter
        galleryRecycler.layoutManager = imagesRecyclerLayoutManager


        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelItineraryImages = ViewModelProviders.of(it).get(SharedViewModelItineraryImages::class.java)
            sharedViewModelItineraryImages.imageList.observe(activity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList
                }
            })
        }



        galleryAdapter.setOnItemClickListener { item, _ ->

            val image = item as ImageSelector

            if (!myImageList.contains(image.image)) {
                myImageList.add(image.image)
                sharedViewModelItineraryImages.imageList.postValue(myImageList)
            } else {
                myImageList.remove(image.image)
                sharedViewModelItineraryImages.imageList.postValue(myImageList)
            }

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.itineraryEditFragment)
                .commit()
            activity.subActive = activity.itineraryEditFragment
        }
    }


    companion object {
        fun newInstance(): AddImagesToItineraryFragment = AddImagesToItineraryFragment()
    }
}
