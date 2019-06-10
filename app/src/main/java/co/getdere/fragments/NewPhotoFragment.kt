package co.getdere.fragments


import android.graphics.Color
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
import kotlinx.android.synthetic.main.fragment_new_photo.*


class NewPhotoFragment : Fragment() {

    private lateinit var pagerAdapter: CameraPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_new_photo, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as CameraActivity

        val tabLayout = new_photo_pager_tab_layout

        val viewPager = new_photo_pager_pager
        pagerAdapter = CameraPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter


        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                viewPagerController(activity, position, tabLayout)
            }

            override fun onPageSelected(position: Int) {
                viewPagerController(activity, position, tabLayout)
            }
        })

        tabLayout.setupWithViewPager(viewPager)
    }

    private fun viewPagerController(activity: CameraActivity, position : Int, tabLayout: TabLayout){
        when (position) {
            0 -> {
            }

            1 -> {
                tabLayout.setBackgroundColor(Color.parseColor("#FF616161"))
            }
        }
    }
}
