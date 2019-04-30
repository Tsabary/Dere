package co.getdere.fragments


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.models.Images
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_image_map_view.*


//import android.R


class ImageMapViewFragmentOld : Fragment(), PermissionsListener {


    private lateinit var sharedViewModelForImage: SharedViewModelImage

    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager
    private var locationEngine: LocationEngine? = null
    private var locationComponent: LocationComponent? = null
    lateinit var imageObject: Images

    var imageLatitude: Double? = null
    var imageLongtitude: Double? = null

    var imageLatitudeTest: Double? = -33.8886835
    var imageLongtitudeTest: Double? = 151.2785637


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(activity!!.applicationContext, getString(co.getdere.R.string.mapbox_access_token))
//        mapView.onCreate(savedInstanceState);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(co.getdere.R.layout.fragment_image_map_view, container, false)

        mapView = image_map_view

        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                mapView.getMapAsync { mapboxMap ->

                    mapboxMap.setStyle(Style.LIGHT) {


                        map = mapboxMap
//                enableLocation()

                        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {

                            // Get an instance of the component
                            val locationComponent = mapboxMap.locationComponent



                            mapboxMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(image.location[0], image.location[1]))
                            )


                            val position = CameraPosition.Builder()
                                .target(LatLng(image.location[0], image.location[1]))
                                .zoom(10.0)
                                .build()

                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))


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


                    }

                }
            }
        }
        )




        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)


//                mapView.onCreate(savedInstanceState)


    }


    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this.context, "Location needed to use map", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
//            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }


    companion object {
        fun newInstance(): ImageMapViewFragmentOld = ImageMapViewFragmentOld()
    }

}


//class ImageMapViewFragmentOld : Fragment(), PermissionsListener {
//
//
//    private lateinit var sharedViewModelForImage : SharedViewModelImage
//
//    private lateinit var mapView: MapView
//    private lateinit var map: MapboxMap
//    private lateinit var permissionsManager: PermissionsManager
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//
//        activity?.let {
//            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
//        }
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        Mapbox.getInstance(activity!!.applicationContext, getString(co.getdere.R.string.mapbox_access_token))
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? = inflater.inflate(co.getdere.R.layout.fragment_image_map_view, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        mapView = view.findViewById(co.getdere.R.id.mapView)
//
//
//        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
//            it?.let {image ->
//                mapView.onCreate(savedInstanceState)
//                mapView.getMapAsync { mapboxMap ->
//
//                    mapboxMap.setStyle(Style.LIGHT) { it ->
//
//                        map = mapboxMap
//
//                        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
//
//                            // Get an instance of the component
//                            val locationComponent = mapboxMap.locationComponent
//
//                            mapboxMap.addMarker(
//                                MarkerOptions()
//                                    .position(LatLng(image.location[0], image.location[1])))
//
//
//                            val position = CameraPosition.Builder()
//                                .target(LatLng(image.location[0], image.location[1]))
//                                .zoom(10.0)
//                                .build()
//
//                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
//
//
//                            // Activate with options
//                            if (ContextCompat.checkSelfPermission(
//                                    this.context!!,
//                                    Manifest.permission.ACCESS_FINE_LOCATION
//                                ) == PackageManager.PERMISSION_GRANTED
//                            ) {
//                                locationComponent.activateLocationComponent(this.context!!, mapboxMap.style!!)
//                            }
//
//                            // Enable to make component visible
//                            locationComponent.isLocationComponentEnabled = true
//
//                            // Set the component's render mode
//                            locationComponent.renderMode = RenderMode.COMPASS
//
//
//                        } else {
//                            permissionsManager = PermissionsManager(this)
//                            permissionsManager.requestLocationPermissions(activity)
//                        }
//
//
//                    }
//                }
//            }
//        }
//        )
//    }
//
//
//
//    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
//        Toast.makeText(this.context, "Location needed to use map", Toast.LENGTH_LONG).show()
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//
//    override fun onStart() {
//        super.onStart()
//        mapView.onStart()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapView.onStop()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView.onLowMemory()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        mapView.onDestroy()
//    }
//
//    companion object {
//        fun newInstance(): ImageMapViewFragmentOld = ImageMapViewFragmentOld()
//    }
//
//}

