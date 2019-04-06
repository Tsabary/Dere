package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*

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
