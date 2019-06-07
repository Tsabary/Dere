package co.getdere.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.models.*
import co.getdere.viewmodels.SharedViewModelCollection
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_marketplace_purchased.*
import kotlinx.android.synthetic.main.single_purchased_itinerary.view.*


class MarketplacePurchasedFragment : Fragment() {

    val adapter = GroupAdapter<ViewHolder>()
    lateinit var sharedViewModelCollection: SharedViewModelCollection
    val uid = FirebaseAuth.getInstance().uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marketplace_purchased, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.let {
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
        }

        val recycler = marketplace_purchased_recycler
        val layoutManagerStaggered = LinearLayoutManager(this.context)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManagerStaggered

        listenToItineraries()

        marketplace_purchased_swipe_refresh.setOnRefreshListener {
            listenToItineraries()
            marketplace_purchased_swipe_refresh.isRefreshing = false
        }

        adapter.setOnItemClickListener { item, _ ->

            val itinerary = item as SinglePurchasedItinerary

            val itinerarySnapshot =
                FirebaseDatabase.getInstance().getReference(
                    "/sharedItineraries/${itinerary.itinerary.child(
                        "body/id"
                    ).value}"
                )
            itinerarySnapshot.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    sharedViewModelCollection.imageCollection.postValue(p0)
                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.collectionGalleryFragment,
                        "collectionGalleryFragment"
                    ).addToBackStack("collectionGalleryFragment")
                        .commit()
                    activity.subActive = activity.collectionGalleryFragment
                    activity.isCollectionGalleryActive = true
                }
            })
        }
    }


    fun listenToItineraries() {

        adapter.clear()


        FirebaseDatabase.getInstance().getReference("/users/$uid/sharedItineraries")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    for (itineraryPath in p0.children) {

                        FirebaseDatabase.getInstance().getReference("/sharedItineraries/${itineraryPath.key}")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    adapter.add(SinglePurchasedItinerary(p0))
                                }
                            })
                    }
                }
            })
    }


    fun listenToItineraries2() {

        adapter.clear()

        val userPurchasedItinerariesRef =
            FirebaseDatabase.getInstance().getReference("/users/$uid/purchasedItineraries")

        val itinerariesRef = FirebaseDatabase.getInstance().getReference("/itineraries")

        userPurchasedItinerariesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                for (itineraryPath in p0.children) {
                    itinerariesRef.child(itineraryPath.key!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {


                                val itinerary = p0.child("body").getValue(ItineraryBody::class.java)
                                val itineraryListing =
                                    p0.child("listing").getValue(ItineraryListing::class.java)
                                val itineraryBudget =
                                    p0.child("listing").getValue(ItineraryBudget::class.java)

                                if (itinerary != null && itineraryListing != null && itineraryBudget != null) {
                                    adapter.add(SingleItinerary(itinerary, itineraryListing, itineraryBudget))
                                }

                            }
                        })
                }
            }
        })
    }


    companion object {
        fun newInstance(): MarketplacePurchasedFragment = MarketplacePurchasedFragment()
    }
}

class SinglePurchasedItinerary(val itinerary: DataSnapshot) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.single_purchased_itinerary
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val purchasedItinerary = itinerary.child("body").getValue(SharedItineraryBody::class.java)

        if (purchasedItinerary != null) {

            firstImage@ for (image in purchasedItinerary.images) {

                val imageRef = FirebaseDatabase.getInstance().getReference("/images/${image.key}/body")
                imageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val imageObject = p0.getValue(Images::class.java)!!

                        val imageView = viewHolder.itemView.purchased_itinerary_image
                        (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = imageObject.ratio
                        Glide.with(viewHolder.root.context).load(imageObject.imageBig)
                            .into(imageView)
                    }

                })
                break@firstImage
            }

            viewHolder.itemView.purchased_itinerary_title.text = purchasedItinerary.title + " in " + purchasedItinerary.locationName
            viewHolder.itemView.purchased_itinerary_location.text = purchasedItinerary.locationName
            viewHolder.itemView.purchased_itinerary_days.text = "${purchasedItinerary.days.size} days"

        }
    }

}
