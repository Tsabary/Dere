package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import com.camerakit.type.CameraFacing


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
    ): View? = inflater.inflate(co.getdere.R.layout.fragment_camera, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mActivity = activity as CameraActivity

        cameraKitView = view.findViewById(co.getdere.R.id.camera_view)
//        cameraKitView.aspectRatio = 0.8f

        val captureButton = view.findViewById<ImageButton>(R.id.camera_btn)

        captureButton.setOnClickListener {

            Log.d("photoActivity", "button clicked")


            airLocation = AirLocation(mActivity, true, true, object : AirLocation.Callbacks {
                override fun onSuccess(location: Location) {


                    cameraKitView.captureImage() { _, p1 ->
                        Log.d("photoActivity", "image captured")


                        val timeStamp = System.currentTimeMillis().toString()
                        val fileName = "Dere$timeStamp.jpg"

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


                            Glide.with(mActivity).load(savedPhoto)
                                .into(mActivity.photoEditorFragment.view!!.findViewById(R.id.photo_editor_image))

                            Log.d("photoActivity", "image loaded into new fragment")


                            mActivity.switchVisibility(1)

                            Log.d("photoActivity", "visibility switched")


                            val localImagePost = LocalImagePost(
                                timeStamp.toLong(),
                                location.longitude,
                                location.latitude,
                                savedPhoto.path,
                                "",
                                "",
                                true
                            )
                            Log.d("photoActivity", "Took photo")

                            localImageViewModel.insert(localImagePost)

                            sharedViewModelLocalImagePost.sharedImagePostObject.postValue(localImagePost)



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
        }


//        currentAccuracy = view.findViewById<TextView>(co.getdere.R.id.camera_accuracy)
//
//
//        mStatusChecker = object : Runnable {
//            override fun run() {
//                try {
//                    updateStatus(mActivity, currentAccuracy) //this function can change value of mInterval.
//                } finally {
//                    // 100% guarantee that this always happens, even if
//                    // your update method throws an exception
//                    handler.postDelayed(this, delay)
//                }
//            }
//        }
//
//
//
//        startRepeatingTask()


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


    fun updateStatus(activity: Activity, currentAccuracy: TextView) {

        airLocation = AirLocation(activity, true, true, object : AirLocation.Callbacks {
            override fun onSuccess(location: Location) {

                currentAccuracy.text = location.accuracy.toString()
                Log.d("locationAccuracy", "Success")
            }

            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                Log.d("locationAccuracy", "Fail")

            }

        })

    }


//    fun startRepeatingTask() {
//        mStatusChecker.run()
//    }
//
//    fun stopRepeatingTask() {
//        handler.removeCallbacks(mStatusChecker)
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        airLocation?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        ) // ADD THIS LINE INSIDE onRequestPermissionResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

//    override fun onDetach() {
//        super.onDetach()
//        stopRepeatingTask()
//    }


    companion object {
        fun newInstance(): CameraFragment = CameraFragment()
    }


}