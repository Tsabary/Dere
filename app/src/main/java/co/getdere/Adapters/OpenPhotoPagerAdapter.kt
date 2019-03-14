package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.util.SparseArray
import co.getdere.Fragments.BoardFragment
import co.getdere.Fragments.FeedFragment
import co.getdere.Fragments.OpenPhotoSocialBox
import co.getdere.Fragments.ProfileFragment
import java.lang.ref.WeakReference

class OpenPhotoPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int) : Fragment {

        when (p0) {

            0 -> return OpenPhotoSocialBox.newInstance()
            1 -> return BoardFragment.newInstance()
            else -> return OpenPhotoSocialBox.newInstance()
        }
    }


    override fun getCount(): Int {
        return 2
    }
}
