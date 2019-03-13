package co.getdere.Fragments


import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import co.getdere.CameraActivity
import co.getdere.DereMethods
import co.getdere.Models.Images
import co.getdere.R
import com.camerakit.CameraPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import java.io.ByteArrayOutputStream
import java.io.File
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


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationAccuracy = view?.findViewById<TextView>(R.id.camera_accuracy)

        locationAccuracy?.text = (activity as CameraActivity).locationAccuracy

        fotoapparat = Fotoapparat(
            context = this.context,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
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

        val photoResult = fotoapparat
            ?.takePicture()
//            ?.saveToFile(dest)

        fun getImageUri(
            inContext: Context,
            inImage: Bitmap
        ): Uri {
            val bytes = ByteArrayOutputStream()
            inImage.compress(
                Bitmap.CompressFormat.PNG,
                100,
                bytes
            )
            val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
            return Uri.parse(
                path
            )
        }


        photoResult
            ?.toBitmap()
            ?.whenAvailable { bitmapPhoto ->
                val uriImage = getImageUri(this.context, bitmapPhoto!!.bitmap)
                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
                ref.putFile(uriImage).addOnSuccessListener {
                    Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("UploadActivity", "File location: $it")

                        addImageToFirebaseDatabase(it.toString())

                    }


                }.addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload image to server $it")
                }


//
//                val imageView: ImageView = view.findViewById(R.id.imageView)
//
//                imageView.setImageBitmap(bitmapPhoto?.bitmap)
//                imageView.rotation = (-bitmapPhoto!!.rotationDegrees).toFloat()
//
//                Log.d("Snap activity", "Took picture")

            }

    }


    private fun addImageToFirebaseDatabase(image: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/images/feed/").push()
        val location = (getActivity() as CameraActivity).imageLocation


        val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/images/byuser/$uid/${ref.key}")

        val newImage = Images(
            ref.key!!,
            image,
            uid,
            "https://justalink.com",
            "Some details that will be added by the user",
            location,
            System.currentTimeMillis()
        )

        ref.setValue(newImage)
            .addOnSuccessListener {
                Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")
            }
            .addOnFailureListener {
                Log.d("imageToDatabase", "image did not save to feed")
            }


        refToUsersDatabase.setValue(newImage)
            .addOnSuccessListener {
                Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")
            }
            .addOnFailureListener {
                Log.d("imageToDatabaseByUser", "image did not save to byUser")
            }

    }




    companion object {
        fun newInstance(): CameraPreviewFragment = CameraPreviewFragment()
    }


}

