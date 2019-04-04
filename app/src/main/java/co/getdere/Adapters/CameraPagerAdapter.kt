package co.getdere.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.Fragments.CameraFragment
import co.getdere.Fragments.DarkRoomFragment

class CameraPagerAdapter (fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Camera", "Dark Room")

    override fun getItem(p0: Int): Fragment {

        when (p0) {
            0 -> return CameraFragment.newInstance()
            1 -> return DarkRoomFragment.newInstance()
            else -> return CameraFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }


    override fun getCount(): Int {
        return 2
    }

}
