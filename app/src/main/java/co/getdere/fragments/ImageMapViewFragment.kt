package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.viewmodels.SharedViewModelImage
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
import kotlinx.android.synthetic.main.fragment_image_map_view.*


class ImageMapViewFragment : Fragment(), PermissionsListener, DereMethods {

    private val DERE_PIN = "derePin"
//    lateinit var imageBitmap : Bitmap
    var myMapboxMap : MapboxMap? = null


    private lateinit var sharedViewModelForImage: SharedViewModelImage

    private var mapView: MapView? = null
    private lateinit var permissionsManager: PermissionsManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
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

        val currentLocationFocus = image_map_focus

        currentLocationFocus.setOnClickListener {
            panToCurrentLocation(activity as MainActivity, myMapboxMap!!)
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
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.pin_icon))!!,
                    true
                )



                val geoJsonOptions = GeoJsonOptions().withTolerance(0.4f)
                val symbolManager = SymbolManager(mapView!!, mapboxMap, style, null, geoJsonOptions)
                symbolManager.iconAllowOverlap = true

                sharedViewModelForImage.sharedImageObject.observe(this, Observer {
                    it?.let { image ->




                        val symbolOptions = SymbolOptions()
                            .withLatLng(LatLng(image.location[0], image.location[1]))
                            .withIconImage(DERE_PIN)
                            .withIconSize(1.3f)
                            .withZIndex(10)
                            .withDraggable(false)

//                            .withIconImage(image.imageBig)


                        symbolManager.create(symbolOptions)

                        val position = CameraPosition.Builder()
                            .target(LatLng(image.location[0], image.location[1]))
                            .zoom(10.0)
                            .build()

                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))


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
        fun newInstance(): ImageMapViewFragment = ImageMapViewFragment()
    }

}