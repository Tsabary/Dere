package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.CollectionPhoto
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.SharedItineraryBody
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
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

    lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings
    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users
    private lateinit var itineraryBody: ItineraryBody
    lateinit var sharedItineraryBody: SharedItineraryBody
    lateinit var scrollLayout: NestedScrollView

    var myImageList = mutableListOf<MutableMap<String, Boolean>>()
    private val fillerList = mutableListOf<MutableMap<String, Boolean>>()

    private val daysAdapter = GroupAdapter<ViewHolder>()
    var startDay = 0

    var isShared = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_days, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        val addDayBtn = itinerary_days_add_day
        val startDaySpinner = itinerary_days_spinner
        scrollLayout = itinerary_days_scroll_layout

        val galleryRecycler = itinerary_days_recycler
        galleryRecycler.adapter = daysAdapter
        galleryRecycler.layoutManager = LinearLayoutManager(this.context)

        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelItineraryDayStrings =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)

            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)

            sharedViewModelCollection.imageCollection.observe(this, Observer { collection1 ->
                collection1?.let { collection2 ->
                    if (collection2.hasChild("body")) {
                        daysAdapter.clear()
                        myImageList.clear()
                        fillerList.clear()

                        if (collection2.hasChild("body/contributors")) {
                            isShared = 1
                            sharedItineraryBody =
                                collection2.child("body").getValue(SharedItineraryBody::class.java)!!
                            startDaySpinner.selectedIndex = sharedItineraryBody.startDay
                            startDay = sharedItineraryBody.startDay

                            for (map in sharedItineraryBody.days) {
                                if (map.isNullOrEmpty()) {
                                    fillerList.add(mutableMapOf())
                                } else {
                                    fillerList.add(map.toMutableMap())
                                }
                            }

                            sharedViewModelItineraryDayStrings.daysList.postValue(fillerList)

                            populateDays(activity)
                        } else {
                            isShared = 0
                            itineraryBody = collection2.child("body").getValue(ItineraryBody::class.java)!!
                            startDaySpinner.selectedIndex = itineraryBody.startDay
                            startDay = itineraryBody.startDay

                            for (map in itineraryBody.days) {
                                if (map.isNullOrEmpty()) {
                                    fillerList.add(mutableMapOf())
                                } else {
                                    fillerList.add(map.toMutableMap())
                                }
                            }

                            sharedViewModelItineraryDayStrings.daysList.postValue(fillerList)

                            populateDays(activity)
                        }
                    }
                }
            })

            activity.collectionGalleryFragment.itineraryStartDay.observe(this, Observer { liveData ->
                liveData?.let { liveStartDay ->
                    startDay = liveStartDay
                    populateDays(activity)
                }
            })

            sharedViewModelItineraryDayStrings.daysList.observe(activity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList
                }
            })
        }

        startDaySpinner.setItems(
            "Select day",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
        )

        startDaySpinner.setOnItemSelectedListener { _, position, _, _ ->
            activity.collectionGalleryFragment.itineraryStartDay.postValue(position)
            activity.collectionGalleryFragment.hasItineraryDataChanged = true
        }

        addDayBtn.setOnClickListener {
            daysAdapter.add(SingleDay(activity, startDay + 1, currentUser.uid))
            myImageList.add(mutableMapOf())
            sharedViewModelItineraryDayStrings.daysList.postValue(myImageList)
            activity.collectionGalleryFragment.hasItineraryDataChanged = true
            scrollLayout.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun populateDays(activity: MainActivity) {
        daysAdapter.clear()
        for (day in fillerList) {
            if(isShared==0){
                daysAdapter.add(SingleDay(activity, startDay, currentUser.uid))
            } else {
                daysAdapter.add(SingleDay(activity, startDay, currentUser.uid))
            }
        }
    }

    private fun saveDays() {
        val itineraryDaysRef = if (isShared == 0) {
            FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}/body")
        } else {
            FirebaseDatabase.getInstance().getReference("/sharedItineraries/${sharedItineraryBody.id}/body")
        }
        itineraryDaysRef.child("days").setValue(myImageList)
        itineraryDaysRef.child("startDay").setValue(startDay)
    }

    override fun onResume() {
        super.onResume()
        scrollLayout.fullScroll(View.FOCUS_DOWN)
    }

    override fun onPause() {
        super.onPause()
        saveDays()
    }

    companion object {
        fun newInstance(): ItineraryDaysFragment = ItineraryDaysFragment()
    }
}

