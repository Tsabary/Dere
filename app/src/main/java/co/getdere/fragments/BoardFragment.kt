package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.groupieAdapters.SingleQuestion
import co.getdere.models.Question
import co.getdere.models.Users

import co.getdere.R
import co.getdere.viewmodels.SharedViewModelInterests
import co.getdere.viewmodels.SharedViewModelQuestion
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class BoardFragment : Fragment() {

    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    lateinit var sharedViewModelForQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelInterests: SharedViewModelInterests


    var interestsList: MutableList<String> = mutableListOf()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            sharedViewModelForQuestion.questionObject.postValue(Question())

            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(SharedViewModelInterests::class.java)
        }

        val uid = FirebaseAuth.getInstance().uid

        val tagsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/interests")

        tagsRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val tagName = p0.key.toString()

                interestsList.add(tagName)

                sharedViewModelInterests.interestList = interestsList
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Board"

        val activity = activity as MainActivity


        val fab: FloatingActionButton = view.findViewById(R.id.board_fab)

        fab.setOnClickListener {

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.newQuestionFragment).commit()
            activity.subActive = activity.newQuestionFragment

            activity.switchVisibility(1)
        }

        val questionsRecycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.board_question_feed)
        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionRecyclerLayoutManager.reverseLayout = true
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQuestions()

        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleQuestion
            val author = row.question.author
            val question = row.question


            sharedViewModelForQuestion.questionObject.postValue(question)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment).commit()
            activity.subActive = activity.openedQuestionFragment


            activity.switchVisibility(1)

            activity.subActive = activity.openedQuestionFragment


            // meanwhile in the background it will load the random user object

            val refRandomUser = FirebaseDatabase.getInstance().getReference("/users/$author/profile")

            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val randomUserFromDB = p0.getValue(Users::class.java)

                    sharedViewModelRandomUser.randomUserObject.postValue(randomUserFromDB)
                }

            })
        }

    }


    private fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionFromDB = p0.child("main").child("body").getValue(Question::class.java)


                if (singleQuestionFromDB != null) {

                    singleQuestionLoop@for (interest in interestsList) {

                        for (tag in singleQuestionFromDB.tags) {

                            if (interest == tag) {
                                questionsRecyclerAdapter.add(SingleQuestion(singleQuestionFromDB))
                                break@singleQuestionLoop
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.board_navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        val activity = activity as MainActivity

        when (id) {
            R.id.destination_saved -> {

                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.savedQuestionFragment).commit()
                activity.subActive = activity.savedQuestionFragment

                activity.switchVisibility(1)

            }

            R.id.destination_board_notifications -> {

                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.boardNotificationsFragment)
                    .commit()
                activity.subActive = activity.boardNotificationsFragment

                activity.switchVisibility(1)

            }

        }

        return super.onOptionsItemSelected(item)

    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

}