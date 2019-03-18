package co.getdere.Fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

import com.bumptech.glide.Glide
import me.echodev.resizer.Resizer
import android.os.Environment
import co.getdere.CameraActivity
import co.getdere.Models.ImageFile
import co.getdere.Models.Images
import co.getdere.Models.ImagesNewWithThumbnail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URI
import java.util.*


class ApproveImageFragment : Fragment() {


    lateinit var takenImageUri: String
    lateinit var takenImagePath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(co.getdere.R.layout.fragment_approve_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePreview = view.findViewById<ImageView>(co.getdere.R.id.approve_image_image)
        val declineImage = view.findViewById<ImageButton>(co.getdere.R.id.approve_image_x)
        val acceptImage = view.findViewById<ImageButton>(co.getdere.R.id.approve_image_check)

        arguments?.let {
            val safeArgs = ApproveImageFragmentArgs.fromBundle(it)

            takenImageUri = safeArgs.imageUri.uri.toString()
            takenImagePath = safeArgs.imageUri.realPath

        }

        Glide.with(this).load(takenImageUri).into(imagePreview)


        declineImage.setOnClickListener {
            val action = ApproveImageFragmentDirections.actionApproveImageFragmentToCameraPreviewFragment()
            findNavController().navigate(action)
        }

        acceptImage.setOnClickListener {
            uploadPhotoToStorage()
        }

    }


    private fun uploadPhotoToStorage() {

        Log.d("checkiftakeimageuriisnull", takenImageUri)

        val randomName = UUID.randomUUID().toString()
        val storagePath = Environment.getExternalStorageDirectory().absolutePath

//
//        val resizedImageBig = Resizer(this.context)
//            .setTargetLength(1080)
//            .setQuality(100)
//            .setOutputFormat("PNG")
//            .setOutputFilename("$randomName Big")
//            .setOutputDirPath(storagePath)
//            .setSourceImage(File(takenImageUri))
//            .resizedFile
//
//        val resizedImageSmall = Resizer(this.context)
//            .setTargetLength(80)
//            .setQuality(100)
//            .setOutputFormat("JPEG")
//            .setOutputFilename("$randomName Small")
//            .setOutputDirPath(storagePath)
//            .setSourceImage(File(takenImageUri))
//            .resizedFile


        var bigImageUri = ""
        var smallImageUri = ""

        val filename = UUID.randomUUID().toString()
        val refBigImage = FirebaseStorage.getInstance().getReference("/images/" + filename + "Big")
        val refSmallImage = FirebaseStorage.getInstance().getReference("/images/" + filename + "Small")

        refBigImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(80)
                    .setQuality(100)
                    .setOutputFormat("PNG")
                    .setOutputFilename(randomName + "Small")
                    .setOutputDirPath(storagePath)
                    .setSourceImage(File(takenImagePath))
                    .resizedFile
            )
        ).addOnSuccessListener {
            Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

            refBigImage.downloadUrl.addOnSuccessListener {
                Log.d("UploadActivity", "File location: $it")

                bigImageUri = it.toString()


                refSmallImage.putFile(
                    Uri.fromFile(
                        Resizer(this.context)
                            .setTargetLength(800)
                            .setQuality(100)
                            .setOutputFormat("PNG")
                            .setOutputFilename(randomName + "Big")
                            .setOutputDirPath(storagePath)
                            .setSourceImage(File(takenImagePath))
                            .resizedFile
                    )
                ).addOnSuccessListener {
                    Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

                    refBigImage.downloadUrl.addOnSuccessListener {
                        Log.d("UploadActivity", "File location: $it")

                        smallImageUri = it.toString()


                        addImageToFirebaseDatabase(bigImageUri, smallImageUri)

                    }


                }.addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload image to server $it")
                }


            }


        }.addOnFailureListener {
            Log.d("RegisterActivity", "Failed to upload image to server $it")
        }
    }


    private fun addImageToFirebaseDatabase(bigImage: String, smallImage: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/images/feed/").push()
        val location = (activity as CameraActivity).imageLocation


        val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/images/byuser/$uid/${ref.key}")

        val newImage = ImagesNewWithThumbnail(
            ref.key!!,
            bigImage,
            smallImage,
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


}
