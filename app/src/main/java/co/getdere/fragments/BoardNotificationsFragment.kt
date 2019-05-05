package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.models.Notification
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
import kotlinx.android.synthetic.main.feed_notification_single_row.view.*
import kotlinx.android.synthetic.main.fragment_notifications.*
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
    ): View? = inflater.inflate(R.layout.fragment_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications()
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        val notificationRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = notificationRecyclerLayoutManager

//        listenToNotifications()

        refBoardNotifications.addChildEventListener(object : ChildEventListener{
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

                        val activity = activity as MainActivity

                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment)
                            .commit()
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

        val refBoardNotificationsByTime = FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/board").orderByChild("timestamp")

        refBoardNotifications.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                var boardNotCount = 0

                for (i in p0.children) {
                    val notification = i.getValue(Notification::class.java)

                    if (notification != null) {

                        if (notification.seen ==0){
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


class SingleBoardNotification(val notification: Notification, val activity: MainActivity) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val notificationBox = viewHolder.itemView.board_notification_box
        val date = PrettyTime().format(Date(notification.timestamp))
        viewHolder.itemView.board_notification_timestamp.text = date


        activity.let {
            val sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            val sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)


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

                                            activity.subFm.beginTransaction().hide(activity.subActive)
                                                .show(activity.openedQuestionFragment)
                                                .commit()
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

        val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/${notification.mainPostId}/main/body")

        refQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val question = p0.getValue(Question::class.java)

                if (question != null) {

                    inflateInitiatorImage(viewHolder, notification.scenarioType)


                    viewHolder.itemView.board_notification_content.text = when (notification.scenarioType) {

                        0 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" upvoted your question ", Spans.font("open_sans_semibold"))
                                .append(question.title)
                        }

                        2 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" upvoted your answer on the question ", Spans.font("open_sans_semibold"))
                                .append(question.title)
                        }

                        4 -> {
                            if (notification.mainPostId == notification.specificPostId) {
                                Spanner()
                                    .append("Someone")
                                    .append(" downvoted your question ", Spans.font("open_sans_semibold"))
                                    .append(question.title)
                            } else {
                                Spanner()
                                    .append("Someone")
                                    .append(" downvoted your answer to the question ", Spans.font("open_sans_semibold"))
                                    .append(question.title)
                            }
                        }

                        6 -> {

                            Spanner()
                                .append(notification.initiatorName)
                                .append(" answered your question ", Spans.font("open_sans_semibold"))
                                .append(question.title)
                        }

                        10 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" saved your question ", Spans.font("open_sans_semibold"))
                                .append(question.title)
                        }

                        18 -> {
                            Spanner()
                                .append(notification.initiatorName)
                                .append(" commented on your answer to the question ", Spans.font("open_sans_semibold"))
                                .append(question.title)
                        }
                        else -> "Notification failed to load"
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

    private fun inflateInitiatorImage(viewHolder: ViewHolder, case: Int) {

        if (case == 4) {
            Glide.with(viewHolder.root.context).load(R.drawable.user_profile)
                .into(viewHolder.itemView.board_notification_initiator_image)
        } else {
            val refInitiator = FirebaseDatabase.getInstance().getReference("/users/${notification.initiatorId}/profile")

            refInitiator.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Users::class.java)

                    if (user != null) {
                        Glide.with(viewHolder.root.context).load(user.image)
                            .into(viewHolder.itemView.board_notification_initiator_image)
                    }
                }
            })
        }
    }
}
