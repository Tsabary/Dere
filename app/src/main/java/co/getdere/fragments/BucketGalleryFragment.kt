package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.R
import co.getdere.adapters.BucketGalleryPagerAdapter
import co.getdere.otherClasses.SwipeLockableViewPager
import co.getdere.viewmodels.SharedViewModelBucket
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import kotlinx.android.synthetic.main.fragment_bucket_gallery.*


class BucketGalleryFragment : Fragment() {

    lateinit var sharedViewModelBucket: SharedViewModelBucket
    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser : SharedViewModelRandomUser

    var viewPagerPosition = 0
    lateinit var galleryViewPager : SwipeLockableViewPager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucket_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapButton = bucket_gallery_show_map

        galleryViewPager = bucket_gallery_viewpager
        val pagerAdapter = BucketGalleryPagerAdapter(childFragmentManager)
        galleryViewPager.adapter = pagerAdapter


        mapButton.setOnClickListener {
            switchImageAndMap()
        }

        activity?.let {

            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            sharedViewModelBucket = ViewModelProviders.of(it).get(SharedViewModelBucket::class.java)

            sharedViewModelBucket.sharedBucketObject.observe(this, Observer { dataSnapshot ->
                dataSnapshot?.let { bucket ->

                    bucket_gallery_title.text = bucket.key.toString()
                    bucket_gallery_photos_count.text = bucket.childrenCount.toString() + " photos"

                }
            })

        }

    }

    private fun switchImageAndMap() {

        if(viewPagerPosition ==0){
            galleryViewPager.currentItem = 1
            viewPagerPosition = 1
        } else {
            galleryViewPager.currentItem = 0
            viewPagerPosition = 0
        }
    }

}
