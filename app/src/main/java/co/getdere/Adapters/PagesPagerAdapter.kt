package co.getdere.Adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.CameraFragment
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.ProfileFragment
import java.lang.ref.WeakReference

class PagesPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val fragments = SparseArray<WeakReference<Fragment>>()

    override fun getItem(p0: Int) : Fragment {

        val actualPosition = p0 % 4
        when (actualPosition) {

            0 -> return FeedFragment.newInstance()
            1 -> return BoardFragment.newInstance()
            2 -> return ProfileFragment.newInstance()
            3 -> return CameraFragment()
            else -> return FeedFragment.newInstance()
        }
    }


    override fun getCount(): Int {
        return 200
    }
}

//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val fragment = super.instantiateItem(container, position) as Fragment
//        fragments.put(position, WeakReference(it))
//        return fragment
//    }