package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.otherClasses.CustomMapView
import co.getdere.otherClasses.MyCircleProgressBar
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import co.getdere.viewmodels.SharedViewModelTags
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
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
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_add_tag_button
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_chip_group
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_delete_cancel_button
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_delete_message
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_delete_remove_button
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_image_horizontal
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_image_vertical
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_location_input
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_map_include
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_privacy_container
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_privacy_text
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_progress_bar
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_remove
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_save
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_share
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_tag_input
import kotlinx.android.synthetic.main.fragment_dark_room_edit.dark_room_edit_url
import kotlinx.android.synthetic.main.fragment_dark_room_edit2.*
import kotlinx.android.synthetic.main.fragment_dark_room_edit_map.*
import me.echodev.resizer.Resizer
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


class DarkRoomEditFragment : Fragment(), PermissionsListener, DereMethods {

    lateinit var localImagePost: LocalImagePost
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    lateinit var sharedViewModelTags: SharedViewModelTags

    private lateinit var localImageViewModel: LocalImageViewModel


    val tagsFiltredAdapter = GroupAdapter<ViewHolder>()
    lateinit var imageChipGroup: ChipGroup
    val tagsRef = FirebaseDatabase.getInstance().getReference("/tags")

    lateinit var imageLocationInput: TextView
    lateinit var imageUrl: TextView
    var imageTagsList: MutableList<String> = mutableListOf()
    var imagePrivacy = false

    lateinit var progressBar: MyCircleProgressBar
    private lateinit var shareButton: TextView

    private var mapView: CustomMapView? = null
    private var myMapboxMap: MapboxMap? = null
    private lateinit var permissionsManager: PermissionsManager
    private val DERE_PIN = "derePin"
    lateinit var myStyle: Style

    var imageLat = 0.0
    var imageLong = 0.0


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

        return inflater.inflate(R.layout.fragment_dark_room_edit2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageVertical = dark_room_edit_image_vertical
        val imageHorizontal = dark_room_edit_image_horizontal
        val addTagButton = dark_room_edit_add_tag_button
        val imageTagsInput = dark_room_edit_tag_input
        imageChipGroup = dark_room_edit_chip_group
        imageLocationInput = dark_room_edit_location_input
        imageUrl = dark_room_edit_url

        mapView = dark_room_edit_mapview
        val mapContainer = dark_room_edit_map_include

        var symbolOptions: SymbolOptions

        val removeButton = dark_room_edit_remove

        val deleteMessage = dark_room_edit_delete_message
        val deleteButton = dark_room_edit_delete_remove_button
        val cancelButton = dark_room_edit_delete_cancel_button

        val setLocation = dark_room_edit_set_location

        val focus = dark_room_edit_map_focus

        //buttons
        val infoActiveButton = dark_room_edit_info_button_active
        val infoUnactiveButton = dark_room_edit_info_button_unactive
        val tagsActiveButton = dark_room_edit_tags_button_active
        val tagsUnactiveButton = dark_room_edit_tags_button_unactive
        val urlActiveButton = dark_room_edit_url_button_active
        val urlUnactiveButton = dark_room_edit_url_button_unactive

        //containers
        val infoButtonsContainer = dark_room_edit_info_buttons_container
        val tagButtonsContainer = dark_room_edit_tags_buttons_container
        val urlButtonsContainer = dark_room_edit_url_buttons_container


        infoUnactiveButton.setOnClickListener {
            infoUnactiveButton.visibility = View.GONE
            infoActiveButton.visibility = View.VISIBLE
            tagsUnactiveButton.visibility = View.VISIBLE
            tagsActiveButton.visibility = View.GONE
            urlUnactiveButton.visibility = View.VISIBLE
            urlActiveButton.visibility = View.GONE

            infoButtonsContainer.visibility = View.VISIBLE
            tagButtonsContainer.visibility = View.GONE
            urlButtonsContainer.visibility = View.GONE
        }

        tagsUnactiveButton.setOnClickListener {
            infoUnactiveButton.visibility = View.VISIBLE
            infoActiveButton.visibility = View.GONE
            tagsUnactiveButton.visibility = View.GONE
            tagsActiveButton.visibility = View.VISIBLE
            urlUnactiveButton.visibility = View.VISIBLE
            urlActiveButton.visibility = View.GONE

            infoButtonsContainer.visibility = View.GONE
            tagButtonsContainer.visibility = View.VISIBLE
            urlButtonsContainer.visibility = View.GONE
        }

        urlUnactiveButton.setOnClickListener {
            infoUnactiveButton.visibility = View.VISIBLE
            infoActiveButton.visibility = View.GONE
            tagsUnactiveButton.visibility = View.VISIBLE
            tagsActiveButton.visibility = View.GONE
            urlUnactiveButton.visibility = View.GONE
            urlActiveButton.visibility = View.VISIBLE

            infoButtonsContainer.visibility = View.GONE
            tagButtonsContainer.visibility = View.GONE
            urlButtonsContainer.visibility = View.VISIBLE
        }











        focus.setOnClickListener {
            panToCurrentLocation(activity as CameraActivity, myMapboxMap!!)
        }


        removeButton.setOnClickListener {

            removeButton.visibility = View.GONE

            deleteMessage.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {

            removeButton.visibility = View.VISIBLE

            deleteMessage.visibility = View.GONE
            deleteButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
        }

        deleteButton.setOnClickListener {
            localImageViewModel.delete(localImagePost)
        }


        val saveButton = dark_room_edit_save
        shareButton = dark_room_edit_share
        progressBar = dark_room_edit_progress_bar






        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) { style ->

                myMapboxMap = mapboxMap
                myStyle = style

                val geoJsonOptions = GeoJsonOptions().withTolerance(0.4f)
                val symbolManager = SymbolManager(mapView!!, myMapboxMap!!, myStyle, null, geoJsonOptions)

                symbolManager.iconAllowOverlap = true


                style.addImage(
                    DERE_PIN,
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.pin_icon))!!,
                    true
                )


