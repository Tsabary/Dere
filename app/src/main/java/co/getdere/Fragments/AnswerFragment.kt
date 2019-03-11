package co.getdere.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.getdere.Models.Answers

import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment() {

    lateinit var questionId : String
    lateinit var questionAuthorId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.setTitle("Answer")

        arguments?.let {
            val safeArgs = AnswerFragmentArgs.fromBundle(it)
            questionId = safeArgs.questionId
            questionAuthorId = safeArgs.questionAuthor

            val content = view.answer_content
            val uid = FirebaseAuth.getInstance().uid ?: return



            view.answer_btn.setOnClickListener {

                postAnswer(content.text.toString(), "4 days ago", uid, questionId)

            }
        }


    }

    private fun postAnswer(content : String, timestamp: String, author : String, questionIdForAction : String){

        val uid = FirebaseAuth.getInstance().uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("/answers/$questionId").push()

        val newAnswer = Answers(ref.key!!, content, timestamp, uid)

        ref.setValue(newAnswer)
            .addOnSuccessListener {
                Log.d("postAnswerActivity", "Saved answer to Firebase Database")
                val action = AnswerFragmentDirections.actionDestinationAnswerToDestinationQuestionOpened()
                action.questionId = questionIdForAction
                action.questionAuthor = questionAuthorId
                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }
}
