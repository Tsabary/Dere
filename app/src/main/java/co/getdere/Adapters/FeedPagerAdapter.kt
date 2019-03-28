package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.FollowingFeedFragment
import co.getdere.Fragments.OpenPhotoSocialBox
import co.getdere.Fragments.RecentFeedFragment

class FeedPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Following", "Recent")

    override fun getItem(p0: Int): Fragment {

        when (p0) {
            0 -> return FollowingFeedFragment.newInstance()
            1 -> return RecentFeedFragment.newInstance()
            else -> return FollowingFeedFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }


    override fun getCount(): Int {
        return 2
    }

}
