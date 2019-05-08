package co.getdere.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import co.getdere.CameraActivity
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.adapters.FeedPagerAdapter
import co.getdere.groupieAdapters.LinearFeedImage
import co.getdere.groupieAdapters.SingleQuestion
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelTags
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_toolbar.*
import kotlinx.android.synthetic.main.feed_toolbar.*
import kotlinx.android.synthetic.main.fragment_board.*
import kotlinx.android.synthetic.main.fragment_feed.*


class FeedFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelTags: SharedViewModelTags

    private lateinit var searchedImagesRecycler : RecyclerView
    private lateinit var feedFilterChipGroup : ChipGroup

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    val searchedImagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var viewPager : ViewPager


    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var currentUser: Users

    private lateinit var pagerAdapter : FeedPagerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val feedSearchBox = feed_toolbar_search_box
        val tagSuggestionRecycler = feed_search_tags_recycler
        feedFilterChipGroup = feed_toolbar_chipgroup

        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

        searchedImagesRecycler = feed_search_results_recycler
        val searchedImagesRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        searchedImagesRecyclerLayoutManager.reverseLayout = true
        searchedImagesRecycler.adapter = searchedImagesRecyclerAdapter
        searchedImagesRecycler.layoutManager = searchedImagesRecyclerLayoutManager

        val notificationBadge = feed_toolbar_notifications_badge

        activity.feedNotificationsCount.observe(this, Observer {
            it?.let { notCount ->
                notificationBadge.setNumber(notCount)
            }
        })


        feedSearchBox.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {

                    for (tag in sharedViewModelTags.tagList){
                        println(tag.tagString)
                    }


                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {
                        tagSuggestionRecycler.visibility = View.VISIBLE
                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })


        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (feedFilterChipGroup.childCount == 0) {
                onTagSelected(row.tag.tagString)
                feedSearchBox.text.clear()
                searchImages(row.tag.tagString)
                recyclersVisibility(1)
                closeKeyboard(activity)
            } else {
                Toast.makeText(this.context, "You can only search one tag_unactive at a time", Toast.LENGTH_LONG).show()
            }
        }




        viewPager = feed_pager_pager
        pagerAdapter = FeedPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        val tabLayout = feed_pager_tab_layout
        tabLayout.setupWithViewPager(viewPager)

        val feedNotifications = feed_toolbar_notifications_icon
        val feedNotificationsBadge = feed_toolbar_notifications_badge

        val feedToCamera = feed_toolbar_camera_icon

        feedNotificationsBadge.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.feedNotificationsFragment).commit()
            activity.subActive = activity.feedNotificationsFragment
            activity.switchVisibility(1)
            activity.isFeedNotificationsActive = true
            activity.feedNotificationsFragment.listenToNotifications()
        }

        feedNotifications.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.feedNotificationsFragment).commit()
            activity.subActive = activity.feedNotificationsFragment
            activity.switchVisibility(1)
            activity.isFeedNotificationsActive = true
            activity.feedNotificationsFragment.listenToNotifications()
        }

        feedToCamera.setOnClickListener {
            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(activity, CameraActivity::class.java)
                startActivity(intent)
            }
        }

        recyclersVisibility(0)
    }




    private fun searchImages(searchedTag: String) { //This needs to be fixed to not update in real time. Or should it?

        searchedImagesRecyclerAdapter.clear()


        val ref = FirebaseDatabase.getInstance().getReference("/images")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleImageFromDB = p0.child("body").getValue(Images::class.java)

                if (singleImageFromDB != null) {

                    checkMatchWithPhoto@for (imageTag in singleImageFromDB.tags) {

                        if (imageTag == searchedTag) {
                            if (!singleImageFromDB.private) {
                                searchedImagesRecyclerAdapter.add(LinearFeedImage(singleImageFromDB, currentUser, activity as MainActivity))
                                break@checkMatchWithPhoto
                            }
                        }

                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }




    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.green700)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            feedFilterChipGroup.removeView(it)
            recyclersVisibility(0)
        }

        feedFilterChipGroup.addView(chip)
        feedFilterChipGroup.visibility = View.VISIBLE

    }


    private fun recyclersVisibility(case : Int){

        if (case == 0){
            searchedImagesRecycler.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
        } else {
            searchedImagesRecycler.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(co.getdere.R.menu.feed_navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)

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
        ActivityCompat.requestPermissions(activity as MainActivity, permissions, 0)
    }


    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }

}
