package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.groupieAdapters.BoardRow
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_toolbar.*
import kotlinx.android.synthetic.main.fragment_saved_questions.view.*


class SavedQuestionsFragment : Fragment() {

    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var currentUser: Users

    lateinit var sharedViewModelForQuestion: SharedViewModelQuestion


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)

        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_saved_questions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        val questionsRecycler = view.saved_questions_recycler

        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        val boardNotificationIcon = board_toolbar_notifications_icon
        val boardSavedQuestionIcon = board_toolbar_saved_questions_icon

        boardNotificationIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.boardNotificationsFragment)
                .commit()
            activity.subActive = activity.boardNotificationsFragment
        }
        boardSavedQuestionIcon.setImageResource(R.drawable.bookmark_active)
        boardSavedQuestionIcon.setOnClickListener {
            listenToQuestions()
        }

        listenToQuestions()



        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as BoardRow

            sharedViewModelForQuestion.questionObject.postValue(row.question)
        }
    }


    private fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/saved-questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionPathFromDB = p0.key


                if (singleQuestionPathFromDB != null) {


                    val refQuestionObject = FirebaseDatabase.getInstance()
                        .getReference("/questions/$singleQuestionPathFromDB/main/body")

                    refQuestionObject.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val singleQuestionObjectFromDB = p0.getValue(Question::class.java)

                            if (singleQuestionObjectFromDB != null) {
                                questionsRecyclerAdapter.add(BoardRow(singleQuestionObjectFromDB))

                            }

                        }

                    })


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


}
