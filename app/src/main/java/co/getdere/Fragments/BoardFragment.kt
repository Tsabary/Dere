package co.getdere.Fragments


import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.getdere.MainActivity
import co.getdere.Models.Question
import co.getdere.NewQuestionActivity

import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

class BoardFragment : Fragment() {
    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_board, container, false)

        val fab: FloatingActionButton = myView.findViewById(R.id.board_fab)

        fab.setOnClickListener {
//            val intent = Intent(this.context, NewQuestionFragment::class.java)
//            startActivity(intent)

        }


        val questionsRecycler = myView.findViewById<RecyclerView>(R.id.board_question_feed)
        val questionRecyclerLayoutManager = LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQusetions()


        return myView
    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }


    private fun listenToQusetions() {

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionFromDB = p0.getValue(Question::class.java)

                if (singleQuestionFromDB != null) {

//
//                    val p = PrettyTime()
//                    val timeFromDb = singleQuestionFromDB.timestamp
//                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//                    val time : Date = dateFormat.parse(timeFromDb)

                    questionsRecyclerAdapter.add(singleQuestion(
                        singleQuestionFromDB.title,
                        singleQuestionFromDB.details,
                        singleQuestionFromDB.tags,
                        "4 days ago",
                        7,
                        true
                    ))

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


class singleQuestion(
    val question: String,
    val details : String,
    val tags: String,
    val timestamp: String,
    val answers: Int,
    val resolved: Boolean
) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.board_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.board_question.text = question
        viewHolder.itemView.board_tags.text = tags
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
