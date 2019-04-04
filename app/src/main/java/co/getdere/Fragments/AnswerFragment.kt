package co.getdere.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.FeedActivity
import co.getdere.Interfaces.DereMethods
import co.getdere.Models.Answers
import co.getdere.Models.Question
import co.getdere.Models.Users

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelCurrentUser
import co.getdere.ViewModels.SharedViewModelQuestion
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer.*
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment(), DereMethods {

    lateinit var question: Question
    lateinit var currentUser: Users

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            val sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)

            sharedViewModelQuestion.questionObject.observe(this, Observer { observedQuestion ->
                observedQuestion?.let { questionObject ->
                    question = questionObject
                }
            })


        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_answer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Answer"


        val content = view.answer_content


        view.answer_btn.setOnClickListener {

            if (answer_content.text.length > 15){
                postAnswer(content.text.toString(), System.currentTimeMillis())
            } else {
                Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT).show()
            }
        }


//        arguments?.let {
//            val safeArgs = AnswerFragmentArgs.fromBundle(it)
//            question = safeArgs.question
//            currentUser = safeArgs.currentUser
//
//
//        }
//

    }

    private fun postAnswer(content: String, timestamp: Long) {

        val ref = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/").push()

        val refAnswerBody =
            FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/${ref.key}/body")

        val newAnswer = Answers(ref.key!!, question.id, content, timestamp, currentUser.uid)

        refAnswerBody.setValue(newAnswer)
            .addOnSuccessListener {
                Log.d("postAnswerActivity", "Saved answer to Firebase Database")

                changeReputation(
                    6,
                    ref.key!!,
                    question.id,
                    currentUser.uid,
                    currentUser.name,
                    question.author,
                    TextView(this.context),
                    "answer"
                )

                val refQuestionLastInteraction = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/main/body/lastInteraction")

                refQuestionLastInteraction.setValue(timestamp).addOnSuccessListener {

                    val activity = activity as FeedActivity

                    activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment)
                        .commit()
                    activity.subActive = activity.openedQuestionFragment

                    closeKeyboard(activity)

                }



            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }
}
