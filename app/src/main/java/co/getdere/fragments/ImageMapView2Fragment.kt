package co.getdere.fragments

import android.Manifest
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.viewmodels.SharedViewModelImage
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style


class ImageMapView2Fragment : Fragment(), PermissionsListener {


    private lateinit var sharedViewModelForImage: SharedViewModelImage

    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        mapView.onCreate(savedInstanceState);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Mapbox.getInstance(activity!!.applicationContext, getString(co.getdere.R.string.mapbox_access_token))

        return inflater.inflate(co.getdere.R.layout.fragment_image_map_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mapView = view.findViewById(co.getdere.R.id.mapView)

        mapView.onCreate(savedInstanceState)


        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let { image ->
                mapView.getMapAsync { mapboxMap ->
                    mapboxMap.setStyle(Style.LIGHT) { style ->

                        map = mapboxMap

//                enableLocation()

                        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {

                            // Get an instance of the component
                            val locationComponent = mapboxMap.locationComponent

//                            val symbolManager = SymbolManager(mapView, mapboxMap, style)
//
//                            symbolManager.iconAllowOverlap = true

                            // Create a list to store our line coordinates.

//                            val routeCoordinates = ArrayList<Point>()
//                            routeCoordinates.add(Point.fromLngLat(-118.394391, 33.397676))
//                            routeCoordinates.add(Point.fromLngLat(-118.370917, 33.391142))
//
//                            // Create the LineString from the list of coordinates and then make a GeoJSON FeatureCollection so that you can add the line to our map as a layer.
//
//                            val lineString = LineString.fromLngLats(routeCoordinates)
//                            val featureCollection = FeatureCollection.fromFeatures(
//                                arrayOf(Feature.fromGeometry(lineString))
//                            )
//
//                            val geoJsonSource = GeoJsonSource("geojson-lalalalalafff", featureCollection)
//                            mapboxMap.style?.addSource(geoJsonSource)
//
//




//                            mapboxMap.addMarker(
//                                MarkerOptions()
//                                    .position(LatLng(image.location[0], image.location[1]))
//                            )
//
//
//
//
//                            val position = CameraPosition.Builder()
//                                .target(LatLng(image.location[0], image.location[1]))
//                                .zoom(10.0)
//                                .build()

//                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))


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
        fun newInstance(): ImageMapView2Fragment = ImageMapView2Fragment()
    }

}


//
//{
//
//
//    map = mapboxMap
////                enableLocation()
//
//    if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
//
//        // Get an instance of the component
//        val locationComponent = mapboxMap.locationComponent
//
//
//
//        mapboxMap.addMarker(
//            MarkerOptions()
//                .position(LatLng(image.location[0], image.location[1]))
//        )
//
//
//        val position = CameraPosition.Builder()
//            .target(LatLng(image.location[0], image.location[1]))
//            .zoom(10.0)
//            .build()
//
//        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
//
//
//        // Activate with options
//        if (ContextCompat.checkSelfPermission(
//                this.context!!,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            locationComponent.activateLocationComponent(this.context!!, mapboxMap.style!!)
//        }
//
//        // Enable to make component visible
//        locationComponent.isLocationComponentEnabled = true
//
//        // Set the component's camera mode
////                    locationComponent.cameraMode = CameraMode.TRACKING
//
//        // Set the component's render mode
//        locationComponent.renderMode = RenderMode.COMPASS
//
//
//    } else {
//        permissionsManager = PermissionsManager(this)
//        permissionsManager.requestLocationPermissions(activity)
//    }
//
//
//}


//class ImageMapViewFragment : Fragment(), PermissionsListener {
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
//        fun newInstance(): ImageMapViewFragment = ImageMapViewFragment()
//    }
//
//}


//companion object {
//    fun newInstance(): ImageMapView2Fragment = ImageMapView2Fragment()
//}
//
//}
