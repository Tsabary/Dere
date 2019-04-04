package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.FeedActivity
import co.getdere.GroupieAdapters.LinearFeedImage
import co.getdere.Models.Images
import co.getdere.Models.Users

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_recent_feed.*


class RecentFeedFragment : Fragment() {

    private lateinit var currentUser: Users

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelForRandomUser : SharedViewModelRandomUser


    lateinit var feedRecycler: RecyclerView

    val galleryAdapter = GroupAdapter<ViewHolder>()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_recent_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Feed"

        feedRecycler = view.findViewById(R.id.recent_feed_gallary)

        setUpGalleryAdapter()



        galleryAdapter.setOnItemClickListener { item, _ ->
            val activity = activity as FeedActivity

//            if (activity.subActive != activity.imageFullSizeFragment) {
//
//                activity.subFm.beginTransaction()
//                    .add(R.id.feed_subcontents_frame_container, activity.imageFullSizeFragment, "imageFullSizeFragment")
//                    .commit()
//                activity.subActive = activity.imageFullSizeFragment
//
//            }

//            activity.switchVisibility(1)


            val image = item as LinearFeedImage

            sharedViewModelImage.sharedImageObject.postValue(image.image)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
            activity.subActive = activity.imageFullSizeFragment

            activity.switchVisibility(1)


            // meanwhile in the background it will load the random user object

            val refRandomUser =
                FirebaseDatabase.getInstance().getReference("/users/${image.image.photographer}/profile")

            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val randomUserObject = p0.getValue(Users::class.java)!!

                    sharedViewModelForRandomUser.randomUserObject.postValue(randomUserObject)
                }

            })


//
//            val row = item as LinearFeedImage
//            val action = FeedFragmentDirections.actionDestinationFeedToDestinationImageFullSize(row.image.id, "FeedActivity")
//
//            findNavController().navigate(action)

        }

    }

    private fun setUpGalleryAdapter() {

        val galleryLayoutManager = LinearLayoutManager(this.context)

        feedRecycler.adapter = galleryAdapter
        feedRecycler.layoutManager = galleryLayoutManager
        galleryLayoutManager.reverseLayout = true

        listenToImages()
    }


    private fun listenToImages() { //This needs to be fixed to not update in real time. Or should it?

        galleryAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleImageFromDB = p0.child("body").getValue(Images::class.java)

                if (singleImageFromDB != null) {

                    if (!singleImageFromDB.private) {
                        galleryAdapter.add(LinearFeedImage(singleImageFromDB, currentUser))
                    }
                }
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

    companion object {
        fun newInstance(): RecentFeedFragment = RecentFeedFragment()
    }
}
