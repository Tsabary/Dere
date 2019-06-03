package co.getdere.fragments

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.groupieAdapters.LinearFeedImage
import co.getdere.groupieAdapters.LinearFeedImageLean
import co.getdere.groupieAdapters.StaggeredFeedImage
import co.getdere.interfaces.DereMethods
import co.getdere.models.*
import co.getdere.viewmodels.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feeds_layout.*
import mumayank.com.airlocationlibrary.AirLocation
import java.nio.channels.Selector


open class InterestsFeedFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    lateinit var sharedViewModelInterests: SharedViewModelInterests
    var staggeredImageList = mutableListOf<StaggeredFeedImage>()
    var staggeredWithLocationList = mutableListOf<StaggeredWithLocation>()
    var linearImageList = mutableListOf<LinearFeedImageLean>()
    var linearWithLocationList = mutableListOf<LinearWithLocationLean>()


    lateinit var currentUser: Users

    lateinit var feedRecycler: RecyclerView
    val staggeredGalleryAdapter = GroupAdapter<ViewHolder>()
    val linearGalleryAdapter = GroupAdapter<ViewHolder>()
    val distanceStaggeredGalleryAdapter = GroupAdapter<ViewHolder>()
    val distanceLinearGalleryAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid

    lateinit var staggeredGalleryLayoutManager: StaggeredGridLayoutManager
    lateinit var linearGalleryLayoutManager: LinearLayoutManager

    var isDistanceActive = false
    var isLinearActive = false
    var isDistanceListEmpty = true

    var airLocation: AirLocation? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(SharedViewModelInterests::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feeds_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedRecycler = feed_gallery
        setUpGalleryAdapter()

        feed_swipe_refresh.setOnRefreshListener {
            if (isDistanceActive) {
                listenToImagesWithLocation(currentUser)
            } else {
                listenToImages(currentUser)
            }
            feed_swipe_refresh.isRefreshing = false
        }

        val sortByDistance = feed_sort_distance
        val sortByDate = feed_sort_date

        sortByDistance.setOnClickListener {
            if (isLocationServiceEnabled(context!!)) {
                sortByDistance.setTextColor(ContextCompat.getColor(context!!, R.color.green700))
                sortByDate.setTextColor(ContextCompat.getColor(context!!, R.color.gray500))

                val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                firebaseAnalytics.logEvent("sort_by_distance", null)

                isDistanceActive = true

                if (isDistanceListEmpty) {
                    listenToImagesWithLocation(currentUser)
                    if (isLinearActive) {
                        feedRecycler.adapter = distanceLinearGalleryAdapter
                    } else {
                        feedRecycler.adapter = distanceStaggeredGalleryAdapter
                    }
                } else if (isLinearActive) {
                    feedRecycler.adapter = distanceLinearGalleryAdapter
                } else {
                    feedRecycler.adapter = distanceStaggeredGalleryAdapter
                }
            } else {
                Toast.makeText(this.context, "Please turn on your location", Toast.LENGTH_SHORT).show()
            }
        }

        sortByDate.setOnClickListener {
            sortByDistance.setTextColor(ContextCompat.getColor(context!!, R.color.gray500))
            sortByDate.setTextColor(ContextCompat.getColor(context!!, R.color.green700))

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("sort_by_date", null)

            isDistanceActive = false

            if (isLinearActive) {
                feedRecycler.adapter = linearGalleryAdapter
            } else {
                feedRecycler.adapter = staggeredGalleryAdapter
            }
        }

        val linearButton = feed_linear_layout
        val staggeredButton = feed_staggered_layout

        linearButton.setOnClickListener {
            linearButton.setImageResource(R.drawable.linear_layout_active)
            staggeredButton.setImageResource(R.drawable.staggered_layout)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("feed_linear", null)

            val position = IntArray(2)
            staggeredGalleryLayoutManager.findFirstCompletelyVisibleItemPositions(position)
            feedRecycler.layoutManager = linearGalleryLayoutManager
            linearGalleryLayoutManager.scrollToPosition(position[0])
            isLinearActive = true
            feedRecycler.adapter = if (isDistanceActive) {
                distanceLinearGalleryAdapter
            } else {
                linearGalleryAdapter
            }
        }

        staggeredButton.setOnClickListener {
            staggeredButton.setImageResource(R.drawable.staggered_layout_active)
            linearButton.setImageResource(R.drawable.linear_layout)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("feed_staggered", null)

            val position = linearGalleryLayoutManager.findFirstCompletelyVisibleItemPosition()
            feedRecycler.layoutManager = staggeredGalleryLayoutManager
            staggeredGalleryLayoutManager.scrollToPosition(position)
            isLinearActive = false
            feedRecycler.adapter = if (isDistanceActive) {
                distanceStaggeredGalleryAdapter
            } else {
                staggeredGalleryAdapter
            }
        }
    }

    private fun setUpGalleryAdapter() {
        linearGalleryLayoutManager = LinearLayoutManager(this.context)
        staggeredGalleryLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        feedRecycler.adapter = staggeredGalleryAdapter
        feedRecycler.layoutManager = staggeredGalleryLayoutManager
        listenToImages(currentUser)
    }


    private fun listenToImages(currentUser: Users) {
        staggeredGalleryAdapter.clear()
        linearGalleryAdapter.clear()
        linearImageList.clear()
        staggeredImageList.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                for (i in p0.children) {

                    val singleImageFromDB = i.child("body").getValue(Images::class.java)

                    if (singleImageFromDB != null) {
                        val completedInterestsList = sharedViewModelInterests.interestList
                        singlePhotoLoop@ for (tag in singleImageFromDB.tags) {

                            for (interest in completedInterestsList) {
                                if (interest == tag) {
                                    if (!singleImageFromDB.private) {

                                        staggeredImageList.add(
                                            StaggeredFeedImage(
                                                singleImageFromDB,
                                                currentUser,
                                                activity as MainActivity
                                            )
                                        )

                                        linearImageList.add(
                                            LinearFeedImageLean(
                                                singleImageFromDB,
                                                currentUser,
                                                activity as MainActivity
                                            )
                                        )

                                        staggeredGalleryAdapter.clear()
                                        staggeredGalleryAdapter.addAll(staggeredImageList.reversed())

                                        linearGalleryAdapter.clear()
                                        linearGalleryAdapter.addAll(linearImageList.reversed())
                                        break@singlePhotoLoop
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    private fun listenToImagesWithLocation(currentUser: Users) { //This needs to be fixed to not update in real time. Or should it?

        isDistanceListEmpty = false
        distanceStaggeredGalleryAdapter.clear()
        staggeredWithLocationList.clear()
        distanceLinearGalleryAdapter.clear()
        linearWithLocationList.clear()


        airLocation = AirLocation(activity as MainActivity, true, true, object : AirLocation.Callbacks {
            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                Toast.makeText(
                    activity,
                    "Could not retrieve your current location $locationFailedEnum",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onSuccess(location: Location) {


                val ref = FirebaseDatabase.getInstance().getReference("/images")

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {

                        for (i in p0.children) {

                            val singleImageFromDB = i.child("body").getValue(Images::class.java)

                            if (singleImageFromDB != null) {

                                Log.d("AccountPhoto", singleImageFromDB.photographer)


                                val completedInterestsList = sharedViewModelInterests.interestList

                                singlePhotoLoop@ for (tag in singleImageFromDB.tags) {

                                    for (interest in completedInterestsList) {

                                        Log.d("AccountFromList", interest)

                                        if (interest == tag) {
                                            if (!singleImageFromDB.private) {
                                                val result = FloatArray(1)
                                                Location.distanceBetween(
                                                    location.latitude,
                                                    location.longitude,
                                                    singleImageFromDB.location[0],
                                                    singleImageFromDB.location[1],
                                                    result
                                                )

                                                staggeredWithLocationList.add(
                                                    StaggeredWithLocation(
                                                        StaggeredFeedImage(
                                                            singleImageFromDB,
                                                            currentUser,
                                                            activity as MainActivity
                                                        ), result[0]
                                                    )
                                                )

                                                linearWithLocationList.add(
                                                    LinearWithLocationLean(
                                                        LinearFeedImageLean(
                                                            singleImageFromDB,
                                                            currentUser,
                                                            activity as MainActivity
                                                        ), result[0]
                                                    )
                                                )

                                                distanceStaggeredGalleryAdapter.clear()
                                                distanceLinearGalleryAdapter.clear()

                                                staggeredWithLocationList.sortBy { it.distance }
                                                linearWithLocationList.sortBy { it.distance }

                                                for (objectFromList in staggeredWithLocationList) {
                                                    distanceStaggeredGalleryAdapter.add(objectFromList.image)
                                                }

                                                for (objectFromList in linearWithLocationList) {
                                                    distanceLinearGalleryAdapter.add(objectFromList.image)
                                                }



                                                break@singlePhotoLoop
                                            }
                                        }

                                    }

                                }


                            }

                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })

            }

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        airLocation?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        ) // ADD THIS LINE INSIDE onRequestPermissionResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        fun newInstance(): InterestsFeedFragment = InterestsFeedFragment()
    }

}
