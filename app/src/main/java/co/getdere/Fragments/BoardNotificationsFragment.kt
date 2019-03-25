package co.getdere.Fragments


import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.Models.BoardNotification
import co.getdere.Models.Question
import co.getdere.Models.Users

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelQuestion
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_notification_single_row.view.*
import kotlinx.android.synthetic.main.fragment_board_notifications.view.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans.font
import lt.neworld.spanner.Spans.sizeDP

class BoardNotificationsFragment : Fragment() {


    val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    lateinit var currentUserObject: Users
    lateinit var sharedViewModelQuestion: SharedViewModelQuestion

    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUserObject = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
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
            FirebaseDatabase.getInstance().getReference("/users/${currentUserObject.uid}/notifications/board")

        refBoardNotifications.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val notification = p0.getValue(BoardNotification::class.java)

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

            val refQuestionId = FirebaseDatabase.getInstance().getReference("/questions/${row.notification.postId}/main/body")

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

                                if (randomUser != null){
                                    sharedViewModelRandomUser.randomUserObject.postValue(randomUser)

                                    val action =
                                        BoardNotificationsFragmentDirections.actionDestinationBoardNotificationsToDestinationQuestionOpened()
                                    findNavController().navigate(action)

                                }
                            }
                        })
                    }
                }

            })

        }

    }


//    fun setUpNotificationRecycler(){
//
//
//    }
}


class SingleBoardNotification(val notification: BoardNotification) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_notification_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/${notification.postId}/body")

        refQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @RequiresApi(Build.VERSION_CODES.P)
            override fun onDataChange(p0: DataSnapshot) {

                val question = p0.getValue(Question::class.java)

                if (question != null) {

                    if (notification.contentDecider == 0) {

                        val notificationText =
                            "${notification.initiatorName} has upvoted your question ${question.title}"

                        viewHolder.itemView.board_notification_content.text = notificationText

                    } else {


//                        val myTypeface = Typeface.create(
//                            ResourcesCompat.getFont(viewHolder.root.context, R.font.open_sans_semibold),
//                            Typeface.BOLD)
//
//                        val string = SpannableString("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//                        string.setSpan(TypefaceSpan(myTypeface), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


//                        string.setSpan(TypefaceSpan("monospace"), 19, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


                        val text =
                            "${notification.initiatorName} has upvoted your answer on the question ${question.title}"

//                        val notificationText =
//                            SpannableStringBuilder("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//
//                        val notText = Spanner("${notification.initiatorName} has upvoted your answer on the question ${question.title}")
//                            .append(notification.initiatorName)
//                            .append(" has upvoted your answer on the question ", sizeDP(45))
//                            .append(question.title)

//                        notificationText.setSpan(android.text.style.TypefaceSpan(R.font.open_sans_bold))

                        viewHolder.itemView.board_notification_content.text = text

                    }


                }

            }

        })


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
