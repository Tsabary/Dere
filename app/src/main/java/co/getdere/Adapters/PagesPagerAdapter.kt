package co.getdere.Adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.ProfileFragment
import java.lang.ref.WeakReference

class PagesPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int) : Fragment {

        val actualPosition = p0 % 3
        when (actualPosition) {

            0 -> return FeedFragment.newInstance()
            1 -> return BoardFragment.newInstance()
            2 -> return ProfileFragment.newInstance()
            else -> return FeedFragment.newInstance()
        }
    }


    override fun getCount(): Int {
        return 200
    }
}
