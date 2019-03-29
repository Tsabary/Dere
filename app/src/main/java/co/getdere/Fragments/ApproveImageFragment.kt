package co.getdere.Fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.getdere.Adapters.NewPhotoUploadPagerAdapter
import co.getdere.CameraActivity2
import co.getdere.MainActivity
import co.getdere.Models.Images
import co.getdere.OtherClasses.SwipeLockableViewPager
import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import me.echodev.resizer.Resizer
import mumayank.com.airlocationlibrary.AirLocation
import java.io.File
import java.util.*


class ApproveImageFragment : Fragment() {


    lateinit var takenImageUri: String
    lateinit var imagePath: String
    lateinit var imageDescription: String
    lateinit var imageUrl: String
    var privacy = false
    lateinit var boxPagerAdapter: NewPhotoUploadPagerAdapter
    lateinit var viewPager: SwipeLockableViewPager
    private lateinit var progress: ProgressBar


    private var airLocation: AirLocation? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        airLocation?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        ) // ADD THIS LINE INSIDE onRequestPermissionResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(co.getdere.R.layout.fragment_approve_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePreview = view.findViewById<ImageView>(co.getdere.R.id.approve_image_image)
        val declineImage = view.findViewById<ImageButton>(co.getdere.R.id.approve_image_x)
        val acceptImage = view.findViewById<ImageButton>(co.getdere.R.id.approve_image_check)
        val privacyButton = view.findViewById<TextView>(co.getdere.R.id.approve_image_privacy_text)
        progress = view.findViewById<ProgressBar>(R.id.approve_image_progress)

//        setUpViewPager()

        arguments?.let {
            val safeArgs = ApproveImageFragmentArgs.fromBundle(it)
            takenImageUri = safeArgs.imageUri
            imagePath = getRealPathFromURI(this.context!!, Uri.parse(takenImageUri))
            Log.d("CheckIfItIsPath", imagePath)
        }

        Glide.with(this).load(Uri.parse(takenImageUri)).into(imagePreview)


        declineImage.setOnClickListener {
            val action = ApproveImageFragmentDirections.actionApproveImageFragmentToCameraPreviewFragment()
            findNavController().navigate(action)
        }

        acceptImage.setOnClickListener {
            imageDescription = view.findViewById<EditText>(R.id.approve_image_description_input).text.toString()
            imageUrl = view.findViewById<EditText>(R.id.approve_image_url_input).text.toString()
            uploadPhotoToStorage()
        }


        privacyButton.setOnClickListener {

            if (privacyButton.text == "public") {
                privacyButton.text = "private"
                privacy = true
            } else {
                privacyButton.text = "public"
                privacy = false
            }


        }


    }

//    private fun setUpViewPager() {
//        boxPagerAdapter = NewPhotoUploadPagerAdapter((activity as CameraActivity).supportFragmentManager)
//        val adapterOBJ= NewPhotoUploadPagerAdapter(childFragmentManager)
//        viewPager = view!!.approve_image_pager_adapter
//        viewPager.adapter = adapterOBJ
//        viewPager.setSwipePagingEnabled(false)
//
//    }


    private fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } catch (e: Exception) {
            Log.e(TAG, "getRealPathFromURI Exception : $e")
            return ""
        } finally {
            cursor?.close()
        }
    }


    private fun uploadPhotoToStorage() {

        progress.visibility = View.VISIBLE

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


        val filename = UUID.randomUUID().toString()
        val refBigImage = FirebaseStorage.getInstance().getReference("/images/" + filename + "Big")
        val refSmallImage = FirebaseStorage.getInstance().getReference("/images/" + filename + "Small")

        refSmallImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(100)
                    .setQuality(100)
                    .setOutputFormat("PNG")
                    .setOutputFilename(randomName + "Small")
                    .setOutputDirPath(storagePath)
                    .setSourceImage(File(imagePath))
                    .resizedFile
            )
        ).addOnSuccessListener {
            Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

            refSmallImage.downloadUrl.addOnSuccessListener { smallUri ->
                Log.d("UploadActivity", "File location: $smallUri")

                val smallImageUri = smallUri.toString()


                refBigImage.putFile(
                    Uri.fromFile(
                        Resizer(this.context)
                            .setTargetLength(800)
                            .setQuality(100)
                            .setOutputFormat("PNG")
                            .setOutputFilename(randomName + "Big")
                            .setOutputDirPath(storagePath)
                            .setSourceImage(File(imagePath))
                            .resizedFile
                    )
                ).addOnSuccessListener {
                    Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

                    refBigImage.downloadUrl.addOnSuccessListener { bigUri ->
                        Log.d("UploadActivity", "File location: $bigUri")

                        val bigImageUri = bigUri.toString()


                        addImageToFirebaseDatabase(bigImageUri, smallImageUri)

                    }


                }.addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload image to server $it")
                    progress.visibility = View.GONE
                }


            }


        }.addOnFailureListener {
            Log.d("RegisterActivity", "Failed to upload image to server $it")
            progress.visibility = View.GONE

        }
    }


    private fun addImageToFirebaseDatabase(bigImage: String, smallImage: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/images/").push()
        val imageBodyRef = FirebaseDatabase.getInstance().getReference("/images/${ref.key}/body")
//        val location = (activity as CameraActivity2).imageLocation








        airLocation = AirLocation((activity as CameraActivity2), true, true, object : AirLocation.Callbacks {
            override fun onSuccess(location: Location) {

                val locationForImage = mutableListOf(location.latitude, location.longitude)


                val newImage = Images(
                    ref.key!!,
                    bigImage,
                    smallImage,
                    privacy,
                    uid,
                    imageUrl,
                    imageDescription,
                    locationForImage,
                    System.currentTimeMillis()
                )

                imageBodyRef.setValue(newImage)
                    .addOnSuccessListener {
                        Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")
                        val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/users/$uid/images")

                        refToUsersDatabase.setValue(mapOf(ref.key to true))
                            .addOnSuccessListener {
                                Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")

                                val backToFeed = Intent((activity as CameraActivity2), MainActivity::class.java)
                                startActivity(backToFeed)

                            }
                            .addOnFailureListener {
                                Log.d("imageToDatabaseByUser", "image did not save to byUser")
                            }
                    }
                    .addOnFailureListener {
                        Log.d("imageToDatabase", "image did not save to feed")
                    }


            }

            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                // couldn't fetch location due to reason available in locationFailedEnum
                // you may optionally do something to inform the user, even though the reason may be obvious
            }

        })


    }


}
