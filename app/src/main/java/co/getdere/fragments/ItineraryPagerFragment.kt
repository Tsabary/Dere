package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.getdere.R
import co.getdere.adapters.ItineraryGalleryChildPagerAdapter
import kotlinx.android.synthetic.main.fragment_itinerary_pager.*


class ItineraryPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_itinerary_pager, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = itinerary_pager_tabs

        val viewPager = itinerary_pager_pager
        viewPager.adapter = ItineraryGalleryChildPagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }

    companion object {
        fun newInstance(): ItineraryPagerFragment = ItineraryPagerFragment()
    }
}
