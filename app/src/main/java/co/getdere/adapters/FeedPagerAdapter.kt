package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.FollowingFeedFragment
import co.getdere.fragments.InterestsFeedFragment
import co.getdere.fragments.RecentFeedFragment
import co.getdere.fragments.RecentFeedOnBoardingFragment
import com.google.firebase.auth.FirebaseAuth

class FeedPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Interests", "Following", "All")
    val uid = FirebaseAuth.getInstance().uid

    override fun getItem(p0: Int): Fragment {


        return when (p0) {
            0 -> InterestsFeedFragment.newInstance()
            1 -> FollowingFeedFragment.newInstance()
            2 -> RecentFeedFragment.newInstance()
            else -> InterestsFeedFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }


    override fun getCount(): Int {

        return 3
    }

}
