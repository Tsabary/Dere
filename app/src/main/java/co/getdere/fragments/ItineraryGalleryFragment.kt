package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import co.getdere.R
import co.getdere.adapters.FeedAndMapPagerAdapter
import kotlinx.android.synthetic.main.fragment_itinerary_gallery.*


class ItineraryGalleryFragment : Fragment() {

    lateinit var mapButton : ImageView
    lateinit var galleryViewPager : ViewPager
    var viewPagerPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapButton = itinerary_gallery_map_button

        galleryViewPager= itinerary_gallery_viewpager
        galleryViewPager.adapter = FeedAndMapPagerAdapter(childFragmentManager)

        mapButton.setOnClickListener {
            switchImageAndMap()
        }
    }

    private fun switchImageAndMap() {

        if (viewPagerPosition == 0) {
            galleryViewPager.currentItem = 1
            viewPagerPosition = 1
            mapButton.setImageResource(R.drawable.world_active)

        } else {
            galleryViewPager.currentItem = 0
            viewPagerPosition = 0
            mapButton.setImageResource(R.drawable.world)
        }
    }


    companion object {
        fun newInstance(): ItineraryGalleryFragment = ItineraryGalleryFragment()
    }
}
