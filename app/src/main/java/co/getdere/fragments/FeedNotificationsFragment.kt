package co.getdere.fragments


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity
import co.getdere.MainActivity
import co.getdere.models.*

import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_notification_single_row.view.*
import kotlinx.android.synthetic.main.feed_toolbar.*
import kotlinx.android.synthetic.main.fragment_notifications_feed.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans.font
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class FeedNotificationsFragment : Fragment() {

    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()
    private lateinit var notificationRecyclerLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    val uid = FirebaseAuth.getInstance().uid

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var currentUser: Users

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications_feed, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        val notificationBadge = feed_toolbar_notifications_badge

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications(currentUser)
            notifications_swipe_refresh.isRefreshing = false
        }

        activity.feedNotificationsCount.observe(this, androidx.lifecycle.Observer {
            it?.let { notCount ->
                notificationBadge.setNumber(notCount)
            }
        })


        val notificationsRecycler = notifications_recycler
        notificationRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        notificationRecyclerLayoutManager.reverseLayout = true
        notificationRecyclerLayoutManager.stackFromEnd = true
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = notificationRecyclerLayoutManager

        val notificationIcon = feed_toolbar_notifications_icon
        val feedToCamera = feed_toolbar_camera_icon

        notificationIcon.setImageResource(R.drawable.notification_bell_active)

        listenToNotifications(currentUser)

        notificationIcon.setOnClickListener {
            listenToNotifications(currentUser)
        }

        notificationBadge.setOnClickListener {
            listenToNotifications(currentUser)
        }

        feedToCamera.setOnClickListener {
            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(activity, CameraActivity::class.java)
                startActivity(intent)
            }
        }

        notifications_mark_all_as_read.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/gallery")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {

                        var itirations = 0
                        val childrenCount = p0.childrenCount.toInt()

                        for (i in p0.children) {
                            val notificationsRef =
                                FirebaseDatabase.getInstance()
                                    .getReference("/users/$uid/notifications/gallery/${i.key}/seen")
                            notificationsRef.setValue(1)
                            itirations++
                            if (itirations == childrenCount) {
                                notificationsRecyclerAdapter.clear()
                                listenToNotifications(currentUser)
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                })
        }
    }

    fun listenToNotifications(currentUser: Users) {
        val activity = activity as MainActivity

        notificationsRecyclerAdapter.clear()

        FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/gallery").orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    var feedNotCount = 0

                    for (i in p0.children) {
                        val notification = i.getValue(NotificationFeed::class.java)

                        if (notification != null) {

                            if (notification.seen == 0) {
                                feedNotCount++
                            }
                            activity.feedNotificationsCount.postValue(feedNotCount)

                            notificationsRecyclerAdapter.add(
                                SingleFeedNotification(
                                    notification,
                                    activity,
                                    currentUser
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
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
}


class SingleFeedNotification(val notification: NotificationFeed, val activity: MainActivity, val currentUser: Users) :
    Item<ViewHolder>() {
    override fun getLayout(): Int = R.layout.feed_notification_single_row


    override fun bind(viewHolder: ViewHolder, position: Int) {

        val notificationBox = viewHolder.itemView.feed_notification_box

        viewHolder.itemView.feed_notification_timestamp.text = PrettyTime().format(Date(notification.timestamp))

        activity.let {
            val sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            val sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            viewHolder.itemView.feed_notification_initiator_image.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("/users/${notification.initiatorId}/profile")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                            activity.subFm.beginTransaction()
                                .add(
                                    R.id.feed_subcontents_frame_container,
                                    activity.profileRandomUserFragment,
                                    "profileRandomUserFragment"
                                )
                                .addToBackStack("profileRandomUserFragment").commit()
                            activity.subActive = activity.profileRandomUserFragment
                        }
                    })
            }


            viewHolder.itemView.setOnClickListener {

                FirebaseDatabase.getInstance().getReference("/images/${notification.mainPostId}/body")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val image = p0.getValue(Images::class.java)

                            if (image != null) {

                                sharedViewModelImage.sharedImageObject.postValue(image)

                                FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            val randomUser = p0.getValue(Users::class.java)

                                            if (randomUser != null) {
                                                sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

                                                FirebaseDatabase.getInstance()
                                                    .getReference("/users/${currentUser.uid}/notifications/gallery/${notification.mainPostId}${notification.specificPostId}${notification.initiatorId}${notification.scenarioType}/seen")
                                                    .setValue(1).addOnSuccessListener {
                                                        activity.subFm.beginTransaction()
                                                            .add(
                                                                R.id.feed_subcontents_frame_container,
                                                                activity.imageFullSizeFragment,
                                                                "imageFullSizeFragment"
                                                            )
                                                            .addToBackStack("imageFullSizeFragment").commit()
                                                        activity.subActive = activity.imageFullSizeFragment
                                                        activity.feedNotificationsFragment.listenToNotifications(
                                                            currentUser
                                                        )
                                                    }
                                            }
                                        }
                                    })
                            }
                        }
                    })
            }
        }



        if (notification.seen == 0) {
            notificationBox.setBackgroundColor(ContextCompat.getColor(viewHolder.root.context, R.color.green50))
        } else {
            notificationBox.setBackgroundColor(ContextCompat.getColor(viewHolder.root.context, R.color.white))
        }

        Glide.with(viewHolder.root.context).load(
            if (notification.initiatorImage.isNotEmpty()) {
                notification.initiatorImage
            } else {
                R.drawable.user_profile
            }
        )
            .into(viewHolder.itemView.feed_notification_initiator_image)

        Glide.with(viewHolder.root.context).load(
            if (notification.mainPostImage.isNotEmpty()) {
                notification.mainPostImage
            } else {
                android.R.color.transparent
            }
        )
            .into(viewHolder.itemView.feed_notification_post_image)

        viewHolder.itemView.feed_notification_content.text = when (notification.scenarioType) {

            8 -> {
                Spanner()
                    .append(notification.initiatorName)
                    .append(" bucketed your photo", font("roboto_medium"))
            }

            12 -> {
                Spanner()
                    .append(notification.initiatorName)
                    .append(" liked a comment you made", font("roboto_medium"))
            }

            14 -> {
                Spanner()
                    .append(notification.initiatorName)
                    .append(" liked your photo", font("roboto_medium"))
            }

            16 -> {
                Spanner()
                    .append(notification.initiatorName)
                    .append(" commented on your photo", font("roboto_medium"))
            }

            20 -> {
                Spanner()
                    .append(notification.initiatorName)
                    .append(" started following you", font("roboto_medium"))
            }
            else -> "NotificationBoard failed to load"
        }
    }
}


/*

8 : photo bucketed +15 to receiver +notification  // type 2            ***not implemented yet***
12 : comment receives a like +1 to receiver +notification  // type 3            ***not implemented yet***
14 : photo receives a like +2 to receiver +notification  // type 2
16 : photo receives a comment



post types:

0: question
1: answer
2: image
3: comment
*/