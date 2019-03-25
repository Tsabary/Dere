package co.getdere.Fragments


import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.fragment.app.Fragment

import co.getdere.CameraActivity
import co.getdere.CameraActivity2
import co.getdere.MainActivity
import co.getdere.Models.ImageFile
import co.getdere.Models.Images
import co.getdere.R
import com.camerakit.CameraPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import me.echodev.resizer.Resizer
import java.io.*
import java.util.*


class CameraPreviewFragment : Fragment() {

    private var fotoapparat: Fotoapparat? = null
    val filename = "dere_ ${UUID.randomUUID().toString()}"
    val sd = Environment.getExternalStorageDirectory().absolutePath
    val dest = File(sd + File.separator + "Dere" + File.separator + filename + ".jpg")
    var cameraStatus: CameraPreview.CameraState? = null

//    val permissions = arrayOf(
//        android.Manifest.permission.ACCESS_FINE_LOCATION
//    )


    //    var fotoapparatState: FotoapparatState? = null
//    var flashState: FlashState? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera_preview, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationAccuracyText = view.findViewById<TextView>(R.id.camera_accuracy)


        val cameraConfiguration = CameraConfiguration(
            pictureResolution = highestResolution(), // (optional) we want to have the highest possible photo resolution
            previewResolution = highestResolution(), // (optional) we want to have the highest possible preview resolution
            previewFpsRange = highestFps(),          // (optional) we want to have the best frame rate
            focusMode = firstAvailable(              // (optional) use the first focus mode which is supported by device
                continuousFocusPicture(),
                autoFocus(),                       // if continuous focus is not available on device, auto focus will be used
                fixed()                            // if even auto focus is not available - fixed focus mode will be used
            ),
            flashMode = firstAvailable(              // (optional) similar to how it is done for focus mode, this time for flash
                autoRedEye(),
                autoFlash(),
                torch(),
                off()
            ),
            antiBandingMode = firstAvailable(       // (optional) similar to how it is done for focus mode & flash, now for anti banding
                auto(),
                hz50(),
                hz60(),
                none()
            ),
            jpegQuality = manualJpegQuality(100),     // (optional) select a jpeg quality of 90 (out of 0-100) values
            sensorSensitivity = lowestSensorSensitivity(), // (optional) we want to have the lowest sensor sensitivity (ISO)
            frameProcessor = { frame -> }            // (optional) receives each frame from preview stream
        )


        fotoapparat = Fotoapparat(
            context = this.context!!,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            cameraConfiguration = cameraConfiguration,
            cameraErrorCallback = { error ->
                println("Recorder errors: $error")
            }
        )


        camera_btn.setOnClickListener {
            takePhoto()
        }


//        if (ContextCompat.checkSelfPermission(
//                this.context!!,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermission()
//        } else {
//            cameraActivity.locationManager.requestLocationUpdates(
//                LocationManager.NETWORK_PROVIDER,
//                0,
//                0f,
//                cameraActivity.locationListener
//            )
//        }


    }


//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(cameraActivity, permissions, 0)
//    }


    override fun onStart() {
        super.onStart()
        fotoapparat?.start()

    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
    }

    private fun takePhoto() {

        val photoResult = fotoapparat?.takePicture()
//            ?.saveToFile(dest)

//        fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
//
//            val bytes = ByteArrayOutputStream()
//
//            inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
//
//            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
//
//            return Uri.parse(path)
//        }


        fun getImageUri(inContext: Context, inImage: Bitmap): String {

            val bytes = ByteArrayOutputStream()

            inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)

            Log.d("CheckingImageUri", path)

            return path
        }



        fun bitmapToFile(bitmapPhoto : Bitmap) : String{

            val wrapper = ContextWrapper((activity as MainActivity).applicationContext)

            var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
            file = File(file, "${UUID.randomUUID()}.jpg")

            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmapPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return file.absolutePath

        }


        photoResult
            ?.toBitmap()
            ?.whenAvailable { bitmapPhoto ->

                val imagePath = getImageUri(this.context!!, bitmapPhoto!!.bitmap)
//
//                bitmapToFile(bitmapPhoto)
//
//
//                val smallImageUri = Resizer(this.context)
//                    .setTargetLength(80)
//                    .setQuality(100)
//                    .setOutputFormat("PNG")
//                    .setOutputFilename(randomName + "Small")
//                    .setOutputDirPath(storagePath)
//                    .setSourceImage(uriImage)
//                    .resizedBitmap


                val action =
                    CameraPreviewFragmentDirections.actionCameraPreviewFragmentToApproveImageFragment(imagePath)
                findNavController(camera_nav_host_fragment).navigate(action)
            }



    }

    //
//                val imageView: ImageView = view.findViewById(R.answerId.imageView)
//
//                imageView.setImageBitmap(bitmapPhoto?.bitmap)
//                imageView.rotation = (-bitmapPhoto!!.rotationDegrees).toFloat()
//
//                Log.d("Snap activity", "Took picture")


//    private fun uploadPhotoToStorage(){
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
//        ref.putFile(uriImage).addOnSuccessListener {
//            Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")
//
//            ref.downloadUrl.addOnSuccessListener {
//                Log.d("UploadActivity", "File location: $it")
//
//                addImageToFirebaseDatabase(it.toString())
//
//            }
//
//
//        }.addOnFailureListener {
//            Log.d("RegisterActivity", "Failed to upload image to server $it")
//        }
//    }


//    private fun addImageToFirebaseDatabase(image: String) {
//
//        val uid = FirebaseAuth.getInstance().uid ?: ""
//        val ref = FirebaseDatabase.getInstance().getReference("/images/feed/").push()
//        val location = (activity as CameraActivity).imageLocation
//
//
//        val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/images/byuser/$uid/${ref.key}")
//
//        val newImage = Images(
//            ref.key!!,
//            image,
//            uid,
//            "https://justalink.com",
//            "Some details that will be added by the user",
//            location,
//            System.currentTimeMillis()
//        )
//
//        ref.setValue(newImage)
//            .addOnSuccessListener {
//                Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")
//            }
//            .addOnFailureListener {
//                Log.d("imageToDatabase", "image did not save to feed")
//            }
//
//
//        refToUsersDatabase.setValue(newImage)
//            .addOnSuccessListener {
//                Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")
//            }
//            .addOnFailureListener {
//                Log.d("imageToDatabaseByUser", "image did not save to byUser")
//            }
//
//    }


    companion object {
        fun newInstance(): CameraPreviewFragment = CameraPreviewFragment()
    }


}

