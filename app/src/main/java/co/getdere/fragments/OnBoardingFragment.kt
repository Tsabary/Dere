package co.getdere.fragments


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.adapters.OnBoardingPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_on_boarding.*

class OnBoardingFragment : Fragment() {


    val title1 = "Welcome to Dere"
    val subTitle1 = "All your future travel destinations in one place, filtered by your interests."

    val title2 = "Photos"
    val subTitle2 = "Are all geo-tagged, so saving a photo adds it to your map."

    val title3 = "Buckets"
    val subTitle3 =
        "Are like binders for your dreams. Save and arrange photos in different buckets."

    val title4 = "Board"
    val subTitle4 = "Is where you talk to other explorers. Ask questions and share your knowledge."

    val title5 = "Interests"
    val subTitle5 =
        "Are what filters the content you see in the app, and are saved automatically as you use Dere. Press and hold your finger on a photo to create your first bucket and collect its tags to your interests"


    var viewPagerPosition = 0

    lateinit var viewPager: ViewPager

    lateinit var title: TextView
    lateinit var subTitle: TextView
    lateinit var button: TextView

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val activity = activity as MainActivity

        activity.mBottomNav.visibility = View.GONE

        return inflater.inflate(R.layout.fragment_on_boarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = on_boarding_viewpager
        val pagerAdapter = OnBoardingPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        button = on_boarding_button

        title = on_boarding_title
        subTitle = on_boarding_subtitle

        button.setOnClickListener {
            when (viewPagerPosition) {
                0 -> {
                    setUpItem2()
                }

                1 -> {
                    setUpItem3()

                }

                2 -> {
                    setUpItem4()
                }

                3 -> {
                    setUpItem5()
                }

                4 -> {
                    finishOnBoarding()
                }

                5 -> {
                    finishOnBoarding()
                }
            }
        }
    }

    fun finishOnBoarding(){

        val activity = activity as MainActivity

        val uid = FirebaseAuth.getInstance().uid
        val interestsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/interests")
        interestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()) {
                    if (hasNoPermissions()) {
                        requestPermission()
                    } else {
                        activity.fm.beginTransaction().remove(activity.active).show(activity.feedFragment)
                            .commit()
                        activity.active = activity.feedFragment
                        activity.mBottomNav.visibility = View.VISIBLE

                        Toast.makeText(activity, "Start exploring!", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    Toast.makeText(activity, "Please create your first bucket", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    fun setUpItem1() {
        title.text = title1
        subTitle.text = subTitle1
        viewPager.currentItem = 0
        viewPagerPosition = 0
    }

    fun setUpItem2() {
        title.text = title2
        subTitle.text = subTitle2
        viewPager.currentItem = 1
        viewPagerPosition = 1
    }

    fun setUpItem3() {
        title.text = title3
        subTitle.text = subTitle3
        viewPager.currentItem = 2
        viewPagerPosition = 2
    }

    fun setUpItem4() {
        title.text = title4
        subTitle.text = subTitle4
        viewPager.currentItem = 3
        viewPagerPosition = 3
        button.text = "Next"

    }

    fun setUpItem5() {
        title.text = title5
        subTitle.text = subTitle5
        viewPager.currentItem = 4
        viewPagerPosition = 4
        button.text = "Finish"
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
        ActivityCompat.requestPermissions(activity!!, permissions, 0)
    }
}
