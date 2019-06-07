package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class FeedAndMapPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> CollectionFeedFragment.newInstance()
            1 -> CollectionMapViewFragment.newInstance()
            else -> BoardFragment.newInstance()
        }
    }



    override fun getCount(): Int {
        return 2
    }

}
