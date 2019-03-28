package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.FollowingFeedFragment
import co.getdere.Fragments.OpenPhotoSocialBox
import co.getdere.Fragments.RecentFeedFragment

class FeedPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        when (p0) {
            0 -> return RecentFeedFragment.newInstance()
            1 -> return FollowingFeedFragment.newInstance()
            else -> return OpenPhotoSocialBox.newInstance()
        }
    }


    override fun getCount(): Int {
        return 2
    }

}
