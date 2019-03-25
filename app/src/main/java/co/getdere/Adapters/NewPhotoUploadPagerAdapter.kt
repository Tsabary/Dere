package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.util.SparseArray
import co.getdere.Fragments.*
import java.lang.ref.WeakReference

class NewPhotoUploadPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(p0: Int): Fragment {

        when (p0) {
            0 -> return ImageDescriptionFragment.newInstance()
            1 -> return ImageInformationFragment.newInstance()
            else -> return ImageDescriptionFragment.newInstance()
        }
    }


    override fun getCount(): Int {
        return 2
    }

}
