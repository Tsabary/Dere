package co.getdere.Fragments


import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.getdere.R
import com.camerakit.CameraPreview
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
    val filename = "Dere"
    val sd = Environment.getExternalStorageDirectory().absolutePath
    val dest = File(sd + File.separator + "Dere" + File.separator + filename + ".jpg")
    var cameraStatus: CameraPreview.CameraState? = null

    //    var fotoapparatState: FotoapparatState? = null
//    var flashState: FlashState? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera_preview, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    }

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


    private fun addImageToFirebaseDatabase(image : String){


    }

}

