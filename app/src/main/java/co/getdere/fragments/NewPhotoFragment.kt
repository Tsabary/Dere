package co.getdere.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import co.getdere.CameraActivity
import co.getdere.adapters.CameraPagerAdapter
import co.getdere.R
import com.google.android.material.tabs.TabLayout


class NewPhotoFragment : Fragment() {

    lateinit var pagerAdapter : CameraPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val viewPager = view.findViewById<ViewPager>(R.id.new_photo_pager_pager)
        pagerAdapter = CameraPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        val activity = activity as CameraActivity

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {

                when(position){

                    0 -> {
                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.photoEditorFragment).commit()
                        activity.subActive = activity.photoEditorFragment
                    }

                    1 -> {
                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.darkRoomEditFragment).commit()
                        activity.subActive = activity.darkRoomEditFragment
                    }

                }

            }

        })

        val tabLayout = view.findViewById<TabLayout>(R.id.new_photo_pager_tab_layout)
        tabLayout.setupWithViewPager(viewPager)
    }



}