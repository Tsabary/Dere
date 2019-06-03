package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.CollectionPhoto
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_itinerary_days.*
import kotlinx.android.synthetic.main.itinerary_day.view.*

class ItineraryDaysFragment : Fragment() {

    lateinit var sharedViewModelItineraryDayImages: SharedViewModelItineraryDayImages
    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users
    lateinit var itineraryBody : ItineraryBody

    var myImageList = mutableListOf<MutableMap<String, Boolean>>()

    val daysAdapter = GroupAdapter<ViewHolder>()
    var startDay = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_days, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addDayBtn = itinerary_days_add_day
        val startDaySpinner = itinerary_days_spinner

        activity?.let {
            sharedViewModelItineraryDayImages =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayImages::class.java)
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)

            sharedViewModelCollection.imageCollection.observe(this, Observer { collection1 ->
                collection1?.let { collection2 ->
                    daysAdapter.clear()
                    myImageList.clear()

//                    itineraryBody = collection2.child("body").getValue(ItineraryBody::class.java)!!
//                    sharedViewModelItineraryDayImages.imageList.postValue(itineraryBody.days)

                }
            })



            sharedViewModelItineraryDayImages.imageList.observe(activity as MainActivity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList

                }
            })

        }

        startDaySpinner.setItems("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        startDaySpinner.setOnItemSelectedListener { _, position, _, _ ->
            startDay = position
        }

        addDayBtn.setOnClickListener {
            daysAdapter.add(SingleDay(activity as MainActivity, startDay + 1))
            myImageList.add(mutableMapOf())
            sharedViewModelItineraryDayImages.imageList.postValue(myImageList)
        }

        val galleryRecycler = itinerary_days_recycler

        val imagesRecyclerLayoutManager =
            LinearLayoutManager(this.context)

        galleryRecycler.adapter = daysAdapter
        galleryRecycler.layoutManager = imagesRecyclerLayoutManager

//        activity?.let {
//
//            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
//            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
//            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
//            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
//
//            sharedViewModelCollection.imageCollection.observe(this, Observer { bucketName ->
//                bucketName?.let { bucket ->
//                    daysAdapter.clear()
//
//
//                }
//            })
//
//        }


    }

    fun saveDays(){
        val itineraryDaysRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/days")
        itineraryDaysRef.setValue(myImageList)
    }

    override fun onPause() {
        super.onPause()
        saveDays()
    }

    companion object {
        fun newInstance(): ItineraryDaysFragment = ItineraryDaysFragment()
    }

    class SingleDay(val activity: MainActivity, private val startDay: Int) : Item<ViewHolder>() {

        lateinit var sharedViewModelItineraryImages: SharedViewModelItineraryDayImages
        val imagesAdapter = GroupAdapter<ViewHolder>()
//        val dayLocationsImages = mutableMapOf<String, Boolean>()

        override fun getLayout(): Int {
            return R.layout.itinerary_day
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            val imagesRecycler = viewHolder.itemView.itinerary_day_images_recycler
            imagesRecycler.adapter = imagesAdapter
            imagesRecycler.layoutManager = GridLayoutManager(viewHolder.root.context, 4)
            val addImagesBtn = viewHolder.itemView.itinerary_day_plus

            activity.let {
                sharedViewModelItineraryImages = ViewModelProviders.of(it).get(
                    SharedViewModelItineraryDayImages::class.java
                )

                sharedViewModelItineraryImages.imageList.observe(activity, Observer { it1 ->
                    it1?.let { existingImageList ->

                        imagesAdapter.clear()
//                        dayLocationsImages.clear()

                        val imagesRef = FirebaseDatabase.getInstance().getReference("/images")

                        if (existingImageList.size > position) {
                            imagesRecycler.visibility = View.VISIBLE

                            for (image in existingImageList[position]) {

                                imagesRef.child("${image.key}/body")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            val imageObject = p0.getValue(Images::class.java)
                                            if (imageObject != null) {
                                                imagesAdapter.add(
                                                    CollectionPhoto(
                                                        imageObject,
                                                        activity,
                                                        "itineraryDay",
                                                        position
                                                    )
                                                )
//                                                dayLocationsImages[imageObject.id] = true
                                            }
                                        }

                                    })


                            }
                        } else {
                            imagesRecycler.visibility = View.GONE
                        }
                    }
                })

            }

            addImagesBtn.setOnClickListener {
                activity.subFm.beginTransaction().add(
                    R.id.feed_subcontents_frame_container,
                    activity.addImagesToItineraryDayFragment,
                    "addImagesToItineraryDayFragment"
                ).addToBackStack("addImagesToItineraryDayFragment")
                    .commit()
                activity.addImagesToItineraryDayFragment.day = position
                activity.subActive = activity.addImagesToItineraryDayFragment
            }







            viewHolder.itemView.itinerary_day_number.text = (position + 1).toString()

            val weekday = (startDay + position).rem(7)

            viewHolder.itemView.itinerary_day_weekday.text = when (weekday) {

                1 -> {
                    ("(Monday)")
                }
                2 -> {
                    ("(Tuesday)")
                }
                3 -> {
                    ("(Wednesday)")
                }
                4 -> {
                    ("(Thursday)")
                }
                5 -> {
                    ("(Friday)")
                }
                6 -> {
                    ("(Saturday)")
                }

                0 -> {
                    ("(Sunday)")
                }

                else -> ("unknown")

            }

        }

    }

}
