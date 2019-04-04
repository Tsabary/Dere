package co.getdere.Fragments


import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.getdere.CameraActivity
import com.camerakit.CameraKitView
import com.camerakit.CameraPreview
import mumayank.com.airlocationlibrary.AirLocation
import java.io.File
import java.io.FileOutputStream
import java.util.*


class CameraFragment : Fragment() {

    val filename = "dere_ ${UUID.randomUUID().toString()}"
    val sd = Environment.getExternalStorageDirectory().absolutePath
    val dest = File(sd + File.separator + "Dere" + File.separator + filename + ".jpg")
    var cameraStatus: CameraPreview.CameraState? = null

    lateinit var cameraKitView : CameraKitView


    private var airLocation: AirLocation? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(co.getdere.R.layout.fragment_camera, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraKitView = view.findViewById(co.getdere.R.id.camera_view)
        val captureButton = view.findViewById<ImageButton>(co.getdere.R.id.camera_btn)

        captureButton.setOnClickListener {

            cameraKitView.captureImage{ _, image ->

                val timeStamp = System.currentTimeMillis().toString()
                val fileName = "Dere22$timeStamp.jpg"

                val savedPhoto = File(Environment.getExternalStorageDirectory(), fileName)
                try {
                    val outputStream = FileOutputStream(savedPhoto.path)
                    outputStream.write(image)
                    outputStream.close()
                    Log.d("photoActivity", "Took photo")

                } catch (e: java.io.IOException) {
                    e.printStackTrace()
                    Log.d("photoActivity", "failed to take photo")

                }

            }
        }


        val currentAccuracy = view.findViewById<TextView>(co.getdere.R.id.camera_accuracy)
        val activity = activity as CameraActivity


        airLocation = AirLocation(activity, true, true, object : AirLocation.Callbacks {
            override fun onSuccess(location: Location) {
                currentAccuracy.text = location.accuracy.toString()
                Log.d("locationAccuracy","Success")
            }

            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                Log.d("locationAccuracy","Fail")

            }

        })




        val handler = Handler()
        val delay: Long = 2000 //milliseconds

        handler.postDelayed(object : Runnable {
            override fun run() {

                activity.runOnUiThread {
                    object : Runnable {
                        override fun run() {
                            getAccuracy(activity, currentAccuracy)
                        }
                    }
                }

                handler.postDelayed(this, delay)
            }
        }, delay)
    }



    fun getAccuracy(activity: Activity, currentAccuracy: TextView) {

        airLocation = AirLocation(activity, true, true, object : AirLocation.Callbacks {
            override fun onSuccess(location: Location) {
                currentAccuracy.text = location.accuracy.toString()
                Log.d("locationAccuracy","Success")
            }

            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                Log.d("locationAccuracy","Fail")

            }

        })

    }


    override fun onStart() {
        super.onStart()
        cameraKitView.onStart()
    }

    override fun onResume() {
        super.onResume()
        cameraKitView.onResume()
    }

    override fun onPause() {
        cameraKitView.onPause()
        super.onPause()
    }

    override fun onStop() {
        cameraKitView.onStop()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        airLocation?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        ) // ADD THIS LINE INSIDE onRequestPermissionResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    companion object {
        fun newInstance(): CameraFragment = CameraFragment()
    }


}