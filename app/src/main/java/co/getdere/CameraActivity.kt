package co.getdere

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener


    lateinit var imageLocation : MutableList<Double>
    var locationAccuracy : String? = null   //this doesn't work right now will fix later


    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)


        // Acquire a reference to the system Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define a listener that responds to location updates
        locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )
        }



    }

        private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }


    fun makeUseOfNewLocation(location: Location){
        println(location.toString())

        imageLocation = mutableListOf(location.latitude, location.longitude)

        locationAccuracy = location.accuracy.toInt().toString()   //this doesn't work right now will fix later

        Log.d("LocationCheck", "location is: ${location.toString()}")
        Log.d("LocationCheck", "location latitude is: ${location.latitude.toString()}")
        Log.d("LocationCheck", "location latitude is: ${location.longitude.toString()}")

    }


}
