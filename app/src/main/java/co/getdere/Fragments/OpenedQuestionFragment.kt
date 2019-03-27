package co.getdere.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.*
import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelQuestion
import co.getdere.ViewModels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenedQuestionFragment : Fragment(), DereMethods {

    var question: Question? = null

    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var questionObject: Question


    lateinit var currentUserObject : Users
    lateinit var randomUserObject : Users

    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var saveButton : TextView

    lateinit var openedQuestionAuthorReputation : TextView


    val uid = FirebaseAuth.getInstance().uid

    val answersAdapter = GroupAdapter<ViewHolder>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            questionObject = sharedViewModelQuestion.questionObject.value!!

            currentUserObject = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelRandomUser =ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_opened_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Board"

        saveButton = view.findViewById<TextView>(R.id.opened_question_save)
        val answerButton = view.findViewById<TextView>(R.id.opened_question_answer_btn)
        val answersRecycler = view.findViewById<RecyclerView>(R.id.opened_question_answers_recycler)
        val openedQuestionTitle = view.findViewById<TextView>(R.id.opened_question_title)
        val openedQuestionContent = view.findViewById<TextView>(R.id.opened_question_content)
        val openedQuestionTimeStamp = view.findViewById<TextView>(R.id.opened_question_timestamp)
        val openedQuestionTags = view.findViewById<TextView>(R.id.opened_question_tags)
        val openedQuestionUpVote = view.findViewById<ImageButton>(R.id.opened_question_upvote)
        val openedQuestionDownVote = view.findViewById<ImageButton>(R.id.opened_question_downvote)
        val openedQuestionVotes = view.findViewById<TextView>(R.id.opened_question_votes)
        val openedQuestionAuthorImage = view.findViewById<ImageView>(R.id.opened_question_author_image)
        val openedQuestionAuthorName = view.findViewById<TextView>(R.id.opened_question_author_name)
        openedQuestionAuthorReputation = view.findViewById<TextView>(R.id.opened_question_author_reputation)


        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question
                checkIfQuestionSaved(0)

                val stampMills = questionObject.timestamp
                val pretty = PrettyTime()
                val date = pretty.format(Date(stampMills))


                openedQuestionTitle.text = questionObject.title
                openedQuestionContent.text = questionObject.details
                openedQuestionTimeStamp.text = date
                openedQuestionTags.text = "tags: ${questionObject.tags.joinToString()}"
                listenToAnswers(questionObject.id)

                executeVote(
                    "checkStatus",
                    questionObject.id,
                    currentUserObject.uid,
                    currentUserObject.name,
                    questionObject.author,
                    0,
                    openedQuestionVotes,
                    openedQuestionUpVote,
                    openedQuestionDownVote,
                    questionObject.id,
                    0,
                    openedQuestionAuthorReputation
                )

            }
        }
        )






        sharedViewModelRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(user.image).into(openedQuestionAuthorImage)
                openedQuestionAuthorName.text = user.name

                openedQuestionAuthorReputation.text = "(${user.reputation})"

                randomUserObject = user // remove later this is just from pasr bad code - is it?
            }
        }
        )



        openedQuestionUpVote.setOnClickListener {
            executeVote(
                "up",
                questionObject.id,
                currentUserObject.uid,
                currentUserObject.name,
                questionObject.author,
                0,
                openedQuestionVotes,
                openedQuestionUpVote,
                openedQuestionDownVote,
                questionObject.id,
                1,
                openedQuestionAuthorReputation
            )
        }

        openedQuestionDownVote.setOnClickListener {
            executeVote(
                "down",
                questionObject.id,
                currentUserObject.uid,
                currentUserObject.name,
                questionObject.author,
                0,
                openedQuestionVotes,
                openedQuestionUpVote,
                openedQuestionDownVote,
                questionObject.id,
                1,
                openedQuestionAuthorReputation
            )
        }


        saveButton.setOnClickListener {
            checkIfQuestionSaved(1)
        }

        answerButton.setOnClickListener {

            val action = OpenedQuestionFragmentDirections.actionOpenedQuestionFragmentToAnswerFragment()
            action.questionId = questionObject.id
            action.questionAuthor = randomUserObject.uid

            findNavController().navigate(action)
        }

        val answersRecyclerLayoutManager = LinearLayoutManager(this.context)
        answersRecycler.adapter = answersAdapter
        answersRecycler.layoutManager = answersRecyclerLayoutManager
    }

    override fun onDetach() {
        super.onDetach()
        sharedViewModelRandomUser.randomUserObject.postValue(Users())
        sharedViewModelQuestion.questionObject.postValue(Question())
    }

    private fun listenToAnswers(qId: String) {

        answersAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions/$qId/answers")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleAnswerFromDB = p0.child("body").getValue(Answers::class.java)

                if (singleAnswerFromDB != null) {

                    answersAdapter.add(
                        SingleAnswer(
                            singleAnswerFromDB,
                            currentUserObject.uid,
                            currentUserObject.name
                        )
                    )
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun checkIfQuestionSaved(ranNum: Int) {

        val refCurrentUserSavedQuestions = FirebaseDatabase.getInstance().getReference("/users/${currentUserObject.uid}/saved-questions")

        refCurrentUserSavedQuestions.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(questionObject.id)) {
                    if (ranNum == 1) {
                        saveButton.text = getString(R.string.Save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900 ))

                        refCurrentUserSavedQuestions.child(questionObject.id).removeValue().addOnSuccessListener {
                            changeReputation(11, questionObject.id, questionObject.id,currentUserObject.uid, currentUserObject.name, questionObject.author, openedQuestionAuthorReputation, "questionsave")
                        }

                    } else {
                        saveButton.text = getString(R.string.Saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700 ))
                    }


                } else {
                    if (ranNum == 1) {

                        saveButton.text = getString(R.string.Saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700 ))

                        val refQuestionInUserSavedQuestions =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/${currentUserObject.uid}/saved-questions/${questionObject.id}")
                        val savedQuestion = SimpleString(questionObject.id)
                        refQuestionInUserSavedQuestions.setValue(savedQuestion).addOnSuccessListener {
                            changeReputation(10, questionObject.id, questionObject.id,currentUserObject.uid, currentUserObject.name, questionObject.author, openedQuestionAuthorReputation, "questionsave")

                        }

                    } else {
                        saveButton.text = getString(R.string.Save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900 ))
                    }
                }
            }
        })
    }
}




