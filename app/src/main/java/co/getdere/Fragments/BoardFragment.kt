package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.GroupieAdapters.SingleQuestion
import co.getdere.Models.Question
import co.getdere.Models.Users

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelQuestion
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class BoardFragment : Fragment() {

    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    lateinit var sharedViewModelForQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser


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
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Board"

        val fab: FloatingActionButton = view.findViewById(R.id.board_fab)

        fab.setOnClickListener {
            val action = BoardFragmentDirections.actionDestinationBoardToDestinationNewQuestion()
            findNavController().navigate(action)
        }

        val questionsRecycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.board_question_feed)
        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQuestions()

        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleQuestion
            val author = row.question.author
            val question = row.question.id

            val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/$question/main/body")

            refQuestion.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val questionFromDB = p0.getValue(Question::class.java)

                    sharedViewModelForQuestion.questionObject.postValue(questionFromDB)

                    val refRandomUser = FirebaseDatabase.getInstance().getReference("/users/$author/profile")

                    refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val randomUserFromDB = p0.getValue(Users::class.java)

                            sharedViewModelRandomUser.randomUserObject.postValue(randomUserFromDB)


                        }

                    })

                }


            })

            val action = BoardFragmentDirections.actionDestinationBoardToOpenedQuestionFragment()
//            action.questionId = question
//            action.questionAuthor = author
            findNavController().navigate(action)


        }

    }


    private fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionFromDB = p0.child("main").child("body").getValue(Question::class.java)


                if (singleQuestionFromDB != null) {

                    questionsRecyclerAdapter.add(SingleQuestion(singleQuestionFromDB))

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

        when (id) {
            R.id.destination_saved -> {

                val action = BoardFragmentDirections.actionDestinationBoardToSavedQuestionsFragment()
                findNavController().navigate(action)

            }

            R.id.destination_board_notifications -> {

                val action = BoardFragmentDirections.actionDestinationBoardToBoardNotificationsFragment()
                findNavController().navigate(action)

            }

        }

        return super.onOptionsItemSelected(item)

    }





    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

}


//class SingleQuestion(
//    val questionId: String,
//    val authorUid: String,
//    val question: String,
//    val details: String,
//    val tags: MutableList<String>,
//    val timestamp: String,
//    val answers: Int,
//    val resolved: Boolean
//) : Item<ViewHolder>() {
//    override fun getLayout(): Int {
//        return R.layout.board_single_row
//    }
//
//    override fun bind(viewHolder: ViewHolder, position: Int) {
//
//        viewHolder.itemView.board_question.text = question
//        viewHolder.itemView.board_tags.text = tags.joinToString()
//        viewHolder.itemView.board_timestamp.text = timestamp
//        viewHolder.itemView.board_answers.text = answers.toString()
//
//    }
//
//}



