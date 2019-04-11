package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.models.Images
import co.getdere.viewmodels.SharedViewModelBucket
import co.getdere.viewmodels.SharedViewModelImage
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
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils


class BucketMapViewFragment : Fragment(), PermissionsListener {

    private val DERE_PIN = "derePin"

    private lateinit var sharedViewModelForBucket: SharedViewModelBucket

    private var mapView: MapView? = null
    private lateinit var permissionsManager: PermissionsManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForBucket = ViewModelProviders.of(it).get(SharedViewModelBucket::class.java)
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

                sharedViewModelForBucket.sharedBucketObject.observe(this, Observer {
                    it?.let { bucket ->

                        for (imageId in bucket.children) {

                            val ref = FirebaseDatabase.getInstance().getReference("images/${imageId.key}/body")

                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {


                                    val image = p0.getValue(Images::class.java)

                                    val symbolOptions = SymbolOptions()
                                        .withLatLng(LatLng(image!!.location[0], image.location[1]))
                                        .withIconImage(DERE_PIN)
                                        .withIconSize(1.3f)
                                        .withZIndex(10)
                                        .withDraggable(false)


                                    symbolManager.create(symbolOptions)

                                }

                            })




                        }


//                        val position = CameraPosition.Builder()
//                            .target(LatLng(image.location[0], image.location[1]))
//                            .zoom(10.0)
//                            .build()

//                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))


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
        fun newInstance(): BucketMapViewFragment = BucketMapViewFragment()
    }

}