class SingleAnswer(
    val answer : Answers,
    val currentUserId : String,
    val currentUserName : String
) : Item<ViewHolder>(), DereMethods {

    override fun getLayout(): Int {
        return R.layout.answer_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        executeVote(
            "checkStatus",
            answer.questionId,
            currentUserId,
            currentUserName,
            answer.author,
            1,
            viewHolder.itemView.answer_votes,
            viewHolder.itemView.answer_upvote,
            viewHolder.itemView.answer_downvote,
            answer.answerId,
            0,
            viewHolder.itemView.answer_author_reputation
        )





        val ref = FirebaseDatabase.getInstance().getReference("/users/${answer.author}/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)

                if (author != null){

                    val stampMills = answer.timestamp
                    val pretty = PrettyTime()
                    val date = pretty.format(Date(stampMills))

                    Glide.with(viewHolder.root.context).load(author.image).into(viewHolder.itemView.answer_author_image)
                    viewHolder.itemView.answer_author_name.text = author.name
                    viewHolder.itemView.answer_content.text = answer.content
                    viewHolder.itemView.answer_timestamp.text = date
                    viewHolder.itemView.answer_author_reputation.text = "reputation: ${author.reputation}"
                }
            }
        })


        viewHolder.itemView.answer_upvote.setOnClickListener {
            executeVote(
                "up",
                answer.questionId,
                currentUserId,
                currentUserName,
                answer.author,
                1,
                viewHolder.itemView.answer_votes,
                viewHolder.itemView.answer_upvote,
                viewHolder.itemView.answer_downvote,
                answer.answerId,
                1,
                viewHolder.itemView.answer_author_reputation
            )
        }

        viewHolder.itemView.answer_downvote.setOnClickListener {
            executeVote(
                "down",
                answer.questionId,
                currentUserId,
                currentUserName,
                answer.author,
                1,
                viewHolder.itemView.answer_votes,
                viewHolder.itemView.answer_upvote,
                viewHolder.itemView.answer_downvote,
                answer.answerId,
                1,
                viewHolder.itemView.answer_author_reputation
            )
        }
    }
}
