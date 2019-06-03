package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class ItineraryGalleryChildPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Itinerary", "All Locations")


    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> ItineraryDaysFragment.newInstance()
            1 -> CollectionFeedFragment.newInstance()
            else -> BoardFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return 2
    }

}
