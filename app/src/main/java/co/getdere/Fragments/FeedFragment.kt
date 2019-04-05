package co.getdere.Fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import co.getdere.Adapters.FeedPagerAdapter
import co.getdere.CameraActivity
import co.getdere.FeedActivity
import co.getdere.Models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import com.google.android.material.tabs.TabLayout


class FeedFragment : Fragment() {

//    lateinit var mToolbar: Toolbar

    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var currentUser: Users


    lateinit var feedActivity: Activity

    lateinit var pagerAdapter : FeedPagerAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        feedActivity = activity as FeedActivity


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            setHasOptionsMenu(true)


        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(co.getdere.R.layout.fragment_feed, container, false)
//        val activity = activity as FeedActivity
//        mToolbar = view.findViewById(R.id.my_toolbar)
//        activity.setSupportActionBar(mToolbar)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager>(co.getdere.R.id.feed_pager_pager)
        pagerAdapter = FeedPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        val tabLayout = view.findViewById<TabLayout>(co.getdere.R.id.feed_pager_tab_layout)
        tabLayout.setupWithViewPager(viewPager)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(co.getdere.R.menu.feed_navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val activity = activity as FeedActivity

        when (id) {
            co.getdere.R.id.destination_camera -> {

                if (hasNoPermissions()) {
                    requestPermission()
                } else {

                    val intent = Intent(activity, CameraActivity::class.java)
                    startActivity(intent)
                }
            }

            co.getdere.R.id.destination_feed_notifications -> {

                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.feedNotificationsFragment).commit()
                activity.subActive = activity.feedNotificationsFragment

                activity.switchVisibility(1)

            }

        }

        return super.onOptionsItemSelected(item)

    }


    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity as FeedActivity, permissions, 0)
    }


//    override fun onSaveInstanceState(state: Bundle) {
//        super.onSaveInstanceState(state)
//
//        state.putParcelable(LIST_STATE_KEY, layoutManager.onSaveInstanceState())
//    }
//











    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }

}
