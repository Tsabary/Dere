package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.CollectionPhoto
import co.getdere.interfaces.DereMethods
import co.getdere.models.*
import co.getdere.viewmodels.*
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_itinerary_edit.*
import kotlin.math.ceil


class ItineraryEditFragment : Fragment(), DereMethods {
    val REQUEST_CODE_AUTOCOMPLETE = 19
    lateinit var selectedCarmenFeature: CarmenFeature

    lateinit var sharedViewModelItineraryImages: SharedViewModelItineraryImages
    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    lateinit var itineraryBody: ItineraryBody
    lateinit var itineraryInformational: ItineraryInformational
    lateinit var itineraryListing: ItineraryListing
    lateinit var itineraryBudget: ItineraryBudget

    val teaserImagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    lateinit var aboutYouContainer: ConstraintLayout
    lateinit var basicInformationContainer: ConstraintLayout
    lateinit var aboutItineraryContainer: ConstraintLayout
    lateinit var budgetContainer: ConstraintLayout
    lateinit var presentationContainer: ConstraintLayout
    lateinit var priceContainer: ConstraintLayout

    lateinit var location: TextView
    var locationIdString = ""
    var locationNameString = ""
    lateinit var title: EditText
    lateinit var description: EditText
    var audience = mutableListOf<Boolean>()
    lateinit var audienceSolo: CheckBox
    lateinit var audienceCouples: CheckBox
    lateinit var audienceFamilies: CheckBox
    lateinit var audienceGroups: CheckBox

    private var authorResidency = 0
    lateinit var authorAbout: EditText

    lateinit var aboutFood: EditText
    lateinit var aboutNightlife: EditText
    lateinit var aboutNature: EditText
    lateinit var aboutActivities: EditText
    lateinit var aboutAccommodation: EditText
    lateinit var aboutTransportation: EditText

    lateinit var budget: EditText
    lateinit var budgetFood: CheckBox
    lateinit var budgetNightlife: CheckBox
    lateinit var budgetActivities: CheckBox
    lateinit var budgetTransportation: CheckBox
    lateinit var budgetAccommodation: CheckBox
    lateinit var budgetOther: EditText


    var imageList = mutableListOf<String>()
    var sampleImages = mutableMapOf<String, Boolean>()
    lateinit var youtubeVideo: EditText

    lateinit var price: EditText
    lateinit var priceSuggestion: TextView
    var maxPrice = 0.0

    lateinit var nextBtn: TextView
    lateinit var backtBtn: TextView
    lateinit var publishBtn: TextView

    var step = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            sharedViewModelItineraryImages = ViewModelProviders.of(it).get(SharedViewModelItineraryImages::class.java)
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        aboutYouContainer = itinerary_edit_about_you_container
        basicInformationContainer = itinerary_edit_basic_information
        aboutItineraryContainer = itinerary_edit_about_itinerary_container
        budgetContainer = itinerary_edit_budget_container
        presentationContainer = itinerary_edit_presentation_container
        priceContainer = itinerary_edit_price_container

        location = itinerary_edit_location
        title = itinerary_edit_title
        description = itinerary_edit_description
        audienceSolo = itinerary_edit_solo_checkbox
        audienceCouples = itinerary_edit_couples_checkbox
        audienceFamilies = itinerary_edit_families_checkbox
        audienceGroups = itinerary_edit_groups_checkbox
        val lengthMinText = itinerary_edit_days_min
        val lengthMaxText = itinerary_edit_days_max

        val residencyOptionsContainer = itinerary_edit_author_residency
        val selectResidency = itinerary_edit_select_residency
        authorAbout = itinerary_edit_author_about


        aboutFood = itinerary_edit_food_input
        aboutNightlife = itinerary_edit_drinks_input
        aboutNature = itinerary_edit_nature_input
        aboutActivities = itinerary_edit_activities_input
        aboutAccommodation = itinerary_edit_accommodation_input
        aboutTransportation = itinerary_edit_transportation_input


