package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.models.NotificationBoard
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelQuestion
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_notification_single_row.view.*
import kotlinx.android.synthetic.main.board_toolbar.*
import kotlinx.android.synthetic.main.fragment_notifications_feed.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class BoardNotificationsFragment : Fragment() {

    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid
    val refBoardNotifications =
        FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/board")

    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val boardNotificationBadge = board_toolbar_notifications_badge
        val activity = activity as MainActivity

        activity.boardNotificationsCount.observe(this, androidx.lifecycle.Observer {
            it?.let { notCount ->
                boardNotificationBadge.setNumber(notCount)
            }
        })

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications()
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

        val boardNotificationIcon = board_toolbar_notifications_icon
        val boardSavedQuestionIcon = board_toolbar_saved_questions_icon

        listenToNotifications()

        boardNotificationIcon.setImageResource(
            R.drawable.notification_bell_active
        )

        boardNotificationIcon.setOnClickListener {
            listenToNotifications()
        }

        boardNotificationBadge.setOnClickListener {
            listenToNotifications()
        }

        boardSavedQuestionIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.boardNotificationsFragment).show(activity.savedQuestionFragment).commit()
            activity.subActive = activity.savedQuestionFragment
        }


        notifications_mark_all_as_read.setOnClickListener {
            refBoardNotifications.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    var iterations = 0
                    val childrenCount = p0.childrenCount.toInt()

                    for (i in p0.children) {
                        val notificationsRef =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/$uid/notifications/board/${i.key}/seen")
                        notificationsRef.setValue(1)
                        iterations++
                        if (iterations == childrenCount) {
                            notificationsRecyclerAdapter.clear()
                            listenToNotifications()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}

            })
        }



        notificationsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleBoardNotification

            val refQuestionId =
                FirebaseDatabase.getInstance().getReference("/questions/${row.notification.mainPostId}/main/body")

            refQuestionId.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val question = p0.getValue(Question::class.java)

                    if (question != null) {

                        sharedViewModelQuestion.questionObject.postValue(question)

                        activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.openedQuestionFragment, "openedQuestionFragment").addToBackStack("openedQuestionFragment").commit()

                        activity.subActive = activity.openedQuestionFragment

                        val refRandomUser =
                            FirebaseDatabase.getInstance().getReference("/users/${question.author}/profile")

                        refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val randomUser = p0.getValue(Users::class.java)

                                if (randomUser != null) {
                                    sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

                                }
                            }
                        })
                    }
                }
            })
        }
    }

    fun listenToNotifications() {
        notificationsRecyclerAdapter.clear()

        val refBoardNotificationsByTime =
            FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/board").orderByChild("timestamp")

        refBoardNotificationsByTime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                var boardNotCount = 0

                for (i in p0.children) {
                    val notification = i.getValue(NotificationBoard::class.java)

                    if (notification != null) {

                        if (notification.seen == 0) {
                            boardNotCount++
                        }

                        (activity as MainActivity).boardNotificationsCount.postValue(boardNotCount)

                        notificationsRecyclerAdapter.add(
                            SingleBoardNotification(
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


class SingleBoardNotification(val notification: NotificationBoard, val activity: MainActivity) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val notificationBox = viewHolder.itemView.board_notification_box

        viewHolder.itemView.board_notification_timestamp.text = PrettyTime().format(Date(notification.timestamp))


        activity.let {
            val sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            val sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)


            viewHolder.itemView.board_notification_initiator_image.setOnClickListener {
                val randomUserRef =
                    FirebaseDatabase.getInstance().getReference("/users/${notification.initiatorId}/profile")
                randomUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        sharedViewModelRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                        activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.profileRandomUserFragment, "profileRandomUserFragment").addToBackStack("profileRandomUserFragment").commit()

                        activity.subActive = activity.profileRandomUserFragment
                        activity.isBoardNotificationsActive = true
                    }

                })
            }


            viewHolder.itemView.setOnClickListener {

                val uid = FirebaseAuth.getInstance().uid

                val refQuestionId =
                    FirebaseDatabase.getInstance().getReference("/questions/${notification.mainPostId}/main/body")

                refQuestionId.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val question = p0.getValue(Question::class.java)

                        if (question != null) {

                            sharedViewModelQuestion.questionObject.postValue(question)

                            val refRandomUser =
                                FirebaseDatabase.getInstance().getReference("/users/${question.author}/profile")

                            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val randomUser = p0.getValue(Users::class.java)

                                    if (randomUser != null) {
                                        sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

                                        val notificationRef = FirebaseDatabase.getInstance()
                                            .getReference("/users/$uid/notifications/board/${notification.mainPostId}${notification.specificPostId}${notification.initiatorId}${notification.scenarioType}/seen")
                                        notificationRef.setValue(1).addOnSuccessListener {

                                            activity.boardNotificationsFragment.listenToNotifications()

//                                        notificationBox.setBackgroundColor(
//                                            ContextCompat.getColor(
//                                                viewHolder.root.context,
//                                                R.color.white
//                                            )
//                                        )

                                            activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.openedQuestionFragment, "openedQuestionFragment").addToBackStack("openedQuestionFragment").commit()

                                            activity.subActive = activity.openedQuestionFragment


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

        if (notification.scenarioType == 4) {
            Glide.with(viewHolder.root.context).load(R.drawable.user_profile)
                .into(viewHolder.itemView.board_notification_initiator_image)
        } else {
            Glide.with(viewHolder.root.context).load(
                if (notification.initiatorImage.isNotEmpty()) {
                    notification.initiatorImage
                } else {
                    R.drawable.user_profile
                }
            )
                .into(viewHolder.itemView.board_notification_initiator_image)
        }

        val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/${notification.mainPostId}/main/body")

        refQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val question = p0.getValue(Question::class.java)

                if (question != null) {

                    viewHolder.itemView.board_notification_content.text = when (notification.scenarioType) {

                        0 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" upvoted your question ", Spans.font("roboto_medium"))
                                .append(question.title)
                        }

                        2 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" upvoted your answer on the question ", Spans.font("roboto_medium"))
                                .append(question.title)
                        }

                        4 -> {
                            if (notification.mainPostId == notification.specificPostId) {
                                Spanner()
                                    .append("Someone")
                                    .append(" downvoted your question ", Spans.font("roboto_medium"))
                                    .append(question.title)
                            } else {
                                Spanner()
                                    .append("Someone")
                                    .append(" downvoted your answer to the question ", Spans.font("roboto_medium"))
                                    .append(question.title)
                            }
                        }

                        6 -> {

                            Spanner()
                                .append(notification.initiatorName)
                                .append(" answered your question ", Spans.font("roboto_medium"))
                                .append(question.title)
                        }

                        10 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" saved your question ", Spans.font("roboto_medium"))
                                .append(question.title)
                        }

                        18 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" commented on your answer to the question ", Spans.font("roboto_medium"))
                                .append(question.title)
                        }
                        else -> "NotificationBoard failed to load"
                    }


                    /*

                    1 : question upvoted
                    3 : answer upvoted
                    4 : question or answer downvoted
                    10 : question saved

                     */
                }
            }
        })
    }
}
