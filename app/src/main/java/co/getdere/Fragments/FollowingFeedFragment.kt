package co.getdere.Fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.CameraActivity2
import co.getdere.GroupieAdapters.LinearFeedImage
import co.getdere.MainActivity
import co.getdere.Models.Images
import co.getdere.Models.Users
import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelFollowedAccounts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_following_feed.*
import kotlinx.android.synthetic.main.fragment_following_feed.view.*

class FollowingFeedFragment : Fragment() {

//        private lateinit var currentUser: Users
    lateinit var sharedViewModelFollowedAccounts: SharedViewModelFollowedAccounts

    val uid = FirebaseAuth.getInstance().uid

    val galleryAdapter = GroupAdapter<ViewHolder>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val myView = inflater.inflate(R.layout.fragment_following_feed, container, false)

        activity?.let {
            //            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelFollowedAccounts = ViewModelProviders.of(it).get(SharedViewModelFollowedAccounts::class.java)
        }

        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)
                setUpGalleryAdapter(myView, currentUser!!)


            }


        })


        createFollowedAccountsList()

        return myView

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activity!!.title = "Feed"


        galleryAdapter.setOnItemClickListener { item, _ ->

            val row = item as LinearFeedImage
            val action = FeedFragmentDirections.actionDestinationFeedToDestinationImageFullSize()
            action.imageId = row.image.id
            findNavController().navigate(action)

        }

    }

    private fun setUpGalleryAdapter(myView: View, currentUser: Users) {

        myView.following_feed_gallary.adapter = galleryAdapter
        val galleryLayoutManager = LinearLayoutManager(this.context)
        galleryLayoutManager.reverseLayout = true

        myView.following_feed_gallary.layoutManager = galleryLayoutManager

        listenToImages(currentUser)

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
