package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager

import co.getdere.R
import co.getdere.adapters.BucketAndSharedItineraryAdapter
import kotlinx.android.synthetic.main.fragment_bucket_and_shared_itinerary_pager.*

class BucketAndSharedItineraryPagerFragment : Fragment() {

    var viewPagerPosition = 0
    lateinit var viewpager: ViewPager
    lateinit var rightArrow: ImageView
    lateinit var leftArrow: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucket_and_shared_itinerary_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewpager = bucket_and_shared_itinerary_viewpager
        viewpager.adapter = BucketAndSharedItineraryAdapter(childFragmentManager)

        rightArrow = bucket_and_shared_itinerary_right
        leftArrow = bucket_and_shared_itinerary_left

        rightArrow.setOnClickListener {
            switchBucketAndItinerary()
        }

        leftArrow.setOnClickListener {
            switchBucketAndItinerary()
        }
    }

    private fun switchBucketAndItinerary() {
        if (viewPagerPosition == 0) {
            goToItinerary()
        } else {
            goToBucket()
        }
    }

    private fun goToItinerary() {
        viewpager.currentItem = 1
        viewPagerPosition = 1
        rightArrow.visibility = View.GONE
        leftArrow.visibility = View.VISIBLE
    }

    private fun goToBucket() {
        viewpager.currentItem = 0
        viewPagerPosition = 0
        rightArrow.visibility = View.VISIBLE
        leftArrow.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        goToBucket()
    }

    companion object {
        fun newInstance(): BucketAndSharedItineraryPagerFragment = BucketAndSharedItineraryPagerFragment()
    }

}
