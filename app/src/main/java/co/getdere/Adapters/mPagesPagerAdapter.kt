package co.getdere.Adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.ProfileFragment

class mPagesPagerAdapter  internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = FeedFragment()
            1 -> fragment = BoardFragment()
            2 -> fragment = ProfileFragment()
        }

        return fragment
    }

    override fun getCount(): Int {
        return 3
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return "Tab " + (position + 1)
//    }
}
