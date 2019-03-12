package co.getdere.Fragments


import android.app.Fragment
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.getdere.R
import com.camerakit.CameraPreview
import com.squareup.picasso.Picasso
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import java.io.File


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

        photoResult
            ?.toBitmap()
            ?.whenAvailable { bitmapPhoto ->
                val imageView: ImageView= view.findViewById(R.id.imageView)

                imageView.setImageBitmap(bitmapPhoto?.bitmap)
                imageView.rotation = (-bitmapPhoto!!.rotationDegrees).toFloat()
            }
        Log.d("Snap activity", "Took picture")

    }


    //These two enums below refers to using the flash and to switching the camera. I don't use either right now but keeping them around if needed later
//    enum class FlashState {
//        TORCH, OFF
//    }
//
//    enum class FotoapparatState {
//        ON, OFF
//    }
//


//
//    private fun saveImageToInternalStorage(drawableId:Int): Uri {
//        // Get the image from drawable resource as drawable object
//        val drawable = ContextCompat.getDrawable(applicationContext,drawableId)
//
//        // Get the bitmap from drawable object
//        val bitmap = (drawable as BitmapDrawable).bitmap
//
//        // Get the context wrapper instance
//        val wrapper = ContextWrapper(applicationContext)
//
//        // Initializing a new file
//        // The bellow line return a directory in internal storage
//        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
//
//
//        // Create a file to save the image
//        file = File(file, "${UUID.randomUUID()}.jpg")
//
//        try {
//            // Get the file output stream
//            val stream: OutputStream = FileOutputStream(file)
//
//            // Compress bitmap
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//
//            // Flush the stream
//            stream.flush()
//
//            // Close stream
//            stream.close()
//        } catch (e: IOException){ // Catch the exception
//            e.printStackTrace()
//        }
//
//        // Return the saved image uri
//        return Uri.parse(file.absolutePath)
//    }


}

