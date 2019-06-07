package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.models.ItineraryBody
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_to_collection_row.view.*
import kotlinx.android.synthetic.main.fragment_add_to_collection.*


class AddToItineraryFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var imageObject: Images
    private lateinit var currentUser: Users
    lateinit var recycler: RecyclerView

    val itinerariesAdapter = GroupAdapter<ViewHolder>()
    private lateinit var itinerariesLayoutManager: LinearLayoutManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForImage.sharedImageObject.observe(this, Observer { images ->
                images?.let { image ->
                    imageObject = image
                }
            })
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_collection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        add_to_collection_title.text = getString(R.string.add_to_itinerary)

        recycler = add_to_collection_recycler
        recycler.adapter = itinerariesAdapter
        itinerariesLayoutManager = LinearLayoutManager(this.context)
        recycler.layoutManager = itinerariesLayoutManager

        listenToItineraryOptions()

        val newItineraryInput = add_to_collection_new_input
        newItineraryInput.hint = "Name your itinerary"
        val addNewButton = add_to_collection_new_button

        addNewButton.setOnClickListener {

            val itineraryImages: MutableMap<String, Boolean> = mutableMapOf()


            if (newItineraryInput.text.isNotEmpty()) {
                val itineraryName = newItineraryInput.text.toString().trimEnd()
                itineraryImages[imageObject.id] = true

                val refItineraries = FirebaseDatabase.getInstance().getReference("/itineraries").push()
                val refUserItineraries =
                    FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/itineraries/")
                val refImageItineraries =
                    FirebaseDatabase.getInstance().getReference("/images/${imageObject.id}/itineraries")

                val newItinerary = ItineraryBody(
                    refItineraries.key!!,
                    false,
                    currentUser.uid,
                    itineraryName,
                    "",
                    itineraryImages,
                    listOf(),
                    0,
                    "",
                    ""
                )

                recycler.visibility = View.VISIBLE


                refItineraries.child("body").setValue(newItinerary).addOnSuccessListener {
                    refUserItineraries.child(refItineraries.key!!).setValue(true)
                        .addOnSuccessListener {
                            refImageItineraries.child(refItineraries.key!!).setValue(true)

                            itinerariesAdapter.add(
                                SingleItinerarySuggestion(
                                    newItinerary,
                                    imageObject,
                                    currentUser,
                                    activity
                                )
                            )


                            closeKeyboard(activity)

                            activity.profileLoggedInUserFragment.listenToItineraries()

                            newItineraryInput.text.clear()

                            val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                            firebaseAnalytics.logEvent("image_added_to_itinerary", null)
                        }

                }

            } else {
                Toast.makeText(this.context, "Please give your new itinerary a name", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun listenToItineraryOptions() {

        itinerariesAdapter.clear()


        val itinerariesRef = FirebaseDatabase.getInstance().getReference("/itineraries")
        val userRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("itineraries")) {
                    recycler.visibility = View.VISIBLE

                    for (itineraryPath in p0.child("itineraries").children) {

                        if (itineraryPath.key != null) {

                            itinerariesRef.child(itineraryPath.key!!).child("body")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        val itineraryObject = p0.getValue(ItineraryBody::class.java)
                                        if (itineraryObject != null) {
                                            itinerariesAdapter.add(
                                                SingleItinerarySuggestion(
                                                    itineraryObject,
                                                    imageObject,
                                                    currentUser,
                                                    activity as MainActivity
                                                )
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

    companion object {
        fun newInstance(): AddToBucketFragment = AddToBucketFragment()
    }
}


class SingleItinerarySuggestion(
    val itinerary: ItineraryBody,
    val image: Images,
    val currentUser: Users,
    val activity: Activity
) : Item<ViewHolder>(),
    DereMethods {


    val uid = FirebaseAuth.getInstance().uid

    override fun getLayout(): Int {
        return R.layout.add_to_collection_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.add_to_collection_name.setText(itinerary.title)
        Log.d("itinerary", itinerary.title)

        val actionText = viewHolder.itemView.add_to_collection_add

        executeItinerary(
            0,
            itinerary,
            image,
            currentUser,
            actionText,
            viewHolder.root.context,
            activity as MainActivity
        )

        actionText.setOnClickListener {
            executeItinerary(1, itinerary, image, currentUser, actionText, viewHolder.root.context, activity)
        }


    }


    private fun executeItinerary(
        case: Int,
        itinerary: ItineraryBody,
        image: Images,
        currentUser: Users,
        actionText: TextView,
        context: Context,
        activity: MainActivity
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val refUserItineraries =
            FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/itineraries/${itinerary.id}")
        val refImageItineraries =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/itineraries/${itinerary.id}")
        val refItinerary =
            FirebaseDatabase.getInstance().getReference("/itineraries/${itinerary.id}/body/images")

        refItinerary.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(image.id)) {
                    if (case == 1) {

                        refUserItineraries.child(image.id).removeValue()
                            .addOnSuccessListener {

                                refImageItineraries.removeValue().addOnSuccessListener {

                                    refItinerary.child(image.id).removeValue().addOnSuccessListener {

                                        firebaseAnalytics.logEvent("image_removed_from_itinerary", null)

                                        checkIfInItinerary(
                                            activity.imageFullSizeFragment.collectButton,
                                            image,
                                            currentUser.uid
                                        )

                                        activity.profileLoggedInUserFragment.listenToItineraries()

                                        actionText.text = "Add"
                                        actionText.setTextColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.green700
                                            )
                                        )

//                                        activity.sharedViewModelImage.sharedImageObject.postValue(Images())
//                                        activity.sharedViewModelImage.sharedImageObject.postValue(image) //not needed?

//                                        activity.profileLoggedInUserFragment.listenToImagesFromCollection() change to listen to itineraries

//                                    activity.addToBucketFragment.listenToBuckets()


                                    }
                                }
                            }
                    } else {
                        actionText.text = "Remove"
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.gray500
                            )
                        )
                    }

                } else {
                    if (case == 1) {
                        refUserItineraries.setValue(true).addOnSuccessListener {
                            refImageItineraries.setValue(true).addOnSuccessListener {

                                refItinerary.child(image.id).setValue(true).addOnSuccessListener {

                                    actionText.text = "Remove"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.gray500
                                        )
                                    )


//                                                activity.sharedViewModelImage.sharedImageObject.postValue(Images())
//                                                activity.sharedViewModelImage.sharedImageObject.postValue(image) not needed

                                    activity.profileLoggedInUserFragment.listenToItineraries()

//                                                        activity.addToBucketFragment.listenToBuckets()

                                    checkIfInItinerary(
                                        activity.imageFullSizeFragment.collectButton,
                                        image,
                                        currentUser.uid
                                    )

                                    refUserItineraries.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        override fun onDataChange(p0allBuckets: DataSnapshot) {

                                            var existsInOtherBuckets = 0

                                            for (bucket in p0allBuckets.children) {
                                                if (bucket.hasChild(image.id)) {
                                                    existsInOtherBuckets++
                                                    Log.d(
                                                        "existanceCountReached",
                                                        existsInOtherBuckets.toString()
                                                    )
                                                }
                                            }
                                            if (existsInOtherBuckets <= 1) {
                                                firebaseAnalytics.logEvent(
                                                    "image_added_to_itinerary",
                                                    null
                                                )
                                            }
                                        }
                                    })
                                }
                            }
                        }


                    } else {
                        actionText.text = "Add"
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.green700
                            )
                        )
                    }
                }
            }
        })

    }
}
