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
import co.getdere.Models.Question
import co.getdere.Models.Users

import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment(), DereMethods {

    lateinit var question : Question
    lateinit var currentUser  : Users

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
            question = safeArgs.question
            currentUser = safeArgs.currentUser

            val content = view.answer_content


            view.answer_btn.setOnClickListener {

                postAnswer(content.text.toString(), System.currentTimeMillis())

            }
        }


    }

    private fun postAnswer(content : String, timestamp: Long){

        val ref = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/").push()

        val refAnswerBody = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/${ref.key}/body")

        val newAnswer = Answers(ref.key!!, question.id, content, timestamp, currentUser.uid)

        refAnswerBody.setValue(newAnswer)
            .addOnSuccessListener {
                Log.d("postAnswerActivity", "Saved answer to Firebase Database")

                changeReputation(6, ref.key!!, question.id, currentUser.uid, currentUser.name, question.author, TextView(this.context), "answer")

                val action = AnswerFragmentDirections.actionDestinationAnswerToDestinationQuestionOpened()
                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }
}