                sharedViewModelLocalImagePost.sharedImagePostObject.observe(this, Observer {
                    it?.let { localImageObject ->

                        Glide.with(this).load(localImageObject.imageUri).into(imageHorizontal)
                        localImagePost = localImageObject

                        imageLocationInput.text = localImageObject.details
                        imageUrl.text = localImageObject.url
                        imageLat = localImageObject.locationLat
                        imageLong = localImageObject.locationLong


                        dark_room_edit_privacy_text.text = "public"

                        dark_room_edit_privacy_container.setOnClickListener {

                            if (dark_room_edit_privacy_text.text == "private") {
                                dark_room_edit_privacy_text.text = "public"
                                imagePrivacy = false
                            } else {
                                dark_room_edit_privacy_text.text = "private"
                                imagePrivacy = true
                            }
                        }



                        if (localImagePost.verified) {
                            mapContainer.visibility = View.GONE
                        } else {
                            mapContainer.visibility = View.VISIBLE
                        }


                        symbolOptions = SymbolOptions()
                            .withLatLng(LatLng(imageLat, imageLong))
                            .withIconImage(DERE_PIN)
                            .withIconSize(1.3f)
                            .withZIndex(10)
                            .withDraggable(false)


                        symbolManager.create(symbolOptions)


                        val position = CameraPosition.Builder()
                            .target(LatLng(localImageObject.locationLat, localImageObject.locationLong))
                            .zoom(10.0)
                            .build()

                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))

                        val locationComponent = mapboxMap.locationComponent

                        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {


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

                            // Set the component's camera mode
//                    locationComponent.cameraMode = CameraMode.TRACKING

                            // Set the component's render mode
                            locationComponent.renderMode = RenderMode.COMPASS


                        } else {
                            permissionsManager = PermissionsManager(this)
                            permissionsManager.requestLocationPermissions(activity)
                        }




                        setLocation.setOnClickListener {
                            symbolManager.deleteAll()

                            symbolOptions = SymbolOptions()
                                .withLatLng(
                                    LatLng(
                                        mapboxMap.cameraPosition.target.latitude,
                                        mapboxMap.cameraPosition.target.longitude
                                    )
                                )
                                .withIconImage(DERE_PIN)
                                .withIconSize(1.3f)
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


        val tagSuggestionRecycler =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.dark_room_edit_tag_recycler)
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFiltredAdapter


        addTagButton.setOnClickListener {

            if (!imageTagsInput.text.isEmpty()) {

                var tagsMatchCount = 0

                for (i in 0 until imageChipGroup.childCount) {
                    val chip = imageChipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == imageTagsInput.text.toString()) {
                        tagsMatchCount += 1
                    }
                }

                if (tagsMatchCount == 0) {
                    if (imageChipGroup.childCount < 5) {
                        onTagSelected(imageTagsInput.text.toString())
                        imageTagsInput.text.clear()
                    } else {
                        Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this.context, "Tag had already been added", Toast.LENGTH_LONG).show()
                }

            }


        }



