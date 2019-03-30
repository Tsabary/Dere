package co.getdere.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.AnswerComments
import co.getdere.Models.Answers
import co.getdere.Models.Users
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer_comment.view.*


class AnswerCommentFragment : Fragment(), DereMethods {

    lateinit var questionId : String
    lateinit var answer : Answers
    lateinit var currentUser : Users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Comment"

        arguments?.let {
            val safeArgs = AnswerCommentFragmentArgs.fromBundle(it)
            questionId = safeArgs.questionId
            answer = safeArgs.answer
            currentUser = safeArgs.currentUser

            val content = view.answer_comment_content_input
            val uid = FirebaseAuth.getInstance().uid ?: return

            view.answer_comment_post_btn.setOnClickListener {

                postComment(content.text.toString(), System.currentTimeMillis(), uid)

            }
        }


    }

    private fun postComment(content : String, timestamp: Long, author : String){

        val refComment = FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/${answer.answerId}/comments").push()

        val newComment = AnswerComments(refComment.key!!, answer.answerId, questionId, content, timestamp, author)

        refComment.setValue(newComment)
            .addOnSuccessListener {
                Log.d("postAnswerComment", "Saved comment to Firebase Database")

                changeReputation(18, answer.answerId, questionId, currentUser.uid, currentUser.name, answer.author, TextView(this.context), "answercomment")


                val action = AnswerCommentFragmentDirections.actionAnswerCommentFragmentToDestinationQuestionOpened()
                findNavController().navigate(action)

            }.addOnFailureListener {
                Log.d("postAnswerComment", "Failed to save question to database")
            }
    }
}
