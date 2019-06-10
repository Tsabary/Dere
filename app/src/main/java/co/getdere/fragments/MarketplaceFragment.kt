package co.getdere.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.ItineraryBudget
import co.getdere.models.ItineraryListing
import co.getdere.viewmodels.SharedViewModelItinerary
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.stfalcon.pricerangebar.model.BarEntry
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_marketplace.*
import kotlinx.android.synthetic.main.fragment_marketplace.rangeBar
import kotlinx.android.synthetic.main.marketplace_single_itinerary.view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.collections.ArrayList


class MarketplaceFragment : Fragment(), DereMethods {

    private val REQUEST_CODE_AUTOCOMPLETE = 21
    val adapter = GroupAdapter<ViewHolder>()
    lateinit var sharedViewModelItinerary: SharedViewModelItinerary
    private lateinit var filterLocation: TextView
    private var selectedCarmenFeature: CarmenFeature? = null

    var pricesList = mutableListOf<Float>()
    val barEntries = mutableListOf<BarEntry>()

    var allItineraries = mutableListOf<SingleItinerary>()
    var filteredItineraries = listOf<SingleItinerary>()

    var minimumBudget = 0
    var maximumBudget = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_marketplace, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
        }
        val savedItinerariesIcon = marketplace_saved_itineraries_icon
        val filterItinerariesIcon = marketplace_filter_icon
        val filterItinerariesBox = marketplace_filter_box
        filterLocation = marketplace_filter_location

        val recycler = marketplace_recycler
        recycler.adapter = adapter
