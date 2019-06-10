package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.otherClasses.CustomMapView
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelTags
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*
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
import kotlinx.android.synthetic.main.fragment_dark_room_edit_map.*
import kotlinx.android.synthetic.main.fragment_image_expanded.*


class ImagePostEditFragment : Fragment(), PermissionsListener, DereMethods {

    lateinit var sharedViewModelTags: SharedViewModelTags
    lateinit var sharedViewModelImage: SharedViewModelImage

    private lateinit var myImageObject: Images

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    lateinit var imageChipGroup: ChipGroup

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

    lateinit var currentUser: Users



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

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
        }

        val imageView = dark_room_edit_image_horizontal
        val addTagButton = dark_room_edit_add_tag_button
        val imageTagsInput = dark_room_edit_tag_input
        imageChipGroup = dark_room_edit_chip_group
        imageLocationInput = dark_room_edit_location_input
        imageUrl = dark_room_edit_url

        val currentUserName = dark_room_edit_author_name
        val currentUserPhoto = dark_room_edit_author_image
        currentUserName.text = currentUser.name
        Glide.with(this.context!!).load(currentUser.image).into(currentUserPhoto)

        mapView = dark_room_edit_mapview
        val mapContainer = dark_room_edit_map_include

        var symbolOptions: SymbolOptions

        dark_room_edit_actions_container.visibility = View.GONE
        dark_room_edit_after_post_actions_container.visibility = View.VISIBLE

        val options = dark_room_edit_options
        val optionsContainer = dark_room_edit_options_background

        val saveButton = dark_room_edit_after_post_save
        val cancelButton = dark_room_edit_after_post_cancel

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

        cancelButton.setOnClickListener {
            activity.subFm.popBackStack("imagePostEditFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
                    BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.location_map))!!
                )

                sharedViewModelImage.sharedImageObject.observe(this, Observer {
                    it?.let { imageObject ->
                        myImageObject = imageObject

                        imageTagsList.clear()
                        imageChipGroup.removeAllViews()
                        symbolManager.deleteAll()

                        Glide.with(this).load(imageObject.imageBig).into(imageView)

                        imageLocationInput.text = imageObject.details
                        imageUrl.text = imageObject.link
                        imageLat = imageObject.location[0]
                        imageLong = imageObject.location[1]

                        imageObject.tags.forEach { tag ->
                            //                            imageTagsList.add(tag)
                            onTagSelected(tag)
                        }

                        println("tagsUpdated ${imageObject.tags}")


                        if (imageObject.private) {
                            dark_room_edit_privacy_text.text = getString(R.string.private_text)
                        } else {
                            dark_room_edit_privacy_text.text = getString(R.string.public_text)
                        }

                        dark_room_edit_privacy_container.setOnClickListener {

                            if (dark_room_edit_privacy_text.text == "private") {
                                dark_room_edit_privacy_text.text = getString(R.string.public_text)
                                imagePrivacy = false
                            } else {
                                dark_room_edit_privacy_text.text = getString(R.string.private_text)
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

                            if (imageChipGroup.childCount > 0) {
                                val locationInput = imageLocationInput.text.toString()

                                val url = if (imageUrl.text.isNotEmpty()) {
                                    imageUrl.text.toString()
                                } else {
                                    ""
                                }

                                val privacy = dark_room_edit_privacy_text.text == "private"

                                for (i in 0 until imageChipGroup.childCount) {
                                    val chip = imageChipGroup.getChildAt(i) as Chip
                                    imageTagsList.add(chip.text.toString())
                                }

                                val imageRef =
                                    FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/body")

                                imageRef.child("private").setValue(privacy)
                                imageRef.child("link").setValue(url)
                                imageRef.child("details").setValue(locationInput)
                                imageRef.child("tags").setValue(imageTagsList)
                                imageRef.child("location").setValue(mutableListOf(imageLat, imageLong))

                                FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/body")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {

                                            sharedViewModelImage.sharedImageObject.postValue(p0.getValue(Images::class.java))
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

                                            activity.subFm.popBackStack(
                                                "imagePostEditFragment",
                                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                                            )

                                        }

                                    })

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
        tagSuggestionRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
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

                val userInput = s.toString().toLowerCase()

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
        chip.setChipBackgroundColorResource(R.color.white)
        chip.chipStrokeWidth = 1f
        chip.setChipStrokeColorResource(R.color.gray500)
        chip.setCloseIconTintResource(R.color.gray500)
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
