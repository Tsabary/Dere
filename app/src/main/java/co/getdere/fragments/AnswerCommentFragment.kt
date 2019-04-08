package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.AnswerComments
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer_comment.view.*


class AnswerCommentFragment : Fragment(), DereMethods {

    lateinit var currentUser: Users

    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var questionObject: Question


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
//            questionObject = sharedViewModelQuestion.questionObject.value!!

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Comment"

        val content = view.answer_comment_content_input
        val uid = FirebaseAuth.getInstance().uid

        view.answer_comment_post_btn.setOnClickListener {

            postComment(content.text.toString(), System.currentTimeMillis(), uid!!)
        }


//        arguments?.let {
//            val safeArgs = AnswerCommentFragmentArgs.fromBundle(it)
//            questionId = safeArgs.questionId
//            answer = safeArgs.answer
////            currentUser = safeArgs.currentUser
//
//        }


    }

    private fun postComment(content: String, timestamp: Long, author: String) {


        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question

                val activity = (activity as MainActivity)

                val answer = activity.answerObject

                val refComment =
                    FirebaseDatabase.getInstance()
                        .getReference("/questions/${questionObject.id}/answers/${answer.answerId}/comments")
                        .push()

                val newComment =
                    AnswerComments(refComment.key!!, answer.answerId, questionObject.id, content, timestamp, author)

                refComment.setValue(newComment)
                    .addOnSuccessListener {
                        Log.d("postAnswerComment", "Saved comment to Firebase Database")

                        changeReputation(
                            18,
                            answer.answerId,
                            questionObject.id,
                            currentUser.uid,
                            currentUser.name,
                            answer.author,
                            TextView(this.context),
                            "answercomment"
                        )

                        val refQuestionLastInteraction = FirebaseDatabase.getInstance()
                            .getReference("/questions/${question.id}/main/body/lastInteraction")

                        refQuestionLastInteraction.setValue(timestamp).addOnSuccessListener {

                            activity.subFm.beginTransaction().hide(activity.subActive)
                                .show(activity.openedQuestionFragment).commit()
                            activity.subActive = activity.openedQuestionFragment

                            closeKeyboard(activity)

                        }


//                        val action = AnswerCommentFragmentDirections.actionAnswerCommentFragmentToDestinationQuestionOpened()
//                        findNavController().navigate(action)

                    }.addOnFailureListener {
                        Log.d("postAnswerComment", "Failed to save question to database")
                    }


            }
        })


    }
}