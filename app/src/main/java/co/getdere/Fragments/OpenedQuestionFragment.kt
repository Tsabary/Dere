package co.getdere.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.DereMethods
import co.getdere.MainActivity
import co.getdere.Models.Answers
import co.getdere.Models.Question
import co.getdere.Models.Users
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_layout.view.*
import kotlinx.android.synthetic.main.fragment_opened_question.view.*


class OpenedQuestionFragment : Fragment(), DereMethods {

    var postAuthor: Users? = null
    var question: Question? = null
    lateinit var authorUid: String
    lateinit var questionId: String
    val uid = FirebaseAuth.getInstance().uid

    val answersAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opened_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = OpenedQuestionFragmentArgs.fromBundle(it)
            authorUid = safeArgs.questionAuthor
            questionId = safeArgs.questionId

            val refUser = FirebaseDatabase.getInstance().getReference("/users/$authorUid")
            val refQuestion = FirebaseDatabase.getInstance().getReference("/questions/$questionId")

            refQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    question = p0.getValue(Question::class.java)
                    view.opened_question_title.text = question!!.title
                    view.opened_question_content.text = question!!.details
                    view.opened_question_tags.text = question!!.tags

                    view.opened_question_upvote.setOnClickListener {
                        executeVote("up",questionId, uid!!, view.opened_question_votes, view.opened_question_upvote, view.opened_question_downvote)
                    }

                    view.opened_question_downvote.setOnClickListener {
                        executeVote("down",questionId, uid!!, view.opened_question_votes, view.opened_question_upvote, view.opened_question_downvote)
                    }
                }


            })


            refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    postAuthor = p0.getValue(Users::class.java)
                    Picasso.get().load(postAuthor!!.image).into(view.opened_question_author_image)
                    view.opened_question_author_name.text = postAuthor!!.name

                }

            })


        }

        view.opened_question_answer_btn.setOnClickListener {

            val action = OpenedQuestionFragmentDirections.actionOpenedQuestionFragmentToAnswerFragment()
            action.questionId = question!!.id
            action.questionAuthor = authorUid

            findNavController().navigate(action)

        }

        val answersRecycler = view.opened_question_answers_recycler
        val answersRecyclerLayoutManager = LinearLayoutManager(this.context)
        answersRecycler.adapter = answersAdapter
        answersRecycler.layoutManager = answersRecyclerLayoutManager

        listenToAnswers(questionId)

    }


    private fun listenToAnswers(qId: String) {

        answersAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/answers/$qId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleAnswerFromDB = p0.getValue(Answers::class.java)

                if (singleAnswerFromDB != null) {

                    answersAdapter.add(
                        SingleAnswer(
                            singleAnswerFromDB.id,
                            singleAnswerFromDB.content,
                            singleAnswerFromDB.author,
                            singleAnswerFromDB.timestamp
                        )
                    )

                }

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }


        })


    }


}

class SingleAnswer(
    val answerId: String,
    val answerContent: String,
    val answerAuthor: String,
    val answerTimestamp: String
) : Item<ViewHolder>(), DereMethods {

    val uid = FirebaseAuth.getInstance().uid

    var author: Users? = null
    override fun getLayout(): Int {
        return R.layout.answer_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val ref = FirebaseDatabase.getInstance().getReference("/users/$answerAuthor")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                author = p0.getValue(Users::class.java)
                Picasso.get().load(author!!.image).into(viewHolder.itemView.answer_author_image)
                viewHolder.itemView.answer_author_name.text = author!!.name
                viewHolder.itemView.answer_content.text = answerContent
                viewHolder.itemView.answer_timestamp.text = answerTimestamp
            }

        })


        viewHolder.itemView.answer_upvote.setOnClickListener {
            executeVote("up", answerId, uid!!, viewHolder.itemView.answer_votes, viewHolder.itemView.answer_upvote, viewHolder.itemView.answer_downvote)
        }

        viewHolder.itemView.answer_downvote.setOnClickListener {
            executeVote("down", answerId, uid!!, viewHolder.itemView.answer_votes, viewHolder.itemView.answer_upvote, viewHolder.itemView.answer_downvote)
        }

        val refVotes = FirebaseDatabase.getInstance().getReference("/votes/$answerId")


        refVotes.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var count = 0

                for (ds in p0.getChildren()) {
                    val rating = ds.getValue(Int::class.java)
                    count += rating!!
                    viewHolder.itemView.answer_votes.text = count.toString()
                }

                if (p0.hasChild("$uid")) {
                    val refUserVote = FirebaseDatabase.getInstance().getReference("/votes/$answerId/$uid")
                    var vote = 0
                    refUserVote.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            vote = p0.getValue().toString().toInt()

                            when (vote) {
                                1 -> {
                                    viewHolder.itemView.answer_upvote.setImageResource(R.drawable.arrow_up_active)
                                    viewHolder.itemView.answer_downvote.setImageResource(R.drawable.arrow_down_default)
                                }
                                0 -> {
                                    viewHolder.itemView.answer_upvote.setImageResource(R.drawable.arrow_up_default)
                                    viewHolder.itemView.answer_downvote.setImageResource(R.drawable.arrow_down_default)
                                }
                                -1 -> {
                                    viewHolder.itemView.answer_upvote.setImageResource(R.drawable.arrow_up_default)
                                    viewHolder.itemView.answer_downvote.setImageResource(R.drawable.arrow_down_active)
                                }
                            }

                        }

                    })


                }
            }


        })

    }








//    private fun executeVote1(vote: String, answerId : String) {
//
//
//        val refVotes = FirebaseDatabase.getInstance().getReference("/votes/$answerId")
//        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                val refUserVote = FirebaseDatabase.getInstance().getReference("/votes/$answerId/$uid")
//
//                if (p0.hasChild("$uid")) {
//                    var voteValue = 0
//                    refUserVote.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onCancelled(p0: DatabaseError) {
//
//                        }
//
//                        override fun onDataChange(p0: DataSnapshot) {
//                            voteValue = p0.getValue().toString().toInt()
//
//                            when (voteValue) {
//
//                                1 -> {
//                                    when (vote) {
//                                        "up" -> refUserVote.setValue(1)
//                                        "down" -> refUserVote.setValue(0)
//                                        else -> refUserVote.setValue(0)
//                                    }
//                                }
//
//                                0 -> {
//                                    when (vote) {
//                                        "up" -> refUserVote.setValue(1)
//                                        "down" -> refUserVote.setValue(-1)
//                                        else -> refUserVote.setValue(0)
//
//                                    }
//                                }
//
//                                -1 -> {
//                                    when (vote) {
//                                        "up" -> refUserVote.setValue(0)
//                                        "down" -> refUserVote.setValue(-1)
//                                        else -> refUserVote.setValue(0)
//
//                                    }
//                                }
//                            }
//
//                        }
//
//                    })
//
//
//                } else {
//                    when (vote) {
//                        "up" -> refUserVote.setValue(1)
//                        "down" -> refUserVote.setValue(-1)
//                        else -> refUserVote.setValue(0)
//                    }
//                }
//            }
//
//
//        })
//
//    }


}
