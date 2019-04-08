package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.groupieAdapters.LinearFeedImage
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelFollowedAccounts
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


open class FollowingFeedFragment : Fragment() {

    lateinit var sharedViewModelFollowedAccounts: SharedViewModelFollowedAccounts

    lateinit var sharedViewModelImage: SharedViewModelImage

    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

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

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            createFollowedAccountsList()

        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val myView = inflater.inflate(R.layout.fragment_following_feed, container, false)

        feedRecycler = myView.findViewById(R.id.following_feed_gallery)

        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Feed"

        setUpGalleryAdapter()


        galleryAdapter.setOnItemClickListener { item, _ ->
            val activity = activity as MainActivity

            val image = item as LinearFeedImage

            sharedViewModelImage.sharedImageObject.postValue(image.image)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()

            activity.switchVisibility(1)

            activity.subActive = activity.imageFullSizeFragment



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

        }

    }

    private fun setUpGalleryAdapter() {


        galleryLayoutManager = LinearLayoutManager(this.context)
        feedRecycler.adapter = galleryAdapter
        feedRecycler.layoutManager = galleryLayoutManager
        galleryLayoutManager.reverseLayout = true

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
