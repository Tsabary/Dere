package co.getdere.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.models.ItineraryBody
import co.getdere.models.ItineraryTechnical
import co.getdere.viewmodels.SharedViewModelCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_marketplace_purchased.*


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
        val layoutManagerStaggered = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManagerStaggered

        listenToItineraries()

        marketplace_purchased_swipe_refresh.setOnRefreshListener {
            listenToItineraries()
            marketplace_purchased_swipe_refresh.isRefreshing = false
        }

        adapter.setOnItemClickListener { item, _ ->

            val itinerary = item as SingleItinerary

            val itinerarySnapshot =
                FirebaseDatabase.getInstance().getReference("/itineraries/${itinerary.itinerary.id}")
            itinerarySnapshot.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    sharedViewModelCollection.imageCollection.postValue(p0)
                    activity.subFm.beginTransaction().hide(activity.subActive).show(activity.collectionGalleryFragment)
                        .commit()
                    activity.subActive = activity.collectionGalleryFragment
                }
            })
        }
    }

    private fun listenToItineraries() {

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
                                    p0.child("listing").getValue(ItineraryTechnical::class.java)

                                if (itinerary != null && itineraryListing != null) {
                                    adapter.add(SingleItinerary(itinerary, itineraryListing))
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
