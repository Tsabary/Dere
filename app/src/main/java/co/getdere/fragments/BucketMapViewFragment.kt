package co.getdere.fragments

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelBucket
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import com.google.firebase.database.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import mumayank.com.airlocationlibrary.AirLocation


class BucketMapViewFragment : Fragment(), PermissionsListener {

    private val DERE_PIN = "derePin"

    private lateinit var sharedViewModelForBucket: SharedViewModelBucket
    lateinit var currentUser: Users

    private var mapView: MapView? = null
    private lateinit var permissionsManager: PermissionsManager

    private var airLocation: AirLocation? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForBucket = ViewModelProviders.of(it).get(SharedViewModelBucket::class.java)
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

        return inflater.inflate(co.getdere.R.layout.fragment_image_map_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(co.getdere.R.id.image_map_view)



        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) { style ->

                style.addImage(
                    DERE_PIN,
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.pin_icon))!!,
                    true
                )


                val geoJsonOptions = GeoJsonOptions().withTolerance(0.4f)
                val symbolManager = SymbolManager(mapView!!, mapboxMap, style, null, geoJsonOptions)
                symbolManager.iconAllowOverlap = true







                sharedViewModelForBucket.sharedBucketId.observe(this, Observer {
                    it?.let { bucket ->


                        airLocation = AirLocation(activity as MainActivity, true, true, object : AirLocation.Callbacks{
                            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {

                            }

                            override fun onSuccess(location: Location) {

                                val position = CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(10.0)
                                    .build()

                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))

                            }

                        })


                        for (image in bucket.children){

                            val imagePath = image.key

                            val imageObjectPath =
                                FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")

                            imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val imageObject = p0.getValue(Images::class.java)

                                    val symbolOptions = SymbolOptions()
                                        .withLatLng(LatLng(imageObject!!.location[0], imageObject.location[1]))
                                        .withIconImage(DERE_PIN)
                                        .withIconSize(1.3f)
                                        .withZIndex(10)
                                        .withDraggable(false)


                                    symbolManager.create(symbolOptions)


                                }
                            })

                        }


//
//                        for (imageId in bucket.children) {
//
//                            val ref = FirebaseDatabase.getInstance().getReference("images/${imageId.key}/body")
//
//                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                                override fun onCancelled(p0: DatabaseError) {
//                                }
//
//                                override fun onDataChange(p0: DataSnapshot) {
//
//
//                                    val image = p0.getValue(Images::class.java)
//
//                                    val symbolOptions = SymbolOptions()
//                                        .withLatLng(LatLng(image!!.location[0], image.location[1]))
//                                        .withIconImage(DERE_PIN)
//                                        .withIconSize(1.3f)
//                                        .withZIndex(10)
//                                        .withDraggable(false)
//
//
//                                    symbolManager.create(symbolOptions)
//
//                                }
//
//                            })
//
//
//                        }





                    }
                })


            }
        }

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


    companion object {
        fun newInstance(): BucketMapViewFragment = BucketMapViewFragment()
    }

}

