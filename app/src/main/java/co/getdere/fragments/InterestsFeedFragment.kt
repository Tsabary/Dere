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
import co.getdere.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feeds_layout.*


open class InterestsFeedFragment : Fragment() {

    lateinit var sharedViewModelImage: SharedViewModelImage

    lateinit var sharedViewModelForRandomUser: SharedViewModelRandomUser

    lateinit var sharedViewModelInterests: SharedViewModelInterests


    lateinit var currentUser: Users

    lateinit var feedRecycler: RecyclerView
    val galleryAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid

    lateinit var galleryLayoutManager: LinearLayoutManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            sharedViewModelInterests = ViewModelProviders.of(it).get(SharedViewModelInterests::class.java)


            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

//            createFollowedAccountsList()

        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feeds_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedRecycler = following_feed_gallery

        setUpGalleryAdapter()

        feed_swipe_refresh.setOnRefreshListener {
            listenToImages(currentUser)
            feed_swipe_refresh.isRefreshing = false
        }

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

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                for (i in p0.children){

                    val singleImageFromDB = i.child("body").getValue(Images::class.java)

                    if (singleImageFromDB != null) {

                        Log.d("AccountPhoto", singleImageFromDB.photographer)


                        val completedInterestsList = sharedViewModelInterests.interestList

                        singlePhotoLoop@ for (tag in singleImageFromDB.tags) {

                            for (interest in completedInterestsList) {

                                Log.d("AccountFromList", interest)

                                if (interest == tag) {
                                    if (!singleImageFromDB.private) {
                                        galleryAdapter.add(
                                            LinearFeedImage(
                                                singleImageFromDB,
                                                currentUser,
                                                activity as MainActivity
                                            )
                                        )
                                        break@singlePhotoLoop
                                    }
                                }

                            }

                        }


                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }


    companion object {
        fun newInstance(): InterestsFeedFragment = InterestsFeedFragment()
    }

}
