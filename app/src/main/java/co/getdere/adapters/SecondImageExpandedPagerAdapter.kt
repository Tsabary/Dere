package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class SecondImageExpandedPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        when (p0) {
            0 -> return SecondImageFragment.newInstance()
            1 -> return SecondImageMapViewFragment.newInstance()
            else -> return SecondImageFragment.newInstance()
        }
    }



    override fun getCount(): Int {
        return 2
    }

}
