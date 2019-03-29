package co.getdere.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.Answers

import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment(), DereMethods {

    lateinit var questionId : String
    lateinit var questionAuthorId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Answer"

        arguments?.let {
            val safeArgs = AnswerFragmentArgs.fromBundle(it)
            questionId = safeArgs.questionId
            questionAuthorId = safeArgs.questionAuthor

            val content = view.answer_content
            val uid = FirebaseAuth.getInstance().uid ?: return



            view.answer_btn.setOnClickListener {

                postAnswer(content.text.toString(), System.currentTimeMillis(), uid)

            }
        }


    }

    private fun postAnswer(content : String, timestamp: Long, author : String){

        val ref = FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/").push()

        val refAnswerBody = FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/${ref.key}/body")

        val newAnswer = Answers(ref.key!!, questionId, content, timestamp, author)

        refAnswerBody.setValue(newAnswer)
            .addOnSuccessListener {
                Log.d("postAnswerActivity", "Saved answer to Firebase Database")

                changeReputation(6, ref.key!!,questionId, author, "you", author, TextView(this.context), "answer")

                val action = AnswerFragmentDirections.actionDestinationAnswerToDestinationQuestionOpened()
                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }
}