        tagsFiltredAdapter.setOnItemClickListener { item, _ ->
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
                tagsFiltredAdapter.clear()

                val userInput = s.toString()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

//                        tagSuggestionRecycler.visibility = View.VISIBLE
//                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                        var countTagMatches = 0
                        for (i in 0 until imageChipGroup.childCount) {
                            val chip = imageChipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {

                                countTagMatches += 1

                            }

                        }


                        if (countTagMatches == 0) {
                            tagSuggestionRecycler.visibility = View.VISIBLE
                            tagsFiltredAdapter.add(SingleTagSuggestion(t))

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

            shareButton.isClickable = false

            for (i in 0 until imageChipGroup.childCount) {
                val chip = imageChipGroup.getChildAt(i) as Chip
                imageTagsList.add(chip.text.toString())
            }

            uploadPhotoToStorage()
        }


        saveButton.setOnClickListener {

            val locationInput = imageLocationInput.text.toString()
            val url = imageUrl.text.toString()

            val privacy = dark_room_edit_privacy_text.text == "private"


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
                //                Toast.makeText(this.context, "Photo details saved successfully", Toast.LENGTH_LONG).show()
            }

        }

    }

    var imageFile: File = File.createTempFile("smallfile", "temporary")

    private fun uploadPhotoToStorage() {

        if (localImagePost.verified) {
            Log.d("verified yes", localImagePost.imageUri)
        } else {
            Log.d("verified no", localImagePost.imageUri)
        }



        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0f

        val randomName = UUID.randomUUID().toString()
        val storagePath = Environment.getExternalStorageDirectory().absolutePath
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"

        val refBigImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/big")
        val refSmallImage = FirebaseStorage.getInstance().getReference("/images/feed/$randomName/small")

        val myInputStream = activity!!.contentResolver.openInputStream(Uri.parse(localImagePost.imageUri))


        if (localImagePost.verified) {
            imageFile = File(localImagePost.imageUri)
        } else {
            FileUtils.copyInputStreamToFile(myInputStream, imageFile)
        }


        refSmallImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(200)
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
                            .setTargetLength(800)
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


                        addImageToFirebaseDatabase(bigImageUri, smallImageUri, localImagePost.verified)

                    }.addOnFailureListener {
                        uploadFail()
                    }


                }.addOnFailureListener {
                    uploadFail()
                    Log.d("RegisterActivity", "Failed to upload image to server $it")
//                    progress.visibility = View.GONE
                }


            }.addOnFailureListener {
                uploadFail()
            }


        }.addOnFailureListener {
            uploadFail()
            Log.d("RegisterActivity", "Failed to upload image to server $it")
//            progress.visibility = View.GONE

        }
    }


    private fun addImageToFirebaseDatabase(bigImage: String, smallImage: String, verified: Boolean) {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/images/").push()
        val imageBodyRef = FirebaseDatabase.getInstance().getReference("/images/${ref.key}/body")

        val newImage = Images(
            ref.key!!,
            bigImage,
            smallImage,
            imagePrivacy,
            uid!!,
            imageUrl.text.toString(),
            imageLocationInput.text.toString(),
            mutableListOf(localImagePost.locationLat, localImagePost.locationLong),
            localImagePost.timestamp,
            System.currentTimeMillis(),
            imageTagsList,
            verified
        )


        imageBodyRef.setValue(newImage)
            .addOnSuccessListener {
                progressBar.progress = 85f
                Log.d("imageToDatabase", "image saved to feed successfully: ${ref.key}")
                val refToUsersDatabase = FirebaseDatabase.getInstance().getReference("/users/$uid/images")

                refToUsersDatabase.setValue(mapOf(ref.key to true))
                    .addOnSuccessListener {
                        progressBar.progress = 95f
                        Log.d("imageToDatabaseByUser", "image saved to byUser successfully: ${ref.key}")

                        for (t in imageTagsList) {
                            val refTag = FirebaseDatabase.getInstance().getReference("/tags/$t/${ref.key}")
                            val refUserTags = FirebaseDatabase.getInstance().getReference("users/$uid/interests/$t")

                            refTag.setValue("image")
                            refUserTags.setValue(true)
                            progressBar.progress = 100f
                        }

                        localImageViewModel.delete(localImagePost)


                        val backToFeed = Intent((activity as CameraActivity), MainActivity::class.java)
                        startActivity(backToFeed)

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


    private fun uploadFail() {
        progressBar.visibility = View.GONE
        shareButton.isClickable = true
    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.green700)
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
