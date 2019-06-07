package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.models.Images
import co.getdere.models.SharedItineraryBody
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_join_shared_itinerary.*

class JoinSharedItineraryFragment : Fragment() {

    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var currentUser: Users
    val imagesAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_shared_itinerary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        val imagesRecycler = join_shared_itinerary_teaser_recycler
        val joinCta = join_shared_itinerary_cta

        joinCta.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive)
                .add(R.id.feed_subcontents_frame_container, activity.buyItineraryFragment, "buyItineraryFragment")
                .addToBackStack("buyItineraryFragment").commit()
            activity.subActive = activity.buyItineraryFragment
        }

        imagesRecycler.adapter = imagesAdapter
        imagesRecycler.layoutManager = GridLayoutManager(this.context, 4)

        activity.let {

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)

            sharedViewModelCollection.imageCollection.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { collectionSnapshot ->

                    val sharedItinerary = collectionSnapshot.child("body").getValue(SharedItineraryBody::class.java)

                    if (sharedItinerary != null) {
                        join_shared_itinerary_title.text = sharedItinerary.title + " in " + sharedItinerary.locationName
                        joinCta.text = "Join itinerary ($" + (sharedItinerary.originalPrice/2-0.01)+ ")"
                        var imageCount = 0

                        firstTwelve@ for (imagePath in sharedItinerary.images) {
                            if (imageCount == 12) {
                                break@firstTwelve
                            } else {
                                imageCount++
                                FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            val image = p0.getValue(Images::class.java)
                                            if (image != null) {
                                                imagesAdapter.add(
                                                    SampleImages(image, activity)
                                                )
                                            }
                                        }
                                    })
                            }

                        }
                    }
                }
            })
        }
    }
}

