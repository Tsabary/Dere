package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_itinerary_summery.*
import kotlinx.android.synthetic.main.itinerary_summery_day.view.*
import kotlinx.android.synthetic.main.itinerary_summery_location.view.*


class ItinerarySummeryFragment : Fragment() {

    lateinit var sharedViewModelItineraryDayStrings: SharedViewModelItineraryDayStrings
    lateinit var sharedViewModelCollection: SharedViewModelCollection

    var myImageList = mutableListOf<MutableMap<String, Boolean>>()

    val daysAdapter = GroupAdapter<ViewHolder>()
    var startDay = 0

    lateinit var itineraryBody: ItineraryBody
    val uid = FirebaseAuth.getInstance().uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_summery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val daysRecycler = itinerary_summery_recycler
        daysRecycler.adapter = daysAdapter
        daysRecycler.layoutManager = LinearLayoutManager(this.context)

        activity.let {
            sharedViewModelItineraryDayStrings =
                ViewModelProviders.of(it).get(SharedViewModelItineraryDayStrings::class.java)

            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)

            sharedViewModelCollection.imageCollection.observe(this, Observer { collection1 ->
                collection1?.let { collection2 ->
                    if (collection2.hasChild("body")) {
                        daysAdapter.clear()
                        myImageList.clear()

                        itineraryBody = collection2.child("body").getValue(ItineraryBody::class.java)!!
                        startDay = itineraryBody.startDay

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
                    //                    myImageList.clear()
                    myImageList = existingImageList
                    populateDays(activity)
                }
            })

        }


        sharedViewModelItineraryDayStrings.daysList.observe(activity, Observer { mutableList ->
            mutableList?.let { existingImageList ->
                myImageList = existingImageList
            }
        })
    }

    private fun populateDays(activity: MainActivity) {
        daysAdapter.clear()
        for (day in myImageList) {
            daysAdapter.add(SingleDaySummery(activity, startDay, itineraryBody, uid!!))
        }
    }

    companion object {
        fun newInstance(): ItinerarySummeryFragment = ItinerarySummeryFragment()
    }

}

class SingleDaySummery(
    val activity: MainActivity,
    private val startDay: Int,
    val itineraryBody: ItineraryBody,
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

    val daysAdapter = GroupAdapter<ViewHolder>()
//        val dayLocationsImages = mutableMapOf<String, Boolean>()

    override fun getLayout(): Int {
        return R.layout.itinerary_summery_day
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val daysRecycler = viewHolder.itemView.itinerary_summery_day_recycler
        daysRecycler.adapter = daysAdapter
        daysRecycler.layoutManager = LinearLayoutManager(viewHolder.root.context)


        activity.let {
            sharedViewModelItineraryImages.daysList.observe(activity, Observer { it1 ->
                it1?.let { existingDaysList ->

                    daysAdapter.clear()
//                        dayLocationsImages.clear()

                    val imagesRef = FirebaseDatabase.getInstance().getReference("/images")

                    if (existingDaysList.size > position) {
                        daysRecycler.visibility = View.VISIBLE

                        for (image in existingDaysList[position]) {

                            imagesRef.child("${image.key}/body")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val imageObject = p0.getValue(Images::class.java)
                                        if (imageObject != null) {
                                            daysAdapter.add(
                                                SingleLocationSummery(
                                                    imageObject,
                                                    activity
                                                )
                                            )
//                                                dayLocationsImages[imageObject.id] = true
                                        }
                                    }
                                })
                        }
                    } else {
                        daysRecycler.visibility = View.GONE
                    }
                }
            })

        }


        viewHolder.itemView.itinerary_summery_day_number.text = (position + 1).toString()

        val weekday = (startDay + position).rem(7)

        viewHolder.itemView.itinerary_summery_day_weekday.text = when (weekday) {

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

class SingleLocationSummery(val image : Images, val activity : MainActivity) : Item<ViewHolder>(){

    private var sharedViewModelImage = ViewModelProviders.of(activity).get(
        SharedViewModelImage::class.java
    )

    private var sharedViewModelRandomUser = ViewModelProviders.of(activity).get(
        SharedViewModelRandomUser::class.java
    )

    override fun getLayout(): Int {
        return R.layout.itinerary_summery_location
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.itinerary_summery_location_description.text = image.details
        val linkAddress = viewHolder.itemView.itinerary_summery_location_link
        val imageView = viewHolder.itemView.itinerary_summery_location_image_image

        linkAddress.text = image.link
        linkAddress.setOnClickListener {
            sharedViewModelImage.sharedImageObject.postValue(image)
            activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.webViewFragment , "webViewFragment").addToBackStack("webViewFragment").commit()
            activity.subActive = activity.webViewFragment
        }

        Glide.with(viewHolder.root.context).load(image.imageBig).into(imageView)

        imageView.setOnClickListener {
            sharedViewModelImage.sharedImageObject.postValue(image)

            FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")
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
    }

}
