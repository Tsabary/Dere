package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.FeedActivity
import co.getdere.Models.Notification
import co.getdere.Models.Question
import co.getdere.Models.Users
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

class BoardNotificationsFragment : Fragment() {

    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    val uid = FirebaseAuth.getInstance().uid

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
    ): View? = inflater.inflate(R.layout.fragment_board_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Notifications"

        notificationsRecyclerAdapter.clear()

        val notificationsRecycler = view.findViewById<RecyclerView>(R.id.board_notifications_recycler)
        val notificationRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = notificationRecyclerLayoutManager


        val refBoardNotifications =
            FirebaseDatabase.getInstance().getReference("/users/$uid/notifications/board")

        refBoardNotifications.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val notification = p0.getValue(Notification::class.java)

                if (notification != null) {
                    notificationsRecyclerAdapter.add(SingleBoardNotification(notification))
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

            val row = item as SingleBoardNotification

            val refQuestionId = FirebaseDatabase.getInstance().getReference("/questions/${row.notification.mainPostId}/main/body")

            refQuestionId.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val question = p0.getValue(Question::class.java)

                    if (question != null) {

                        sharedViewModelQuestion.questionObject.postValue(question)

                        val activity = activity as FeedActivity

                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment).commit()
                        activity.subActive = activity.openedQuestionFragment

                        val refRandomUser =
                            FirebaseDatabase.getInstance().getReference("/users/${question.author}/profile")

                        refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val randomUser = p0.getValue(Users::class.java)

                                if (randomUser != null){
                                    sharedViewModelRandomUser.randomUserObject.postValue(randomUser)
//
//                                    val action =
//                                        BoardNotificationsFragmentDirections.actionDestinationBoardNotificationsToDestinationQuestionOpened()
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


class SingleBoardNotification(val notification: Notification) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/${notification.mainPostId}/main/body")

        refQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val question = p0.getValue(Question::class.java)

                if (question != null) {


                    when (notification.scenarioType){

                        0 -> {
                            val notificationText =
                                "${notification.initiatorName} upvoted your question ${question.title}"

                            viewHolder.itemView.board_notification_content.text = notificationText

                            inflateInitiatorImage(viewHolder, 1)
                        }

                        2 -> {
                            val text =
                                "${notification.initiatorName} upvoted your answer on the question ${question.title}"

                            viewHolder.itemView.board_notification_content.text = text

                            inflateInitiatorImage(viewHolder, 1)
                        }

                        4 -> {
                            if (notification.mainPostId == notification.specificPostId){
                                val text =
                                    "Someone downvoted your question ${question.title}"

                                viewHolder.itemView.board_notification_content.text = text
                                inflateInitiatorImage(viewHolder, 0)

                            } else {
                                val text =
                                    "Someone downvoted your answer to the question ${question.title}"

                                viewHolder.itemView.board_notification_content.text = text
                                inflateInitiatorImage(viewHolder, 0)
                            }
                        }

                        6 -> {
                            val text =
                                "${notification.initiatorName} answered your question ${question.title}"

                            viewHolder.itemView.board_notification_content.text = text
                            inflateInitiatorImage(viewHolder, 1)
                        }

                        10 -> {
                            val notificationText =
                                "${notification.initiatorName} saved your question ${question.title}"

                            viewHolder.itemView.board_notification_content.text = notificationText

                            inflateInitiatorImage(viewHolder, 1)

                        }

                        18 -> {
                            val notificationText =
                                "${notification.initiatorName} commented on your answer to the question ${question.title}"

                            viewHolder.itemView.board_notification_content.text = notificationText

                            inflateInitiatorImage(viewHolder, 1)

                        }


                    }



                        /*

                        1 : question upvoted
                        3 : answer upvoted
                        4 : question or answer downvoted
                        10 : question saved

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

    private fun inflateInitiatorImage(viewHolder : ViewHolder, case : Int){

        if (case == 1){
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
        } else {
            Glide.with(viewHolder.root.context).load(R.drawable.user_profile)
                .into(viewHolder.itemView.board_notification_initiator_image)
        }



    }
}