class SingleDay(
    val activity: MainActivity,
    private val startDay: Int,
    val uid: String
) :
    Item<ViewHolder>() {

    private var sharedViewModelImage = ViewModelProviders.of(activity).get(
        SharedViewModelImage::class.java
    )

    private var sharedViewModelRandomUser = ViewModelProviders.of(activity).get(
        SharedViewModelRandomUser::class.java
    )

    private var sharedViewModelItineraryImages = ViewModelProviders.of(activity).get(
        SharedViewModelItineraryDayStrings::class.java
    )

    private val sharedViewModelDayCollection =
        ViewModelProviders.of(activity).get(SharedViewModelDayCollection::class.java)

    val imagesAdapter = GroupAdapter<ViewHolder>()

    override fun getLayout(): Int {
        return R.layout.itinerary_day
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val moveUp = viewHolder.itemView.itinerary_day_move_up
        val moveDown = viewHolder.itemView.itinerary_day_move_down
        val addImagesBtn = viewHolder.itemView.itinerary_day_plus
        val mapBtn = viewHolder.itemView.itinerary_day_map

        val imagesRecycler = viewHolder.itemView.itinerary_day_images_recycler
        imagesRecycler.adapter = imagesAdapter
        imagesRecycler.layoutManager = GridLayoutManager(viewHolder.root.context, 4)

        activity.let {
            sharedViewModelItineraryImages.daysList.observe(activity, Observer { it1 ->
                it1?.let { existingDaysList ->

                    imagesAdapter.clear()

                    if (existingDaysList.size > position) {
                        imagesRecycler.visibility = View.VISIBLE

                        for (image in existingDaysList[position]) {

                            FirebaseDatabase.getInstance().getReference("/images").child("${image.key}/body")
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
                                        }
                                    }

                                })
                        }

                        if (position == 0) {
                            moveUp.visibility = View.INVISIBLE
                            moveUp.isClickable = false
                        } else {
                            moveUp.visibility = View.VISIBLE
                        }

                        if (position == existingDaysList.size - 1) {
                            moveDown.visibility = View.INVISIBLE
                            moveDown.isClickable = false
                        } else {
                            moveDown.visibility = View.VISIBLE
                        }

                        moveUp.setOnClickListener {
                            val movedItem = existingDaysList[position]
                            existingDaysList.removeAt(position)
                            existingDaysList.add(position - 1, movedItem)
                            sharedViewModelItineraryImages.daysList.postValue(existingDaysList)
                            activity.collectionGalleryFragment.hasItineraryDataChanged = true
                        }

                        moveDown.setOnClickListener {
                            val movedItem = existingDaysList[position]
                            existingDaysList.removeAt(position)
                            existingDaysList.add(position + 1, movedItem)
                            sharedViewModelItineraryImages.daysList.postValue(existingDaysList)
                            activity.collectionGalleryFragment.hasItineraryDataChanged = true
                        }

                        mapBtn.setOnClickListener {

                            sharedViewModelDayCollection.imageCollection.postValue(existingDaysList[position])
                            activity.subFm.beginTransaction().add(
                                R.id.feed_subcontents_frame_container,
                                activity.dayMapViewFragment,
                                "dayMapViewFragment"
                            ).addToBackStack("dayMapViewFragment")
                                .commit()

                            activity.subActive = activity.dayMapViewFragment
                        }

                    } else {
                        imagesRecycler.visibility = View.GONE
                    }
                }
            })

        }

        imagesAdapter.setOnItemClickListener { item, view ->
            val image = item as CollectionPhoto

            sharedViewModelImage.sharedImageObject.postValue(image.image)

            FirebaseDatabase.getInstance().getReference("/users/${image.image.photographer}/profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.imageFullSizeFragment,
                            "imageFullSizeFragment"
                        ).addToBackStack("imageFullSizeFragment")
                            .commit()

                        activity.subActive = activity.imageFullSizeFragment
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


