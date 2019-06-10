package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCollection
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_collection_feed.*

class CollectionFeedFragment : Fragment() {


    lateinit var sharedViewModelCollection: SharedViewModelCollection
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users
    val galleryAdapter = GroupAdapter<ViewHolder>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_collection_feed, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity

        val galleryRecycler = bucket_feed_recycler
        galleryRecycler.adapter = galleryAdapter
        galleryRecycler.layoutManager = GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)

        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelCollection = ViewModelProviders.of(it).get(SharedViewModelCollection::class.java)
            sharedViewModelCollection.imageCollection.observe(this, Observer { bucketName ->
                bucketName?.let { bucket ->
                    galleryAdapter.clear()
                    listenToImagesFromCollection(bucket)
                }
            })
        }

        galleryAdapter.setOnItemClickListener { item, _ ->
            val image = item as FeedImage
            sharedViewModelImage.sharedImageObject.postValue(image.image)

            FirebaseDatabase.getInstance().getReference("/users/${image.image.photographer}/profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))

                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.imageFullSizeFragment,
                            "imageFullSizeFragment"
                        ).addToBackStack("imageFullSizeFragment").commit()
                        activity.subActive = activity.imageFullSizeFragment
                    }
                })
        }

    }

    private fun listenToImagesFromCollection(collectionSnapshot: DataSnapshot) {

        galleryAdapter.clear()

        for (image in collectionSnapshot.child("/body/images").children) {

            FirebaseDatabase.getInstance().getReference("/images/${image.key}/body")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val imageObject = p0.getValue(Images::class.java)
                        if (imageObject != null) {
                            galleryAdapter.add(FeedImage(imageObject, 1))
                        }
                    }
                })
        }
    }

    companion object {
        fun newInstance(): CollectionFeedFragment = CollectionFeedFragment()
    }
}
