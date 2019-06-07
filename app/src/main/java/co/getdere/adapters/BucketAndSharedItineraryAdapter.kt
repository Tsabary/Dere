package co.getdere.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.getdere.fragments.*
import com.google.firebase.auth.FirebaseAuth

class BucketAndSharedItineraryAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


    override fun getItem(p0: Int): Fragment {


        return when (p0) {
            0 -> AddToBucketFragment.newInstance()
            1 -> AddToSharedItineraryFragment.newInstance()
            else -> AddToBucketFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 2
    }


}
