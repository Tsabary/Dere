package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.GroupieAdapters.LinearFeedImage
import co.getdere.Models.Images
import co.getdere.Models.Users

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_recent_feed.*


class RecentFeedFragment : Fragment() {

    private lateinit var currentUser: Users

    val galleryAdapter = GroupAdapter<ViewHolder>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_recent_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Feed"

        setUpGalleryAdapter()



        galleryAdapter.setOnItemClickListener { item, _ ->

            val row = item as LinearFeedImage
            val action = FeedFragmentDirections.actionDestinationFeedToDestinationImageFullSize()
            action.imageId = row.image.id
            findNavController().navigate(action)

        }

    }

    private fun setUpGalleryAdapter() {

        recent_feed_gallary.adapter = galleryAdapter
        val galleryLayoutManager = LinearLayoutManager(this.context)
        galleryLayoutManager.reverseLayout = true

        recent_feed_gallary.layoutManager = galleryLayoutManager

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
                        galleryAdapter.add(LinearFeedImage(singleImageFromDB, currentUser.name))
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
