package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.otherClasses.StartSnapHelper
import co.getdere.viewmodels.SharedViewModelCollection
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_collection_map_view.*
import kotlinx.android.synthetic.main.rv_on_top_of_map_card.view.*
import mumayank.com.airlocationlibrary.AirLocation


class CollectionMapViewFragment : Fragment(), PermissionsListener, DereMethods {

    private val DERE_PIN = "derePin"

    private lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users


    var myMapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    var coordinates = mutableListOf<LatLng>()

    val myAdapter = GroupAdapter<ViewHolder>()
    lateinit var myLayoutManager: LinearLayoutManager
    lateinit var locationImagesRecycler: RecyclerView

    var imagePinPosition = MutableLiveData<Int>()
    var positionAssignmentForAdapter = 0
    var currentPosition = 0


    private lateinit var permissionsManager: PermissionsManager

    private var airLocation: AirLocation? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView?.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        Mapbox.getInstance(activity!!.applicationContext, getString(co.getdere.R.string.mapbox_access_token))

        return inflater.inflate(R.layout.fragment_collection_map_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        locationImagesRecycler = bucket_map_images_recycler
        locationImagesRecycler.adapter = myAdapter
        myLayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        locationImagesRecycler.layoutManager = myLayoutManager
        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(locationImagesRecycler)


        mapView = bucket_map_view
        val currentLocationFocus = bucket_map_focus

        currentLocationFocus.setOnClickListener {
            panToCurrentLocation(activity, myMapboxMap!!)
        }



        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) { style ->

                myMapboxMap = mapboxMap

                val locationComponent = mapboxMap.locationComponent

                if (PermissionsManager.areLocationPermissionsGranted(this.context)) {


                    // Activate with options
                    if (ContextCompat.checkSelfPermission(
                            this.context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationComponent.activateLocationComponent(this.context!!, mapboxMap.style!!)
                    }

                    // Enable to make component visible
                    locationComponent.isLocationComponentEnabled = true

                    // Set the component's camera mode
//                    locationComponent.cameraMode = CameraMode.TRACKING

                    // Set the component's render mode
                    locationComponent.renderMode = RenderMode.COMPASS


                } else {
                    permissionsManager = PermissionsManager(this)
                    permissionsManager.requestLocationPermissions(activity)
                }


                style.addImage(
                    DERE_PIN,
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.location_map))!!
                )

                val geoJsonOptions = GeoJsonOptions().withTolerance(0.4f)
                val symbolManager = SymbolManager(mapView!!, mapboxMap, style, null, geoJsonOptions)

                symbolManager.addClickListener {

                    val pinPosition = coordinates.indexOf(it.latLng)
                    currentPosition = pinPosition
                    imagePinPosition.postValue(pinPosition)
                }

                symbolManager.iconAllowOverlap = true

                sharedViewModelCollection.imageCollection.observe(this, Observer {
                    it?.let { bucket ->

                        myAdapter.clear()
                        myAdapter.notifyDataSetChanged()
                        coordinates.clear()
                        symbolManager.deleteAll()

                        for (image in bucket.children) {

                            val imagePath = image.key

                            val imageObjectPath =
                                FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")

                            imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val imageObject = p0.getValue(Images::class.java)

                                    coordinates.add(LatLng(imageObject!!.location[0], imageObject.location[1]))

                                    myAdapter.add(ImageOnMap(imageObject, positionAssignmentForAdapter))

                                    val symbolOptions = SymbolOptions()
                                        .withLatLng(LatLng(imageObject.location[0], imageObject.location[1]))
                                        .withIconImage(DERE_PIN)
                                        .withIconSize(1f)
                                        .withZIndex(10)
                                        .withDraggable(false)

                                    symbolManager.create(symbolOptions)

                                    positionAssignmentForAdapter += 1
                                }
                            })


                        }

                        panToCurrentLocation(activity, myMapboxMap!!)
                    }
                })
            }
        }


        imagePinPosition.observe(this, Observer {
            it?.let { recyclerPosition ->

                val location = coordinates[recyclerPosition]

                val position = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(8.0)
                    .build()

                myMapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000)

                locationImagesRecycler.smoothScrollToPosition(recyclerPosition)

                myAdapter.notifyDataSetChanged()
            }
        })

    }


    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this.context, "Location needed to use map", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
//            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        airLocation?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        ) // ADD THIS LINE INSIDE onRequestPermissionResult
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        fun newInstance(): CollectionMapViewFragment = CollectionMapViewFragment()
    }


    inner class ImageOnMap(val image: Images, val position: Int) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.rv_on_top_of_map_card
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

            val imageFrame = viewHolder.itemView.rv_on_top_of_map_image
            Glide.with(viewHolder.itemView.context).load(image.imageBig).into(imageFrame)


            if (position == currentPosition) {
                imageFrame.alpha = 1f

            } else {
                imageFrame.alpha = 0.5f
            }




            viewHolder.itemView.setOnClickListener {
                currentPosition = position
                myAdapter.notifyDataSetChanged()
                imagePinPosition.postValue(position)
            }

            viewHolder.itemView.setOnLongClickListener {

                sharedViewModelImage.sharedImageObject.postValue(image)

                val randomUserRef = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")
                randomUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val user = p0.getValue(Users::class.java)

                        sharedViewModelRandomUser.randomUserObject.postValue(user)

                        val activity = activity as MainActivity

                        activity.subFm.beginTransaction().hide(activity.subActive)
                            .show(activity.imageFullSizeFragment).commit()
                        activity.subActive = activity.imageFullSizeFragment
//                        activity.bucketGalleryFragment.galleryViewPager.currentItem = 0
                        activity.isCollectionMapViewActive = true

                        currentPosition = position
                        myAdapter.notifyDataSetChanged()
                        imagePinPosition.postValue(position)
                    }


                })


                true
            }

        }


    }


}




