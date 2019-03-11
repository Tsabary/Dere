package co.getdere.Fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.getdere.Models.Question

import co.getdere.R
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.text.DateFormat
import java.util.*

class BoardFragment : Fragment() {
    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.setTitle("Board")

        val fab : FloatingActionButton = view.findViewById<FloatingActionButton>(R.id.board_fab)

        fab.setOnClickListener {
            val action = BoardFragmentDirections.actionDestinationBoardToDestinationNewQuestion()
            findNavController().navigate(action)
        }

        val questionsRecycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.board_question_feed)
        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQuestions()

        questionsRecyclerAdapter.setOnItemClickListener { item, view2 ->

            val row = item as SingleQuestion
            val author = row.authorUid
            val question = row.questionId
            val action = BoardFragmentDirections.actionDestinationBoardToOpenedQuestionFragment()
            action.questionId = question
            action.questionAuthor = author
            findNavController().navigate(action)


        }

    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }


    private fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionFromDB = p0.getValue(Question::class.java)


                if (singleQuestionFromDB != null) {

                    val refAnswers = FirebaseDatabase.getInstance().getReference("/answers/${singleQuestionFromDB.id}")

                    var count = 0

                    refAnswers.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            for (ds in p0.getChildren()) {

                                count += 1
                                Log.d("countAnswers", count.toString())

                            }

                            val stampMills = singleQuestionFromDB.timestamp
                            val pretty = PrettyTime()
                            val date = pretty.format(Date(stampMills))

                            questionsRecyclerAdapter.add(
                                SingleQuestion(
                                    singleQuestionFromDB.id,
                                    singleQuestionFromDB.author,
                                    singleQuestionFromDB.title,
                                    singleQuestionFromDB.details,
                                    singleQuestionFromDB.tags,
                                    date,
                                    count,
                                    false
                                )
                            )
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


class SingleQuestion(
    val questionId: String,
    val authorUid: String,
    val question: String,
    val details: String,
    val tags: MutableList<String>,
    val timestamp: String,
    val answers: Int,
    val resolved: Boolean
) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.board_question.text = question
        viewHolder.itemView.board_tags.text = tags.joinToString()
        viewHolder.itemView.board_timestamp.text = timestamp
        viewHolder.itemView.board_answers.text = answers.toString()

        if (resolved) {
            viewHolder.itemView.board_answers.setBackgroundResource(R.drawable.board_resolved_count_background)
            viewHolder.itemView.board_answers.setTextColor(Color.parseColor("#ffffff"))

        } else {
            viewHolder.itemView.board_answers.setBackgroundResource(R.drawable.board_unresolved_count_background)
            viewHolder.itemView.board_answers.setTextColor(Color.parseColor("#212121"))
        }

    }

}
