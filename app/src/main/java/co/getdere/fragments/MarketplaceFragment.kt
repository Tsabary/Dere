package co.getdere.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.models.Images
import co.getdere.models.ItineraryBody
import co.getdere.models.ItineraryTechnical
import co.getdere.viewmodels.SharedViewModelItinerary
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_marketplace.*
import kotlinx.android.synthetic.main.marketplace_single_row.view.*


class MarketplaceFragment : Fragment() {

    val adapter = GroupAdapter<ViewHolder>()
    lateinit var sharedViewModelItinerary: SharedViewModelItinerary

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marketplace, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.let{
            sharedViewModelItinerary = ViewModelProviders.of(it).get(SharedViewModelItinerary::class.java)
        }

        val recycler = marketplace_recycler
        val layoutManagerStaggered = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManagerStaggered

        listenToItineraries()

        marketplace_swipe_refresh.setOnRefreshListener {
            listenToItineraries()
            marketplace_swipe_refresh.isRefreshing = false
        }

        marketplace_saved_itineraries_icon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).add(R.id.feed_subcontents_frame_container, activity.marketplacePurchasedFragment, "marketplacePurchasedFragment").commit()
            activity.subActive = activity.marketplacePurchasedFragment
            activity.switchVisibility(1)
            activity.isSavedItinerariesActive = true
        }

        adapter.setOnItemClickListener { item, _ ->

            val itinerary = item as SingleItinerary

            val itinerarySnapshot = FirebaseDatabase.getInstance().getReference("/itineraries/${itinerary.itinerary.id}")
            itinerarySnapshot.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    sharedViewModelItinerary.itinerary.postValue(p0)
                    activity.subFm.beginTransaction().hide(activity.subActive).show(activity.itineraryFragment)
                        .commit()
                    activity.subActive = activity.itineraryFragment

                    activity.switchVisibility(1)
                }
            })
        }
    }

    private fun listenToItineraries() {

        adapter.clear()

        val itinerariesRef = FirebaseDatabase.getInstance().getReference("/itineraries")
        itinerariesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itinerarySnapshot in p0.children) {

                    val itinerary = itinerarySnapshot.child("body").getValue(ItineraryBody::class.java)
                    val itineraryListing =
                        itinerarySnapshot.child("listing").getValue(ItineraryTechnical::class.java)

                    if (itinerary != null && itineraryListing != null) {
                        adapter.add(SingleItinerary(itinerary, itineraryListing))
                    }
                }
            }
        })
    }


    companion object {
        fun newInstance(): MarketplaceFragment = MarketplaceFragment()
    }
}

class SingleItinerary(val itinerary: ItineraryBody, val itineraryListing: ItineraryTechnical) :
    Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.marketplace_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        firstImage@for (image in itineraryListing.sampleImages) {

            val imageRef = FirebaseDatabase.getInstance().getReference("/images/${image.key}/body")
            imageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val imageObject = p0.getValue(Images::class.java)!!

                    val imageView = viewHolder.itemView.marketplace_single_row_image
                    (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = imageObject.ratio
                    Glide.with(viewHolder.root.context).load(imageObject.imageBig).into(viewHolder.itemView.marketplace_single_row_image)
                }

            })
            break@firstImage
        }

        viewHolder.itemView.marketplace_single_row_rating.text = itineraryListing.rating.toString()
        viewHolder.itemView.marketplace_single_row_rating_bar.rating = itineraryListing.rating.toFloat()
        viewHolder.itemView.marketplace_single_row_title.text = itinerary.title

    }
}