        budget = itinerary_edit_budget
        budgetFood = itinerary_edit_food_checkbox
        budgetNightlife = itinerary_edit_nightlife_checkbox
        budgetActivities = itinerary_edit_activities_checkbox
        budgetTransportation = itinerary_edit_transportation_checkbox
        budgetAccommodation = itinerary_edit_accommodation_checkbox
        budgetOther = itinerary_edit_budget_other

        youtubeVideo = itinerary_edit_youtube_video
        price = itinerary_edit_price
        priceSuggestion = itinerary_edit_price_suggestion

        nextBtn = itinerary_edit_next
        backtBtn = itinerary_edit_back
        publishBtn = itinerary_edit_publish
        val teaserImageCta = itinerary_edit_teaser_images_cta


        sharedViewModelItinerary.itinerary.observe(this, Observer {
            it?.let { itinerary ->

                imageList.clear()

                itineraryBody = itinerary.child("body").getValue(ItineraryBody::class.java)!!
                maxPrice = ceil(itineraryBody.images.size / 3.0)
                priceSuggestion.text = (maxPrice - 0.01).toString()

                if (itinerary.hasChild("content") && itinerary.hasChild("listing") && itinerary.hasChild("budget")) {
                    itineraryInformational = itinerary.child("content").getValue(ItineraryInformational::class.java)!!
                    itineraryListing = itinerary.child("listing").getValue(ItineraryListing::class.java)!!
                    itineraryBudget = itinerary.child("budget").getValue(ItineraryBudget::class.java)!!



                    title.setText(itineraryBody.title)
                    description.setText(itineraryBody.description)
                    location.text = itineraryBody.locationName
                    locationNameString = itineraryBody.locationName
                    locationIdString = itineraryBody.locationId

                    selectResidency.text = when (itineraryInformational.authorKnowledge) {
                        1 -> {
                            "All my life"
                        }
                        2 -> {
                            "Most of my life"
                        }
                        3 -> {
                            "Over 5 years"
                        }
                        4 -> {
                            "Over 2 years"
                        }
                        5 -> {
                            "Less than 2 years"
                        }
                        6 -> {
                            "I've traveled here"
                        }
                        else -> {
                            "Select one"
                        }
                    }

                    authorAbout.setText(itineraryInformational.aboutAuthor)

                    aboutFood.setText(itineraryInformational.aboutFood)
                    aboutNightlife.setText(itineraryInformational.aboutNightlife)
                    aboutNature.setText(itineraryInformational.aboutNature)
                    aboutActivities.setText(itineraryInformational.aboutActivities)
                    aboutAccommodation.setText(itineraryInformational.aboutAccommodation)
                    aboutTransportation.setText(itineraryInformational.aboutTransportation)

                    budget.setText(itineraryBudget.budget.toString())

                    budgetFood.isChecked = itineraryBudget.food
                    budgetNightlife.isChecked = itineraryBudget.nightlife
                    budgetActivities.isChecked = itineraryBudget.activities
                    budgetTransportation.isChecked = itineraryBudget.transportation
                    budgetAccommodation.isChecked = itineraryBudget.accommodation


                    price.setText(itineraryListing.price.toString())

//                    if (itineraryListing.price > 0) {
//                        price.setText((ceil(itineraryBody.images.size.toDouble() / 3.0) - 0.01).toString())
//                    } else {
//                        price.setText("0")
//                    }

                    val imagesRef = FirebaseDatabase.getInstance().getReference("/images/")
                    for (image in itineraryListing.sampleImages) {
                        imagesRef.child("${image.key}/body")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val imageObject = p0.getValue(Images::class.java)
                                    if (imageObject != null) {
                                        imageList.add(imageObject.id)
                                        sharedViewModelItineraryImages.imageList.postValue(imageList)
                                    }
                                }
                            })
                    }
                    youtubeVideo.setText(itineraryListing.video)
                }
            }
        })


        val residency1 = itinerary_edit_author_residency_1
        val residency2 = itinerary_edit_author_residency_2
        val residency3 = itinerary_edit_author_residency_3
        val residency4 = itinerary_edit_author_residency_4
        val residency5 = itinerary_edit_author_residency_5
        val residency6 = itinerary_edit_author_residency_6

        location.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken()!!)
                .placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(activity)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }

        selectResidency.setOnClickListener {
            residencyOptionsContainer.visibility = View.VISIBLE
        }

        residency1.setOnClickListener {
            authorResidency = 1
            selectResidency.text = residency1.text
            residencyOptionsContainer.visibility = View.GONE
        }
        residency2.setOnClickListener {
            authorResidency = 2
            selectResidency.text = residency2.text
            residencyOptionsContainer.visibility = View.GONE
        }
        residency3.setOnClickListener {
            authorResidency = 3
            selectResidency.text = residency3.text
            residencyOptionsContainer.visibility = View.GONE
        }
        residency4.setOnClickListener {
            authorResidency = 4
            selectResidency.text = residency4.text
            residencyOptionsContainer.visibility = View.GONE
        }
        residency5.setOnClickListener {
            authorResidency = 5
            selectResidency.text = residency5.text
            residencyOptionsContainer.visibility = View.GONE
        }
        residency6.setOnClickListener {
            authorResidency = 6
            selectResidency.text = residency6.text
            residencyOptionsContainer.visibility = View.GONE
        }



        teaserImageCta.setOnClickListener {
            activity.subFm.beginTransaction().add(
                R.id.feed_subcontents_frame_container,
                activity.addImagesToItineraryFragment,
                "addImagesToItineraryFragment"
            ).addToBackStack("addImagesToItineraryFragment")
                .commit()
            activity.subActive = activity.addImagesToItineraryFragment
        }

        nextBtn.setOnClickListener {
            nextPage()
        }

        backtBtn.setOnClickListener {
            previousPage()
        }

        publishBtn.setOnClickListener {
            publishItinerary(1)
        }

        val teaserImagesRecycler = itinerary_edit_teaser_images_recycler
        teaserImagesRecycler.adapter = teaserImagesRecyclerAdapter
        teaserImagesRecycler.layoutManager = GridLayoutManager(this.context, 3)




        sharedViewModelItineraryImages.imageList.observe(this, Observer {
            it?.let { existingImageList ->


                teaserImagesRecyclerAdapter.clear()
                sampleImages.clear()

                if (existingImageList.isNotEmpty()) {
                    teaserImagesRecycler.visibility = View.VISIBLE

                    val imagesRef = FirebaseDatabase.getInstance().getReference("/images")


                    for (image in existingImageList) {

                        imagesRef.child("$image/body")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {

                                    val imageObject = p0.getValue(Images::class.java)
                                    if (imageObject != null) {
                                        teaserImagesRecyclerAdapter.add(
                                            CollectionPhoto(
                                                imageObject,
                                                activity,
                                                "itinerary",
                                                0
                                            )
                                        )
                                        sampleImages[imageObject.id] = true
                                    }
                                }
                            })


                    }
                } else {
                    teaserImagesRecycler.visibility = View.GONE
                }


            }
        })
    }

    override fun onPause() {
        super.onPause()
        step = 0
        publishItinerary(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        closeKeyboard(activity as MainActivity)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            // Retrieve selected locationId's CarmenFeature
            selectedCarmenFeature = PlaceAutocomplete.getPlace(data!!)


            location.text = selectedCarmenFeature.placeName()
            locationIdString = selectedCarmenFeature.id()!!
            locationNameString = selectedCarmenFeature.placeName()!!

        }
    }

    private fun nextPage() {

        when (step) {

            0 -> {
                basicInformationContainer.visibility = View.GONE
                aboutItineraryContainer.visibility = View.VISIBLE
                step = 1
                backtBtn.visibility = View.VISIBLE
            }

            1 -> {
                aboutItineraryContainer.visibility = View.GONE
                aboutYouContainer.visibility = View.VISIBLE
                step = 2
            }

            2 -> {
                aboutYouContainer.visibility = View.GONE
                budgetContainer.visibility = View.VISIBLE
                step = 3
            }

            3 -> {
                budgetContainer.visibility = View.GONE
                presentationContainer.visibility = View.VISIBLE
                step = 4
            }

            4 -> {
                presentationContainer.visibility = View.GONE
                priceContainer.visibility = View.VISIBLE
                step = 5
                nextBtn.visibility = View.GONE
                publishBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun previousPage() {

        when (step) {

            1 -> {
                basicInformationContainer.visibility = View.VISIBLE
                aboutItineraryContainer.visibility = View.GONE
                step = 0
                backtBtn.visibility = View.GONE
            }

            2 -> {
                aboutItineraryContainer.visibility = View.VISIBLE
                aboutYouContainer.visibility = View.GONE
                step = 1
            }

            3 -> {
                aboutYouContainer.visibility = View.VISIBLE
                budgetContainer.visibility = View.GONE
                step = 2
            }

            4 -> {
                budgetContainer.visibility = View.VISIBLE
                presentationContainer.visibility = View.GONE
                step = 3

            }

            5 -> {
                presentationContainer.visibility = View.VISIBLE
                priceContainer.visibility = View.GONE
                step = 4
                nextBtn.visibility = View.VISIBLE
                publishBtn.visibility = View.GONE
            }
        }
    }

    private fun publishItinerary(case: Int) {
        val activity = activity as MainActivity

        val time = System.currentTimeMillis()

        audience.add(0, audienceSolo.isChecked)
        audience.add(1, audienceCouples.isChecked)
        audience.add(2, audienceFamilies.isChecked)
        audience.add(3, audienceGroups.isChecked)

        val itineraryTechnicalDetails =

            ItineraryListing(
                time,
                time,
                if (price.text.isNotEmpty()) {
                    if (price.text.toString().toInt() < maxPrice) {
                        price.text.toString().toInt().toDouble()
                    } else {
                        maxPrice.toInt().toDouble()
                    }
                } else {
                    maxPrice.toInt().toDouble()
                }
                ,
                0f,
                audience,
                youtubeVideo.text.toString(),
                sampleImages
            )

        val itineraryInformationalDetails = ItineraryInformational(
            authorResidency,
            authorAbout.text.toString(),
            aboutFood.text.toString(),
            aboutNightlife.text.toString(),
            aboutNature.text.toString(),
            aboutActivities.text.toString(),
            aboutAccommodation.text.toString(),
            aboutTransportation.text.toString()
        )

        val itineraryBudgetDetails = ItineraryBudget(
            budget.text.toString().toInt(),
            budgetFood.isChecked,
            budgetNightlife.isChecked,
            budgetActivities.isChecked,
            budgetTransportation.isChecked,
            budgetAccommodation.isChecked,
            budgetOther.text.toString()
        )


        val updatedItineraryBody = ItineraryBody(
            itineraryBody.id,
            case != 0,
            itineraryBody.creator,
            title.text.toString(),
            description.text.toString(),
            itineraryBody.images,
            itineraryBody.days,
            0,
            locationIdString,
            locationNameString
        )

        val itineraryRef = FirebaseDatabase.getInstance().getReference("/itineraries/${itineraryBody.id}")

        itineraryRef.child("body").setValue(updatedItineraryBody).addOnSuccessListener {
            itineraryRef.child("content").setValue(itineraryInformationalDetails).addOnSuccessListener {
                itineraryRef.child("listing").setValue(itineraryTechnicalDetails).addOnSuccessListener {
                    itineraryRef.child("budget").setValue(itineraryBudgetDetails).addOnSuccessListener {
                        //                    activity.switchVisibility(0)
//                    activity.subActive = activity.imageFullSizeFragment
                        step = 0
                        activity.subFm.popBackStack("itineraryEditFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
//                    activity.subFm.beginTransaction().remove(activity.addImagesToItineraryFragment)
//                        .remove(activity.itineraryEditFragment).remove(activity.collectionGalleryFragment).commit()
                    }
                }
            }
        }
    }
}
