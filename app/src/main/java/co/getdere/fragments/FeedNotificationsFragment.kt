package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.models.*

import co.getdere.R
import co.getdere.viewmodels.SharedViewModelImage
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.feed_notification_single_row.view.*
import kotlinx.android.synthetic.main.fragment_feeds_layout.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans.font
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class FeedNotificationsFragment : Fragment() {

    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()
    lateinit var notificationRecyclerLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    val uid = FirebaseAuth.getInstance().uid
    val refFeedNotifications =
        FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/gallery")

    lateinit var sharedViewModelImage: SharedViewModelImage
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications()
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        notificationRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        notificationRecyclerLayoutManager.reverseLayout = true
        notificationRecyclerLayoutManager.stackFromEnd = true
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = notificationRecyclerLayoutManager

//        listenToNotifications()

        refFeedNotifications.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                listenToNotifications()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                listenToNotifications()
            }
        })

        notifications_mark_all_as_read.setOnClickListener {
            refFeedNotifications.addListenerForSingleValueEvent(object : ValueEventListener {
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
                            listenToNotifications()
                        }
                    }

                }

                override fun onCancelled(p0: DatabaseError) {}

            })


        }
    }

    fun listenToNotifications() {
        notificationsRecyclerAdapter.clear()

        val refFeedNotificationsByTime =
            FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/gallery").orderByChild("timestamp")

        refFeedNotificationsByTime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                var feedNotCount = 0

                for (i in p0.children) {
                    val notification = i.getValue(Notification::class.java)

                    if (notification != null) {

                        if (notification.seen == 0) {
                            feedNotCount++
                        }
                        (activity as MainActivity).feedNotificationsCount.postValue(feedNotCount)

                        notificationsRecyclerAdapter.add(
                            SingleFeedNotification(
                                notification,
                                activity as MainActivity
                            )
                        )
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}


class SingleFeedNotification(val notification: Notification, val activity: MainActivity) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.feed_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val notificationBox = viewHolder.itemView.feed_notification_box

        val date = PrettyTime().format(Date(notification.timestamp))
        viewHolder.itemView.feed_notification_timestamp.text = date

        activity.let {
            val sharedViewModelImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            val sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)

            viewHolder.itemView.setOnClickListener {

                val uid = FirebaseAuth.getInstance().uid

                val refImageId = FirebaseDatabase.getInstance().getReference("/images/${notification.mainPostId}/body")

                refImageId.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val image = p0.getValue(Images::class.java)

                        if (image != null) {

                            sharedViewModelImage.sharedImageObject.postValue(image)

                            val refRandomUser =
                                FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

                            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val randomUser = p0.getValue(Users::class.java)

                                    if (randomUser != null) {
                                        sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

                                        val notificationRef = FirebaseDatabase.getInstance()
                                            .getReference("/users/$uid/notifications/gallery/${notification.mainPostId}${notification.specificPostId}${notification.initiatorId}${notification.scenarioType}/seen")
                                        notificationRef.setValue(1).addOnSuccessListener {
                                            activity.subFm.beginTransaction().hide(activity.subActive)
                                                .show(activity.imageFullSizeFragment)
                                                .commit()
                                            activity.subActive = activity.imageFullSizeFragment
                                            activity.feedNotificationsFragment.listenToNotifications()
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


        val refImage = FirebaseDatabase.getInstance().getReference("/images/${notification.mainPostId}/body")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val image = p0.getValue(Images::class.java)

                if (image != null) {

                    inflateImages(viewHolder, image)

                    viewHolder.itemView.feed_notification_content.text = when (notification.scenarioType) {

                        8 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" bucketed your photo", font("open_sans_semibold"))
                        }

                        12 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" liked a comment you made", font("open_sans_semibold"))
                        }

                        14 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" liked your photo", font("open_sans_semibold"))
                        }

                        16 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" commented on your photo", font("open_sans_semibold"))
                        }

                        20 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" started following you", font("open_sans_semibold"))
                        }
                        else -> "Notification failed to load"
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
                }
            }
        })
    }

    private fun inflateImages(viewHolder: ViewHolder, images: Images) {

        Glide.with(viewHolder.root.context).load(images.imageSmall)
            .into(viewHolder.itemView.feed_notification_post_image)

        val refInitiator = FirebaseDatabase.getInstance().getReference("/users/${notification.initiatorId}/profile")

        refInitiator.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                if (user != null) {
                    Glide.with(viewHolder.root.context).load(user.image)
                        .into(viewHolder.itemView.feed_notification_initiator_image)
                }

            }

        })


    }
}
