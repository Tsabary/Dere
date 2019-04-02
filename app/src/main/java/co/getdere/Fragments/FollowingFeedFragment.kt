package co.getdere.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import co.getdere.ViewModels.SharedViewModelFollowedAccounts
import co.getdere.ViewModels.SharedViewModelImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


open class FollowingFeedFragment : Fragment() {

    lateinit var sharedViewModelFollowedAccounts: SharedViewModelFollowedAccounts

    lateinit var sharedViewModelImage: SharedViewModelImage

    lateinit var currentUser: Users

    lateinit var feedRecycler: RecyclerView
    val galleryAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid

    lateinit var galleryLayoutManager: LinearLayoutManager



    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelFollowedAccounts = ViewModelProviders.of(it).get(SharedViewModelFollowedAccounts::class.java)

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            createFollowedAccountsList()

        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val myView = inflater.inflate(R.layout.fragment_following_feed, container, false)

        feedRecycler = myView.findViewById(R.id.following_feed_gallary)
        setUpGalleryAdapter()

        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activity!!.title = "Feed"


        galleryAdapter.setOnItemClickListener { item, _ ->

            val image = item as LinearFeedImage

            sharedViewModelImage.sharedImageObject.postValue(image.image)

            val activity = activity as FeedActivity

            activity.switchVisibility(1)

//            activity.fm.beginTransaction().replace(R.id.feed_frame_container, activity.imageFullSizeFragment, "imageFullSizeFragment").addToBackStack(null).commit()

//            activity.fm.beginTransaction().hide(activity.active).show(activity.imageFullSizeFragment).commit()
//            activity.active = activity.imageFullSizeFragment

//            val row = item as LinearFeedImage
//            val action =
//                FeedFragmentDirections.actionDestinationFeedToDestinationImageFullSize(row.image.id, "FeedActivity")
//            findNavController().navigate(action)

        }

    }

    private fun setUpGalleryAdapter() {


        galleryLayoutManager = LinearLayoutManager(this.context)
        feedRecycler.adapter = galleryAdapter
        feedRecycler.layoutManager = galleryLayoutManager
        galleryLayoutManager.reverseLayout = true

        listenToImages(currentUser)

        Handler().postDelayed(Runnable { galleryLayoutManager.scrollToPosition(3) }, 2000)
    }





    private fun listenToImages(currentUser: Users) { //This needs to be fixed to not update in real time. Or should it?

        galleryAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleImageFromDB = p0.child("body").getValue(Images::class.java)

                if (singleImageFromDB != null) {

                    Log.d("AccountPhoto", singleImageFromDB.photographer)


                    val completedFollowedAccountsList = sharedViewModelFollowedAccounts.followedAccounts

                    for (accountUid in completedFollowedAccountsList) {

                        Log.d("AccountFromList", accountUid)

                        if (accountUid == singleImageFromDB.photographer) {
                            if (!singleImageFromDB.private) {
                                galleryAdapter.add(LinearFeedImage(singleImageFromDB, currentUser))


                            }
                        }

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

    private fun createFollowedAccountsList() {

        sharedViewModelFollowedAccounts.followedAccounts.clear()


        val followedAccountRef = FirebaseDatabase.getInstance().getReference("/users/$uid/following")

        followedAccountRef.addChildEventListener(object : ChildEventListener {


            var followedUsersList: MutableList<String> = mutableListOf()


            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                Log.d("AccountToListP0", p0.toString())

                val followedUserUid = p0.key.toString()

                Log.d("AccountToList", followedUserUid)

                followedUsersList.add(followedUserUid)

                sharedViewModelFollowedAccounts.followedAccounts = followedUsersList

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
        fun newInstance(): FollowingFeedFragment = FollowingFeedFragment()
    }

}
