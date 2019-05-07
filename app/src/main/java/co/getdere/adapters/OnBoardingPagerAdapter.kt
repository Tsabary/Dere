package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class OnBoardingPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> ImageFragment.newInstance()
            1 -> ImageFragment.newInstance()
            2 -> ImageFragment.newInstance()
            3 -> ImageFragment.newInstance()
            else -> ImageFragment.newInstance()
        }
    }



    override fun getCount(): Int {
        return 4
    }

}
