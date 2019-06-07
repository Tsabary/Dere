package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import co.getdere.fragments.*

class ItineraryGalleryChildPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Itinerary","Summery", "Gallery")


    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> ItineraryDaysFragment.newInstance()
            1 -> ItinerarySummeryFragment.newInstance()
            2 -> ItineraryGalleryFragment.newInstance()
            else -> ItineraryDaysFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return 3
    }

}
