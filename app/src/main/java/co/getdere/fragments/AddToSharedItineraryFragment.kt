package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import co.getdere.models.SharedItineraryBody
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


class AddToSharedItineraryFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var imageObject: Images
    private lateinit var currentUser: Users

    lateinit var recycler: RecyclerView
    val itinerariesAdapter = GroupAdapter<ViewHolder>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_collection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForImage.sharedImageObject.observe(this, Observer { images ->
                images?.let { image ->
                    imageObject = image
                }
            })
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        add_to_collection_new_input.visibility = View.GONE
        add_to_collection_new_button.visibility = View.GONE
        add_to_collection_title.text = "Add to shared itinerary"

        recycler = add_to_collection_recycler
        recycler.adapter = itinerariesAdapter
        recycler.layoutManager = LinearLayoutManager(this.context)

        listenToSharedItineraries()
    }


    private fun listenToSharedItineraries() {

        itinerariesAdapter.clear()

        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChild("sharedItineraries")) {
                        recycler.visibility = View.VISIBLE

                        for (itineraryPath in p0.child("sharedItineraries").children) {

                            FirebaseDatabase.getInstance().getReference("/sharedItineraries/${itineraryPath.key}/body")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {

                                        val sharedItinerary = p0.getValue(SharedItineraryBody::class.java)

                                        if (sharedItinerary != null) {
                                            itinerariesAdapter.add(
                                                SingleSharedItinerarySuggestion(
                                                    sharedItinerary,
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
            })
    }

    companion object {
        fun newInstance(): AddToSharedItineraryFragment = AddToSharedItineraryFragment()
    }
}


class SingleSharedItinerarySuggestion(
    private val sharedItinerary: SharedItineraryBody,
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
        viewHolder.itemView.add_to_collection_name.text = sharedItinerary.title

        val actionText = viewHolder.itemView.add_to_collection_add

        executeSharedItinerary(
            0,
            sharedItinerary,
            image,
            currentUser,
            actionText,
            viewHolder.root.context,
            activity as MainActivity
        )

        actionText.setOnClickListener {
            executeSharedItinerary(
                1,
                sharedItinerary,
                image,
                currentUser,
                actionText,
                viewHolder.root.context,
                activity
            )
        }
    }


    private fun executeSharedItinerary(
        case: Int,
        sharedItinerary: SharedItineraryBody,
        image: Images,
        currentUser: Users,
        actionText: TextView,
        context: Context,
        activity: MainActivity
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val sharedItineraryRef =
            FirebaseDatabase.getInstance().getReference("/sharedItineraries/${sharedItinerary.id}/body/images")

        val imageSharedItinerariesRef =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/sharedItineraries/${sharedItinerary.id}")


        sharedItineraryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(image.id)) {
                    if (case == 1) {
                        sharedItineraryRef.child(image.id).removeValue()
                            .addOnSuccessListener {

                                imageSharedItinerariesRef.removeValue().addOnSuccessListener {
                                    actionText.text = context.getString(R.string.add)
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green700
                                        )
                                    )
                                    activity.marketplacePurchasedFragment.listenToItineraries()
                                }
                            }

                    } else {
                        actionText.text = context.getString(R.string.remove)
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.gray500
                            )
                        )
                    }

                } else {
                    if (case == 1) {
                        sharedItineraryRef.child(image.id).setValue(true)
                            .addOnSuccessListener {

                                imageSharedItinerariesRef.setValue(true).addOnSuccessListener {
                                    actionText.text = context.getString(R.string.remove)
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.gray500
                                        )
                                    )
                                    activity.marketplacePurchasedFragment.listenToItineraries()
                                }
                            }

                    } else {
                        actionText.text = context.getString(R.string.add)
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

