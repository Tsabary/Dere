package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.getdere.FeedActivity
import co.getdere.Models.*

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelImage
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_notification_single_row.view.*
import kotlinx.android.synthetic.main.feed_notification_single_row.view.*


class FeedNotificationsFragment : Fragment() {

    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid

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
    ): View? = inflater.inflate(R.layout.fragment_feed_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Notifications"

        notificationsRecyclerAdapter.clear()

        val notificationsRecycler = view.findViewById<RecyclerView>(R.id.feed_notifications_recycler)
        val notificationRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = notificationRecyclerLayoutManager


        val refBoardNotifications =
            FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/gallery")

        refBoardNotifications.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val notification = p0.getValue(Notification::class.java)

                if (notification != null) {
                    notificationsRecyclerAdapter.add(SingleFeedNotification(notification))
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


        notificationsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleFeedNotification

            val refImageId = FirebaseDatabase.getInstance().getReference("/images/${row.notification.mainPostId}/body")

            refImageId.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val image = p0.getValue(Images::class.java)

                    if (image != null) {

                        sharedViewModelImage.sharedImageObject.postValue(image)

                        val activity = activity as FeedActivity

                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
                        activity.subActive = activity.imageFullSizeFragment


                        val refRandomUser =
                            FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

                        refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val randomUser = p0.getValue(Users::class.java)

                                if (randomUser != null) {
                                    sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

//                                    val action =
//                                        FeedNotificationsFragmentDirections.actionDestinationFeedNotificationsToDestinationImageFullSize(image.id, "FeedActivity")
//                                    findNavController().navigate(action)

                                }
                            }
                        })
                    }
                }

            })

        }

    }
}


class SingleFeedNotification(val notification: Notification) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.feed_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val refImage = FirebaseDatabase.getInstance().getReference("/images/${notification.mainPostId}/body")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val image = p0.getValue(Images::class.java)

                if (image != null) {

                    inflateImages(viewHolder, image)

                    when (notification.scenarioType) {

                        8 -> {
                            val notificationText =
                                "${notification.initiatorName} bucketed your photo"

                            viewHolder.itemView.feed_notification_content.text = notificationText

                        }

                        12 -> {
                            val notificationText =
                                "${notification.initiatorName} liked a comment you made"

                            viewHolder.itemView.feed_notification_content.text = notificationText

                        }

                        14 -> {
                            val notificationText =
                                "${notification.initiatorName} liked your photo"

                            viewHolder.itemView.feed_notification_content.text = notificationText

                        }

                        16 -> {
                            val notificationText =
                                "${notification.initiatorName} commented on your photo"

                            viewHolder.itemView.feed_notification_content.text = notificationText

                        }

                        20 -> {
                            val notificationText =
                                "${notification.initiatorName} started following you"

                            viewHolder.itemView.feed_notification_content.text = notificationText

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


//                        val myTypeface = Typeface.create(
//                            ResourcesCompat.getFont(viewHolder.root.context, R.font.open_sans_semibold),
//                            Typeface.BOLD)
//
//                        val string = SpannableString("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//                        string.setSpan(TypefaceSpan(myTypeface), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


//                        string.setSpan(TypefaceSpan("monospace"), 19, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


//                        val notificationText =
//                            SpannableStringBuilder("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//
//                        val notText = Spanner("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//                            .append(notification.initiatorName)
//                            .append(" has upvoted your answer on the question ", sizeDP(45))
//                            .append(question.title)

//                        notificationText.setSpan(android.text.style.TypefaceSpan(R.font.open_sans_bold))


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
