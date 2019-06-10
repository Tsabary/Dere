package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.getdere.MainActivity
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.groupieAdapters.StaggeredFeedImageOnBoarding
import co.getdere.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feeds_layout.*


open class RecentFeedOnBoardingFragment : Fragment() {

    var staggeredImageList = mutableListOf<StaggeredFeedImageOnBoarding>()

    lateinit var currentUser: Users

    private lateinit var feedRecycler: RecyclerView
    val staggeredGalleryAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feeds_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        feed_filter_container.visibility = View.GONE

        feedRecycler = feed_gallery
        setUpGalleryAdapter()

        feed_swipe_refresh.setOnRefreshListener {
            listenToImages(currentUser)
            feed_swipe_refresh.isRefreshing = false
        }
    }

    private fun setUpGalleryAdapter() {
        feedRecycler.adapter = staggeredGalleryAdapter
        feedRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        listenToImages(currentUser)
    }

    private fun listenToImages(currentUser: Users) {
        staggeredGalleryAdapter.clear()
        staggeredImageList.clear()

        FirebaseDatabase.getInstance().getReference("/images")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    for (i in p0.children) {
                        val singleImageFromDB = i.child("body").getValue(Images::class.java)

                        if (singleImageFromDB != null) {
                            if (!singleImageFromDB.private) {

                                staggeredImageList.add(
                                    StaggeredFeedImageOnBoarding(
                                        singleImageFromDB,
                                        currentUser,
                                        activity as MainActivity
                                    )
                                )

                                staggeredGalleryAdapter.clear()
                                staggeredGalleryAdapter.addAll(staggeredImageList.reversed())
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    companion object {
        fun newInstance(): RecentFeedOnBoardingFragment = RecentFeedOnBoardingFragment()
    }
}