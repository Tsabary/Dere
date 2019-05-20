package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.otherClasses.CustomMapView
import co.getdere.otherClasses.MyCircleProgressBar
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import co.getdere.viewmodels.SharedViewModelTags
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dark_room_edit.*
import kotlinx.android.synthetic.main.fragment_dark_room_edit_map.*
import me.echodev.resizer.Resizer
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


class DarkRoomEditFragment : Fragment(), PermissionsListener, DereMethods {

    private lateinit var localImagePost: LocalImagePost
    private lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    lateinit var sharedViewModelTags: SharedViewModelTags

    private lateinit var localImageViewModel: LocalImageViewModel

    lateinit var currentLocalImagePost: LocalImagePost

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    lateinit var imageChipGroup: ChipGroup
    private val tagsRef = FirebaseDatabase.getInstance().getReference("/tags")

    lateinit var imageLocationInput: TextView
    lateinit var imageUrl: EditText
    var imageTagsList: MutableList<String> = mutableListOf()
    var imagePrivacy = false
    lateinit var imageTagsInput: EditText

    lateinit var progressBar: MyCircleProgressBar
    lateinit var uploadBackground: ConstraintLayout
    private lateinit var shareButton: TextView

    private var mapView: CustomMapView? = null
    private var myMapboxMap: MapboxMap? = null
    private lateinit var permissionsManager: PermissionsManager
    private val DERE_PIN = "derePin"
    lateinit var myStyle: Style

    var imageLat = 0.0
    var imageLong = 0.0

    private lateinit var infoActiveButton: ImageButton
    private lateinit var infoUnactiveButton: ImageButton
    private lateinit var tagsActiveButton: ImageButton
    private lateinit var tagsUnactiveButton: ImageButton
    private lateinit var urlActiveButton: ImageButton
    private lateinit var urlUnactiveButton: ImageButton

    lateinit var infoContainer: ConstraintLayout
    lateinit var tagsContainer: ConstraintLayout
    lateinit var urlContainer: ConstraintLayout

    lateinit var currentUser: Users

