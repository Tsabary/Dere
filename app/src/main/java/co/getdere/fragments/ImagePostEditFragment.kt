package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
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
import co.getdere.otherClasses.CustomMapView
import co.getdere.otherClasses.MyCircleProgressBar
import co.getdere.roomclasses.LocalImagePost
import co.getdere.viewmodels.SharedViewModelImage
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
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_add_tag_button
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_chip_group
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_image_horizontal
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_image_vertical
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_location_input
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_map_include
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_privacy_container
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_privacy_text
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_progress_bar
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_save
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_share
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_tag_input
import kotlinx.android.synthetic.main.fragment_dark_room_edit_old.dark_room_edit_url
import kotlinx.android.synthetic.main.fragment_dark_room_edit.*
import kotlinx.android.synthetic.main.fragment_dark_room_edit_map.*
import me.echodev.resizer.Resizer
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


class ImagePostEditFragment : Fragment(), PermissionsListener, DereMethods {

    lateinit var sharedViewModelTags: SharedViewModelTags
    lateinit var sharedViewModelImage: SharedViewModelImage

    lateinit var myImageObject: Images

    val tagsFiltredAdapter = GroupAdapter<ViewHolder>()
    lateinit var imageChipGroup: ChipGroup
    val tagsRef = FirebaseDatabase.getInstance().getReference("/tags")

    lateinit var imageLocationInput: TextView
    lateinit var imageUrl: TextView
    var imageTagsList: MutableList<String> = mutableListOf()
    var imagePrivacy = false

    private var mapView: CustomMapView? = null
    private var myMapboxMap: MapboxMap? = null
    private lateinit var permissionsManager: PermissionsManager
    private val DERE_PIN = "derePin"
    lateinit var myStyle: Style

    var imageLat = 0.0
    var imageLong = 0.0

    lateinit var infoActiveButton: ImageButton
    lateinit var infoUnactiveButton: ImageButton
    lateinit var tagsActiveButton: ImageButton
    lateinit var tagsUnactiveButton: ImageButton
    lateinit var urlActiveButton: ImageButton
    lateinit var urlUnactiveButton: ImageButton

    lateinit var infoContainer: ConstraintLayout
    lateinit var tagsContainer: ConstraintLayout
    lateinit var urlContainer: ConstraintLayout


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
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

        val activity = activity as MainActivity

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

        dark_room_edit_actions_container.visibility = View.GONE
        dark_room_edit_after_post_actions_container.visibility = View.VISIBLE
        dark_room_edit_privacy_container.visibility = View.VISIBLE

        val saveButton = dark_room_edit_after_post_save
        val cancelButton = dark_room_edit_after_post_cancel

//        val setLocation = dark_room_edit_set_location

        val focus = dark_room_edit_map_focus
        val pinHint = dark_room_edit_pin_hint
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



        cancelButton.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment)
                .commit()
            activity.subActive = activity.imageFullSizeFragment
            makeInfoActive()
        }


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


                sharedViewModelImage.sharedImageObject.observe(this, Observer {
                    it?.let { imageObject ->
                        myImageObject = imageObject

                        imageTagsList.clear()
//                        imageChipGroup.removeAllViews()

                        Glide.with(this).load(imageObject.imageBig).into(imageHorizontal)

                        imageLocationInput.text = imageObject.details
                        imageUrl.text = imageObject.link
                        imageLat = imageObject.location[0]
                        imageLong = imageObject.location[1]

                        imageObject.tags.forEach { tag ->
                            imageTagsList.add(tag)
                            onTagSelected(tag)
                        }

                        if (imageObject.private) {
                            dark_room_edit_privacy_text.text = "private"
                        } else {
                            dark_room_edit_privacy_text.text = "public"
                        }

                        dark_room_edit_privacy_container.setOnClickListener {

                            if (dark_room_edit_privacy_text.text == "private") {
                                dark_room_edit_privacy_text.text = "public"
                                imagePrivacy = false
                            } else {
                                dark_room_edit_privacy_text.text = "private"
                                imagePrivacy = true
                            }
                        }

                        if (imageObject.verified) {
                            mapContainer.visibility = View.GONE
                        } else {
                            mapContainer.visibility = View.VISIBLE
                        }

                        symbolOptions = SymbolOptions()
                            .withLatLng(LatLng(imageLat, imageLong))
                            .withIconImage(DERE_PIN)
                            .withIconSize(1f)
                            .withZIndex(10)
                            .withDraggable(false)

                        symbolManager.create(symbolOptions)

                        val position = CameraPosition.Builder()
                            .target(LatLng(imageObject.location[0], imageObject.location[1]))
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


                        saveButton.setOnClickListener {

                            if (imageChipGroup.childCount > 0){
                                val locationInput = imageLocationInput.text.toString()
                                val url = imageUrl.text.toString()

                                val privacy = dark_room_edit_privacy_text.text == "private"

                                for (i in 0 until imageChipGroup.childCount) {
                                    val chip = imageChipGroup.getChildAt(i) as Chip
                                    imageTagsList.add(chip.text.toString())
                                }

                                val imageRef = FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/body")

                                imageRef.child("private").setValue(privacy)
                                imageRef.child("link").setValue(url)
                                imageRef.child("details").setValue(locationInput)
                                imageRef.child("tags").setValue(imageTagsList)
                                imageRef.child("location").setValue(mutableListOf(imageLat, imageLong))




                                val updatedImage = Images(
                                    imageObject.id,
                                    imageObject.imageBig,
                                    imageObject.imageSmall,
                                    privacy,
                                    imageObject.photographer,
                                    url,
                                    locationInput,
                                    mutableListOf(imageLat, imageLong),
                                    imageObject.timestampTaken,
                                    imageObject.timestampUpload,
                                    imageTagsList,
                                    imageObject.verified,
                                    0
                                )


                                sharedViewModelImage.sharedImageObject.postValue(updatedImage)

                                activity.subFm.beginTransaction().hide(activity.subActive)
                                    .show(activity.imageFullSizeFragment)
                                    .commit()
                                activity.subActive = activity.imageFullSizeFragment

                                makeInfoActive()

                                for (t in imageTagsList) {
                                    val refTag =
                                        FirebaseDatabase.getInstance().getReference("/tags/$t/${imageObject.id}")
                                    val refUserTags = FirebaseDatabase.getInstance()
                                        .getReference("users/${imageObject.photographer}/interests/$t")

                                    refTag.setValue("image")
                                    refUserTags.setValue(true)
                                }

                                closeKeyboard(activity)
                            } else {
                                Toast.makeText(this.context, "Please add at least one tag", Toast.LENGTH_SHORT).show()
                                makeTagsActive()
                            }
                        }
                    }
                })
            }
        }


        val tagSuggestionRecycler = dark_room_edit_tag_recycler
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFiltredAdapter


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
                        onTagSelected(imageTagsInput.text.toString().toLowerCase())
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

                val userInput = s.toString().toLowerCase()

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


    }

    private fun makeInfoActive() {
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


    private fun makeTagsActive() {

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


    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setCloseIconTintResource(R.color.green100)
        chip.setChipBackgroundColorResource(R.color.green700)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            imageChipGroup.removeView(it)
            imageTagsList.remove(selectedTag)
            val refTag = FirebaseDatabase.getInstance().getReference("/tags/$selectedTag/${myImageObject.id}")
            refTag.removeValue()
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
        fun newInstance(): ImagePostEditFragment = ImagePostEditFragment()
    }
}
