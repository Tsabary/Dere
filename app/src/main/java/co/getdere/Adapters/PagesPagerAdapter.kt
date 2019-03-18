package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.FeedFragment

class PagesPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int) : Fragment {

        val actualPosition = p0 % 3
        when (actualPosition) {

            0 -> return FeedFragment.newInstance()
            1 -> return BoardFragment.newInstance()
            else -> return FeedFragment.newInstance()
        }
    }


    override fun getCount(): Int {
        return 200
    }
}