    lateinit var locationComponent: LocationComponent

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelLocalImagePost = ViewModelProviders.of(it).get(SharedViewModelLocalImagePost::class.java)
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
            localImageViewModel = ViewModelProviders.of(it).get(LocalImageViewModel::class.java)
        }


        tagsRef.addChildEventListener(object : ChildEventListener {

            var tags: MutableList<SingleTagForList> = mutableListOf()


            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val tagName = p0.key.toString()
                val count = p0.childrenCount.toInt()
                tags.add(SingleTagForList(tagName, count))
                sharedViewModelTags.tagList = tags
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView?.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.mapbox_access_token))

        return inflater.inflate(R.layout.fragment_dark_room_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as CameraActivity

        val imageView = dark_room_edit_image_horizontal
        val addTagButton = dark_room_edit_add_tag_button
        imageTagsInput = dark_room_edit_tag_input
        imageChipGroup = dark_room_edit_chip_group
        imageLocationInput = dark_room_edit_location_input
        imageUrl = dark_room_edit_url

        mapView = dark_room_edit_mapview
        val mapContainer = dark_room_edit_map_include

        val currentUserName = dark_room_edit_author_name
        val currentUserPhoto = dark_room_edit_author_image

        val uid = FirebaseAuth.getInstance().uid
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(Users::class.java)!!
                currentUserName.text = currentUser.name
                Glide.with(activity).load(currentUser.image).into(currentUserPhoto)
            }
        })


        var symbolOptions: SymbolOptions


        val options = dark_room_edit_options
        val optionsContainer = dark_room_edit_options_background

        val editDeleteContainer = dark_room_edit_options_edit_delete
        val deleteButton = dark_room_edit_delete
        val saveButton = dark_room_edit_save

        val deleteContainer = dark_room_edit_options_delete_container
        val deleteRemoveButton = dark_room_edit_delete_remove_button
        val cancelButton = dark_room_edit_delete_cancel_button

        shareButton = dark_room_edit_share

        val focus = dark_room_edit_map_focus
        val pinAction = dark_room_edit_map_pin
        //buttons
        infoActiveButton = dark_room_edit_info_button_active
        infoUnactiveButton = dark_room_edit_info_button_unactive
        tagsActiveButton = dark_room_edit_tags_button_active
        tagsUnactiveButton = dark_room_edit_tags_button_unactive
        urlActiveButton = dark_room_edit_url_button_active
        urlUnactiveButton = dark_room_edit_url_button_unactive

        //containers
        infoContainer = dark_room_edit_information_container
        tagsContainer = dark_room_edit_tags_container
        urlContainer = dark_room_edit_url_container

        options.setOnClickListener {
            optionsContainer.visibility = View.VISIBLE
        }

        optionsContainer.setOnClickListener {
            optionsContainer.visibility = View.GONE
        }

        infoUnactiveButton.setOnClickListener {
            makeInfoActive()
        }

        tagsUnactiveButton.setOnClickListener {
            makeTagsActive()
        }

        urlUnactiveButton.setOnClickListener {
            makeUrlActive()
        }

        focus.setOnClickListener {
            panToCurrentLocation(activity, myMapboxMap!!)
        }


        deleteButton.setOnClickListener {
            deleteContainer.visibility = View.VISIBLE
            editDeleteContainer.visibility = View.GONE
        }

        cancelButton.setOnClickListener {
            editDeleteContainer.visibility = View.VISIBLE
            deleteContainer.visibility = View.GONE
        }

        deleteRemoveButton.setOnClickListener {
            localImageViewModel.delete(localImagePost)
            activity.subFm.beginTransaction()
                .remove(activity.darkRoomEditFragment).commit()
            activity.switchVisibility(0)
        }

        saveButton.setOnClickListener {

            val locationInput = imageLocationInput.text.toString()
            val url = imageUrl.text.toString()

            val updatedImage = LocalImagePost(
                localImagePost.timestamp,
                imageLong,
                imageLat,
                localImagePost.imageUri,
                locationInput,
                url,
                localImagePost.verified
            )

            localImageViewModel.update(updatedImage).invokeOnCompletion {
                Toast.makeText(this.context, "Photo details saved successfully", Toast.LENGTH_LONG).show()
            }

            if (currentLocalImagePost != updatedImage) {
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                firebaseAnalytics.logEvent("local_image_updated", null)
            }
        }


        progressBar = dark_room_edit_progress_bar
        uploadBackground = dark_room_edit_white_background


        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) { style ->

                myMapboxMap = mapboxMap
                myStyle = style

                val geoJsonOptions = GeoJsonOptions().withTolerance(0.4f)
                val symbolManager = SymbolManager(mapView!!, myMapboxMap!!, myStyle, null, geoJsonOptions)

                symbolManager.iconAllowOverlap = true


                style.addImage(
                    DERE_PIN,
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.location_map))!!
                )


                sharedViewModelLocalImagePost.sharedImagePostObject.observe(this, Observer {
                    it?.let { localImageObject ->

                        currentLocalImagePost = localImageObject

                        Glide.with(this).load(localImageObject.imageUri).into(imageView)
                        localImagePost = localImageObject

                        imageLocationInput.text = localImageObject.details
                        imageUrl.setText(localImageObject.url)
                        imageLat = localImageObject.locationLat
                        imageLong = localImageObject.locationLong


                        dark_room_edit_privacy_text.text = getString(R.string.public_text)

                        dark_room_edit_privacy_container.setOnClickListener {

                            if (dark_room_edit_privacy_text.text == "private") {
                                dark_room_edit_privacy_text.text = getString(R.string.public_text)
                                imagePrivacy = false
                            } else {
                                dark_room_edit_privacy_text.text = getString(R.string.private_text)
                                imagePrivacy = true
                            }
                        }



                        if (localImagePost.verified) {
                            mapContainer.visibility = View.GONE
                        } else {
                            mapContainer.visibility = View.VISIBLE
                        }

                        symbolManager.deleteAll()
                        symbolOptions = SymbolOptions()
                            .withLatLng(LatLng(imageLat, imageLong))
                            .withIconImage(DERE_PIN)
                            .withIconSize(1f)
                            .withZIndex(10)
                            .withDraggable(false)


                        symbolManager.create(symbolOptions)

                        locationComponent = mapboxMap.locationComponent

                        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {

                            panToCurrentLocation(activity, myMapboxMap!!)


                            // Activate with options
                            if (ContextCompat.checkSelfPermission(
                                    this.context!!,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                locationComponent.activateLocationComponent(this.context!!, mapboxMap.style!!)
                            }

                            // Enable to make component visible
                            locationComponent.isLocationComponentEnabled = true

                            // Set the component's render mode
                            locationComponent.renderMode = RenderMode.COMPASS


                        } else {
                            permissionsManager = PermissionsManager(this)
                            permissionsManager.requestLocationPermissions(activity)
                        }




                        pinAction.setOnClickListener {
                            symbolManager.deleteAll()

                            symbolOptions = SymbolOptions()
                                .withLatLng(
                                    LatLng(
                                        mapboxMap.cameraPosition.target.latitude,
                                        mapboxMap.cameraPosition.target.longitude
                                    )
                                )
                                .withIconImage(DERE_PIN)
                                .withIconSize(1f)
                                .withZIndex(10)
                                .withDraggable(false)

                            symbolManager.create(symbolOptions)

                            imageLat = mapboxMap.cameraPosition.target.latitude
                            imageLong = mapboxMap.cameraPosition.target.longitude
                        }
                    }
                })
            }
        }


        val tagSuggestionRecycler = dark_room_edit_tag_recycler
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFilteredAdapter


        addTagButton.setOnClickListener {

            if (imageTagsInput.text.isNotEmpty()) {

                var tagsMatchCount = 0

                for (i in 0 until imageChipGroup.childCount) {
                    val chip = imageChipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == imageTagsInput.text.toString()) {
                        tagsMatchCount += 1
                    }
                }

                if (tagsMatchCount == 0) {
                    if (imageChipGroup.childCount < 5) {
                        onTagSelected(imageTagsInput.text.toString().toLowerCase().trimEnd().replace(" ", "-"))
                        imageTagsInput.text.clear()
                    } else {
                        Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this.context, "Tag had already been added", Toast.LENGTH_LONG).show()
                }

            }


        }



        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (imageChipGroup.childCount < 5) {
                onTagSelected(row.tag.tagString)
                imageTagsInput.text.clear()
            } else {
                Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
            }
        }


        imageTagsInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString().toLowerCase().replace(" ", "-")

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

                        var countTagMatches = 0
                        for (i in 0 until imageChipGroup.childCount) {
                            val chip = imageChipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {
                                countTagMatches += 1
                            }
                        }
                        if (countTagMatches == 0) {
                            tagSuggestionRecycler.visibility = View.VISIBLE
                            tagsFilteredAdapter.add(SingleTagSuggestion(t))
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })




        shareButton.setOnClickListener {

            if (imageChipGroup.childCount > 0) {
                Log.d("children", "are more than 0")
                for (i in 0 until imageChipGroup.childCount) {
                    val chip = imageChipGroup.getChildAt(i) as Chip
                    imageTagsList.add(chip.text.toString())
                }
                progressBar.visibility = View.VISIBLE
                uploadBackground.visibility = View.VISIBLE
                uploadPhotoToStorage()
                closeKeyboard(activity)
            } else {
                Toast.makeText(this.context, "Please add at least one tag", Toast.LENGTH_SHORT).show()
                makeTagsActive()
            }
        }
    }


    private fun uploadPhotoToStorage() {

        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0f

        val randomName = UUID.randomUUID().toString()
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"

        val refBigImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/big")
        val refSmallImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/small")

        var imageFile: File = File.createTempFile("ImageFile", "temporary")


        if (localImagePost.verified) {
            imageFile = File(localImagePost.imageUri)
        } else {
            val myInputStream =
                (activity as CameraActivity).contentResolver.openInputStream(Uri.parse(localImagePost.imageUri))
            FileUtils.copyInputStreamToFile(myInputStream, imageFile)
        }


        refSmallImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(400)
                    .setQuality(100)
                    .setOutputFormat("PNG")
                    .setOutputFilename(randomName + "Small")
                    .setOutputDirPath(path)
                    .setSourceImage(imageFile)
                    .resizedFile
            )
        ).addOnSuccessListener {
            progressBar.progress = 25f

            Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

            refSmallImage.downloadUrl.addOnSuccessListener { smallUri ->

                progressBar.progress = 35f

                Log.d("UploadActivity", "File location: $smallUri")

                val smallImageUri = smallUri.toString()


                refBigImage.putFile(
                    Uri.fromFile(
                        Resizer(this.context)
                            .setTargetLength(1200)
                            .setQuality(100)
                            .setOutputFormat("PNG")
                            .setOutputFilename(randomName + "Big")
                            .setOutputDirPath(path)
                            .setSourceImage(imageFile)
                            .resizedFile
                    )
                ).addOnSuccessListener {

                    progressBar.progress = 60f
                    Log.d("UploadActivity", "Successfully uploaded image ${it.metadata?.path}")

                    refBigImage.downloadUrl.addOnSuccessListener { bigUri ->
                        progressBar.progress = 70f
                        Log.d("UploadActivity", "File location: $bigUri")

                        val bigImageUri = bigUri.toString()

                        val imageBitmap = BitmapFactory.decodeFile(imageFile.path)

                        val imageHeight: Int = imageBitmap.height
                        val imageWidth: Int = imageBitmap.width

                        val imageRatio = "$imageWidth:$imageHeight"

                        addImageToFirebaseDatabase(bigImageUri, smallImageUri, localImagePost.verified, imageRatio)

                        val fileBig = File(bigUri.path)
                        if (fileBig.exists()) {
                            if (fileBig.delete()) {
                                Log.d("deleteOperation", "deleted big file")
                            } else {
                                Log.d("deleteOperation", "couldn't delete big file")
                            }
                        }

                        val fileSmall = File(smallUri.path)
                        if (fileSmall.exists()) {
                            if (fileSmall.delete()) {
                                Log.d("deleteOperation", "deleted big file")
                            } else {
                                Log.d("deleteOperation", "couldn't delete big file")

                            }
                        }


                    }.addOnFailureListener {
                        uploadFail()
                    }


                }.addOnFailureListener {
                    uploadFail()
                    Log.d("UploadActivity", "Failed to upload image to server $it")
                }


            }.addOnFailureListener {
                uploadFail()
            }


        }.addOnFailureListener {
            uploadFail()
            Log.d("UploadActivity", "Failed to upload image to server $it")
        }
    }


    private fun addImageToFirebaseDatabase(bigImage: String, smallImage: String, verified: Boolean, ratio: String) {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/images/").push()
        val imageBodyRef = FirebaseDatabase.getInstance().getReference("/images/${ref.key}/body")
        val userImagesRef = FirebaseDatabase.getInstance().getReference("/users/$uid/images/${ref.key}")

        val newImage = Images(
            ref.key!!,
            bigImage,
            smallImage,
            imagePrivacy,
            uid!!,
            imageUrl.text.toString(),
            imageLocationInput.text.toString(),
            mutableListOf(imageLat, imageLong),
            localImagePost.timestamp,
            System.currentTimeMillis(),
            imageTagsList,
            verified,
            System.currentTimeMillis(),
            ratio
        )


        imageBodyRef.setValue(newImage)
            .addOnSuccessListener {
                progressBar.progress = 85f
                Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")



                userImagesRef.setValue(true).addOnSuccessListener {

                    progressBar.progress = 95f
                    Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")

                    for (t in imageTagsList) {
                        val refTag = FirebaseDatabase.getInstance().getReference("/tags/$t/${ref.key}")
                        val refUserTags = FirebaseDatabase.getInstance().getReference("users/$uid/interests/$t")

                        refTag.setValue("image")
                        refUserTags.setValue(true)
                        progressBar.progress = 100f
                    }


                    localImageViewModel.delete(localImagePost).invokeOnCompletion {


                        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                        firebaseAnalytics.logEvent(if (newImage.verified) {"image_added_verified"} else {"image_added_unverified"}, null)

                        val backToFeed = Intent((activity as CameraActivity), MainActivity::class.java)
                        startActivity(backToFeed)

                        val activity = activity as CameraActivity
                        activity.subFm.beginTransaction()
                            .remove(activity.darkRoomEditFragment).commit()
                    }



                }

                    .addOnFailureListener {
                        uploadFail()
                        Log.d("imageToDatabaseByUser", "image did not save to byUser")
                    }
            }
            .addOnFailureListener {
                uploadFail()
                Log.d("imageToDatabase", "image did not save to feed")
            }
    }


    private fun makeTagsActive() {
        imageTagsInput.requestFocus()

        infoUnactiveButton.visibility = View.VISIBLE
        infoActiveButton.visibility = View.GONE

        tagsUnactiveButton.visibility = View.GONE
        tagsActiveButton.visibility = View.VISIBLE

        urlUnactiveButton.visibility = View.VISIBLE
        urlActiveButton.visibility = View.GONE

        infoContainer.visibility = View.GONE
        tagsContainer.visibility = View.VISIBLE
        urlContainer.visibility = View.GONE

    }

    private fun makeUrlActive() {
        imageUrl.requestFocus()

        infoUnactiveButton.visibility = View.VISIBLE
        infoActiveButton.visibility = View.GONE

        tagsUnactiveButton.visibility = View.VISIBLE
        tagsActiveButton.visibility = View.GONE

        urlUnactiveButton.visibility = View.GONE
        urlActiveButton.visibility = View.VISIBLE

        infoContainer.visibility = View.GONE
        tagsContainer.visibility = View.GONE
        urlContainer.visibility = View.VISIBLE
    }

    private fun makeInfoActive() {
        imageLocationInput.requestFocus()
        infoUnactiveButton.visibility = View.GONE
        infoActiveButton.visibility = View.VISIBLE

        tagsUnactiveButton.visibility = View.VISIBLE
        tagsActiveButton.visibility = View.GONE

        urlUnactiveButton.visibility = View.VISIBLE
        urlActiveButton.visibility = View.GONE

        infoContainer.visibility = View.VISIBLE
        tagsContainer.visibility = View.GONE
        urlContainer.visibility = View.GONE
    }


    private fun uploadFail() {
        progressBar.visibility = View.GONE
        uploadBackground.visibility = View.GONE
        shareButton.isClickable = true
    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.green700)
        chip.setCloseIconTintResource(R.color.green200)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            imageChipGroup.removeView(it)
        }

        imageChipGroup.addView(chip)
        imageChipGroup.visibility = View.VISIBLE

    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this.context, "Location needed to use map", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
//            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }


    companion object {
        fun newInstance(): DarkRoomEditFragment = DarkRoomEditFragment()
    }


}
