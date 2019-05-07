package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class BucketGalleryPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> BucketFeedFragment.newInstance()
            1 -> BucketMapViewFragment.newInstance()
            else -> BoardFragment.newInstance()
        }
    }



    override fun getCount(): Int {
        return 2
    }

}
