package co.getdere.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.getdere.CameraActivity
import co.getdere.interfaces.DereMethods
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_dark_room_edit_map.*
import kotlinx.android.synthetic.main.fragment_image_map_view.*
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import co.getdere.R
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.Source


class DarkRoomMapViewFragment : Fragment(), PermissionsListener, DereMethods {

    private val DERE_PIN = "derePin"
    var myMapboxMap: MapboxMap? = null


    private var mapView: MapView? = null
    private lateinit var permissionsManager: PermissionsManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView?.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        Mapbox.getInstance(activity!!.applicationContext, getString(co.getdere.R.string.mapbox_access_token))

        return inflater.inflate(co.getdere.R.layout.fragment_dark_room_edit_map, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = image_map_view

        val currentLocationFocus = image_map_focus

        currentLocationFocus.setOnClickListener {
            panToCurrentLocation(activity as CameraActivity, myMapboxMap!!)
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

                        // Enable to make component visible
                        locationComponent.isLocationComponentEnabled = true

                        // Set the component's camera mode
//                    locationComponent.cameraMode = CameraMode.TRACKING

                        // Set the component's render mode
                        locationComponent.renderMode = RenderMode.COMPASS

                        panToCurrentLocation(activity as CameraActivity, mapboxMap)
                    }


                } else {
                    permissionsManager = PermissionsManager(this)
                    permissionsManager.requestLocationPermissions(activity)
                }


                style.addImage(
                    DERE_PIN,
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(co.getdere.R.drawable.location_map))!!
                )

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
        fun newInstance(): DarkRoomMapViewFragment = DarkRoomMapViewFragment()
    }

}