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

    private val fragments = SparseArray<WeakReference<Fragment>>()

    override fun getItem(p0: Int) = when (p0) {

        0 -> FeedFragment.newInstance()
        1 -> BoardFragment.newInstance()
        2 -> ProfileFragment.newInstance()
        else -> FeedFragment.newInstance()
    }


    override fun getCount(): Int {
        return 3
    }
}

//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val fragment = super.instantiateItem(container, position) as Fragment
//        fragments.put(position, WeakReference(it))
//        return fragment
//    }