package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

class OnBoardingPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> RecentFeedOnBoardingFragment.newInstance()
            1 -> AddToBucketFragment.newInstance()
            else -> OnBoardWelcomeFragment.newInstance()
        }

//        return when (p0) {
//            0 -> OnBoardWelcomeFragment.newInstance()
//            1 -> OnBoardPhotosFragment.newInstance()
//            2 -> OnBoardBucketsFragment.newInstance()
//            3 -> OnBoardBoardFragment.newInstance()
//            4 -> RecentFeedOnBoardingFragment.newInstance()
//            5 -> AddToBucketFragment.newInstance()
//            else -> OnBoardWelcomeFragment.newInstance()
//        }
    }


    override fun getCount(): Int {
        return 2
    }

}
