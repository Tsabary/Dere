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
import co.getdere.viewmodels.SharedViewModelBucket
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_bucket_feed.*
import kotlinx.android.synthetic.main.fragment_bucket_gallery.*

class BucketFeedFragment : Fragment() {


    lateinit var sharedViewModelBucket: SharedViewModelBucket
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users
    val galleryAdapter = GroupAdapter<ViewHolder>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucket_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val galleryRecycler = bucket_feed_recycler

        val imagesRecyclerLayoutManager =
            GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)

        galleryRecycler.adapter = galleryAdapter
        galleryRecycler.layoutManager = imagesRecyclerLayoutManager

        activity?.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelBucket = ViewModelProviders.of(it).get(SharedViewModelBucket::class.java)

            sharedViewModelBucket.sharedBucketId.observe(this, Observer { bucketName ->
                bucketName?.let { bucket ->

                    galleryAdapter.clear()


                    for (image in bucket.children){

                        val imagePath = image.key

                        val imageObjectPath =
                            FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")

                        imageObjectPath.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val imageObject = p0.getValue(Images::class.java)

                                galleryAdapter.add(FeedImage(imageObject!!))

                            }
                        })


                    }

                }
            })

        }


        galleryAdapter.setOnItemClickListener { item, _ ->

            val image = item as FeedImage
            sharedViewModelImage.sharedImageObject.postValue(image.image)

            val userRef = FirebaseDatabase.getInstance().getReference("/users/${image.image.photographer}/profile")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))

                    val activity = activity as MainActivity

                    activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment)
                        .commit()
                    activity.subActive = activity.imageFullSizeFragment

                    activity.switchVisibility(1)

                }

            })

        }

    }


    companion object {
        fun newInstance(): BucketFeedFragment = BucketFeedFragment()
    }


}
