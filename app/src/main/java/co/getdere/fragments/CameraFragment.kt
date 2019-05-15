package co.getdere.fragments


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.getdere.CameraActivity
import com.camerakit.CameraKitView
import mumayank.com.airlocationlibrary.AirLocation
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import com.bumptech.glide.Glide
import com.camerakit.CameraKit
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.lang.Exception


class CameraFragment : Fragment() {

//    val filename = "dere_ ${UUID.randomUUID().toString()}"
//    val sd = Environment.getExternalStorageDirectory().absolutePath
//    val dest = File(sd + File.separator + "Dere" + File.separator + filename + ".jpg")
//    var cameraStatus: CameraPreview.CameraState? = null


    lateinit var cameraKitView: CameraKitView

    val handler = Handler()
    val delay: Long = 2000 //milliseconds

    //    lateinit var mActivity: Activity
    lateinit var currentAccuracy: TextView

//    lateinit var mStatusChecker: Runnable

    private lateinit var localImageViewModel: LocalImageViewModel
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost

    private var airLocation: AirLocation? = null


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult

        super.onActivityResult(requestCode, resultCode, data)

//        val activity = activity as CameraActivity
//
//
//        Log.d("ucroppp", "activityResult")
//
//        val localImagePost = LocalImagePost(
//            activity.timeStamp,
//            activity.locationLong,
//            activity.locationLat,
//            UCrop.getOutput(data!!)!!.path!!,
//            "",
//            "",
//            true
//        )
//        Log.d("photoActivity", "Took photo")
//
//        localImageViewModel.insert(localImagePost)
//
//        sharedViewModelLocalImagePost.sharedImagePostObject.postValue(localImagePost)
//
//
//
//        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            val resultUri = UCrop.getOutput(data!!)
//            Log.d("ucroppp", "all good")
//            Log.d("ucroppp", resultUri.toString())
//
//        } else if (resultCode == UCrop.RESULT_ERROR) {
//            val cropError = UCrop.getError(data!!)
//            Log.d("ucroppp", "someError")
//            Log.d("ucroppp", cropError.toString())
//        } else {
//            Log.d("ucroppp", "no conditions satisfied")
//        }
//
//

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            localImageViewModel = ViewModelProviders.of(this).get(LocalImageViewModel::class.java)
            sharedViewModelLocalImagePost = ViewModelProviders.of(it).get(SharedViewModelLocalImagePost::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mActivity = activity as CameraActivity

        cameraKitView = camera_view

        val captureButton = camera_btn

        captureButton.setOnClickListener {

            if (isLocationServiceEnabled()) {

                Log.d("photoActivity", "button clicked")


                airLocation = AirLocation(mActivity, true, true, object : AirLocation.Callbacks {
                    override fun onSuccess(location: Location) {


                        cameraKitView.captureImage() { _, p1 ->
                            Log.d("photoActivity", "image captured")

                            mActivity.timeStamp = System.currentTimeMillis()
                            val fileName = "Dere${mActivity.timeStamp}.jpg"

                            val path =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"
                            val outputDir = File(path)
                            outputDir.mkdir()
                            val savedPhoto = File(path + File.separator + fileName)


                            Log.d("photoActivity", "new file created")

                            try {
                                val outputStream = FileOutputStream(savedPhoto.path)
                                outputStream.write(p1)
                                outputStream.close()
                                mActivity.sendBroadcast(
                                    Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(savedPhoto)
                                    )
                                )

                                Log.d("photoActivity", "Image saved to file and system rescaned the device")


                                mActivity.locationLat = location.latitude
                                mActivity.locationLong = location.longitude
                                cropImage(Uri.fromFile(savedPhoto))

                                mActivity.lastImageTaken = savedPhoto.path
//
//                            mActivity.switchVisibility(1)
//
//                            Log.d("photoActivity", "visibility switched")
//
//
//                            val localImagePost = LocalImagePost(
//                                timeStamp.toLong(),
//                                location.longitude,
//                                location.latitude,
//                                savedPhoto.path,
//                                "",
//                                "",
//                                true
//                            )
//                            Log.d("photoActivity", "Took photo")
//
//                            localImageViewModel.insert(localImagePost)
//
//                            sharedViewModelLocalImagePost.sharedImagePostObject.postValue(localImagePost)


                            } catch (e: java.io.IOException) {
                                e.printStackTrace()
                                Log.d("photoActivity", "failed to take photo")

                            }
                        }

                    }

                    override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                        Log.d("locationAccuracy", "Fail")
                        Toast.makeText(
                            activity,
                            "Please turn your location on to take a photo. Wait 10 seconds for best accuracy",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                })
            } else {
                Toast.makeText(this.context, "Please turn on your location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun isLocationServiceEnabled(): Boolean {
        var gps_enabled = false

        val locationManager: LocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (ex: Exception) {
            //do nothing...
        }

        return gps_enabled
    }

    private fun cropImage(filePath: Uri) {
        val myUcrop = UCrop.of(filePath, filePath)
        val options = UCrop.Options()
        options.setActiveWidgetColor(resources.getColor(R.color.green700))
        options.setActiveControlsWidgetColor(resources.getColor(R.color.white))
        myUcrop.withOptions(options).start(this.context!!, this, 1)
    }

    private fun exifToDegrees(exifOrientation: Int): Float {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
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