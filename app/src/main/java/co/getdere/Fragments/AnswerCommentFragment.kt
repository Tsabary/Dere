package co.getdere.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.Answers
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer_comment.view.*


class AnswerCommentFragment : Fragment(), DereMethods {

    lateinit var questionId : String
    lateinit var answerId : String
    lateinit var questionAuthorId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Comment"

        arguments?.let {
            val safeArgs = AnswerFragmentArgs.fromBundle(it)
            questionId = safeArgs.questionId
            questionAuthorId = safeArgs.questionAuthor

            val content = view.answer_comment_content_input
            val uid = FirebaseAuth.getInstance().uid ?: return

            view.answer_comment_post_btn.setOnClickListener {

                postComment(content.text.toString(), System.currentTimeMillis(), uid)

            }
        }


    }

    private fun postComment(content : String, timestamp: Long, author : String){

        val refComment = FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/$answerId/comments").push()

        val newComment = Answers(refComment.key!!, answerId, content, timestamp, author)

        refComment.setValue(newComment)
            .addOnSuccessListener {
                Log.d("postAnswerComment", "Saved comment to Firebase Database")
//                val action = AnswerFragmentDirections.actionDestinationAnswerToDestinationQuestionOpened()
//                findNavController().navigate(action)

            }.addOnFailureListener {
                Log.d("postAnswerComment", "Failed to save question to database")
            }
    }
}