//        recycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler.layoutManager = LinearLayoutManager(this.context)

        listenToItineraries()

        marketplace_swipe_refresh.setOnRefreshListener {
            listenToItineraries()
            marketplace_swipe_refresh.isRefreshing = false
        }

        savedItinerariesIcon.setOnClickListener {
            activity.subFm.beginTransaction().show(activity.marketplacePurchasedFragment)
                .commit()
            activity.subActive = activity.marketplacePurchasedFragment
            activity.switchVisibility(1)
            activity.isSavedItinerariesActive = true
        }

        filterItinerariesIcon.setOnClickListener {
            if (filterItinerariesBox.visibility == View.VISIBLE) {
                filterItinerariesBox.visibility = View.GONE
                filterItinerariesIcon.setImageResource(R.drawable.filter)
            } else {
                filterItinerariesBox.visibility = View.VISIBLE
                filterItinerariesIcon.setImageResource(R.drawable.filter_active)
            }
        }

        filterLocation.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(activity)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }

        adapter.setOnItemClickListener { item, _ ->

            val itinerary = item as SingleItinerary

            FirebaseDatabase.getInstance().getReference("/itineraries/${itinerary.itinerary.id}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        sharedViewModelItinerary.itinerary.postValue(p0)
                        activity.subFm.beginTransaction()
                            .add(R.id.feed_subcontents_frame_container, activity.itineraryFragment, "itineraryFragment")
                            .addToBackStack("itineraryFragment")
                            .commit()
                        activity.subActive = activity.itineraryFragment

                        activity.switchVisibility(1)
                    }
                })
        }
    }

    private fun initRangeBar(rangeBarEntries: ArrayList<BarEntry>) {
        rangeBar.setEntries(rangeBarEntries)
        rangeBar.onRangeChanged = { leftPinValue, rightPinValue ->
            rangeBarValue.text = getString(R.string.area_range, leftPinValue, rightPinValue)

            filteredItineraries =
                allItineraries.filter {
                    it.itineraryBudget.budget >= leftPinValue!!.toInt() && it.itineraryBudget.budget <= rightPinValue!!.toInt() && if (selectedCarmenFeature != null) {
                        it.itinerary.locationId == selectedCarmenFeature!!.id()
                    } else {
                        it.itinerary.public
                    }
                }
            adapter.clear()
            adapter.addAll(filteredItineraries)

            minimumBudget = leftPinValue!!.toInt()
            maximumBudget = rightPinValue!!.toInt()
        }
        rangeBar.onLeftPinChanged = { index, leftPinValue ->
        }
        rangeBar.onRightPinChanged = { index, rightPinValue ->
        }
        rangeBar.onSelectedEntriesSizeChanged = { selectedEntriesSize ->
        }
        rangeBar.onSelectedItemsSizeChanged = { selectedItemsSize ->
            rangeBarInfo.text = getString(R.string.formatter_elements, selectedItemsSize.toString())
        }

        var totalSelectedSize = 0
        rangeBarEntries.forEach { entry ->
            totalSelectedSize += entry.y.toInt()
        }
        rangeBarInfo.text = getString(R.string.formatter_elements, totalSelectedSize.toString())

        if (rangeBarEntries.isNotEmpty()) {
            rangeBarValue.text = getString(
                R.string.area_range,
                rangeBarEntries.first().x.toInt().toString(),
                rangeBarEntries.last().x.toInt().toString()
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        closeKeyboard(activity as MainActivity)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            // Retrieve selected locationId's CarmenFeature
            selectedCarmenFeature = PlaceAutocomplete.getPlace(data!!)

            filterLocation.text = selectedCarmenFeature!!.placeName()

            filteredItineraries =
                allItineraries.filter { it.itinerary.locationId == selectedCarmenFeature!!.id() && maximumBudget >= it.itineraryBudget.budget && it.itineraryBudget.budget >= minimumBudget }
            adapter.clear()
            adapter.addAll(filteredItineraries)
        }
    }

    private fun listenToItineraries() {

        adapter.clear()
        allItineraries.clear()
        pricesList.clear()
        barEntries.clear()

        FirebaseDatabase.getInstance().getReference("/itineraries")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (itinerarySnapshot in p0.children) {

                        val itinerary = itinerarySnapshot.child("body").getValue(ItineraryBody::class.java)
                        val itineraryListing =
                            itinerarySnapshot.child("listing").getValue(ItineraryListing::class.java)
                        val itineraryBudget =
                            itinerarySnapshot.child("budget").getValue(ItineraryBudget::class.java)
                        val reviewCount = itinerarySnapshot.child("reviews").childrenCount

                        if (itinerary != null && itineraryListing != null && itineraryBudget != null) {
                            allItineraries.add(SingleItinerary(itinerary, itineraryListing, itineraryBudget, reviewCount.toInt()))
                            filteredItineraries = allItineraries
                            adapter.clear()
                            adapter.addAll(allItineraries)

                            pricesList.add(itineraryBudget.budget.toFloat())
                        }
                    }

                    if (pricesList.isNotEmpty()) {
                        for (entry in pricesList.groupingBy { it }.eachCount()) {
                            barEntries.add(BarEntry(entry.key, entry.value.toFloat()))
                        }

                        barEntries.sortBy { it.x }

                        val myArray = ArrayList(barEntries)

                        initRangeBar(myArray)

                        minimumBudget = barEntries.first().x.toInt()
                        maximumBudget = barEntries.last().x.toInt()
                    }
                }
            })
    }


    companion object {
        fun newInstance(): MarketplaceFragment = MarketplaceFragment()
    }
}

class SingleItinerary(
    val itinerary: ItineraryBody,
    private val itineraryListing: ItineraryListing,
    val itineraryBudget: ItineraryBudget,
    val reviewCount : Int
) :
    Item<ViewHolder>() {
    override fun getLayout(): Int = R.layout.marketplace_single_itinerary


    override fun bind(viewHolder: ViewHolder, position: Int) {

        val ratingBar = viewHolder.itemView.marketplace_single_row_rating_bar

        firstImage@ for (image in itineraryListing.sampleImages) {
            FirebaseDatabase.getInstance().getReference("/images/${image.key}/body")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val imageObject = p0.getValue(Images::class.java)
                        if (imageObject != null) {
                            val imageView = viewHolder.itemView.marketplace_single_row_image
                            (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = imageObject.ratio
                            Glide.with(viewHolder.root.context).load(imageObject.imageBig)
                                .into(viewHolder.itemView.marketplace_single_row_image)
                        }
                    }
                })
            break@firstImage
        }
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        viewHolder.itemView.marketplace_single_row_rating.text = df.format(itineraryListing.rating).toString()
        ratingBar.rating = itineraryListing.rating
        viewHolder.itemView.marketplace_single_row_title.text = "${itinerary.days.size} days of ${itinerary.title} in ${itinerary.locationName}"
        viewHolder.itemView.marketplace_single_row_raters.text = "($reviewCount)"
    }
}
