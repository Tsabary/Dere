package co.getdere.fragments

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
import co.getdere.R
import co.getdere.interfaces.DereMethods
import co.getdere.models.AnswerComments
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_answer.*


class AnswerCommentFragment : Fragment(), DereMethods {

    lateinit var currentUser: Users

    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var questionObject: Question

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_answer, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        answer_add_photos_button.visibility = View.GONE

        val content = answer_content
        val uid = FirebaseAuth.getInstance().uid
        val postButton = answer_btn
        postButton.text = getString(R.string.comment)

        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question

                postButton.setOnClickListener {
                    postComment(content.text.toString(), System.currentTimeMillis(), uid!!)
                    content.text.clear()
                }
            }
        })
    }

    private fun postComment(content: String, timestamp: Long, author: String) {

        val activity = (activity as MainActivity)

        val answer = activity.answerObject

        val refComment =
            FirebaseDatabase.getInstance()
                .getReference("/questions/${questionObject.id}/answers/${answer.answerId}/comments")
                .push()

        val newComment =
            AnswerComments(refComment.key!!, answer.answerId, questionObject.id, content, timestamp, author)

        FirebaseDatabase.getInstance()
            .getReference("/questions/${questionObject.id}/answers/${answer.answerId}/comments/${refComment.key}/body")
            .setValue(newComment)
            .addOnSuccessListener {

                changeReputation(
                    18,
                    answer.answerId,
                    questionObject.id,
                    currentUser.uid,
                    currentUser.name,
                    answer.author,
                    TextView(this.context),
                    "answercomment",
                    activity
                )

                val refQuestionLastInteraction = FirebaseDatabase.getInstance()
                    .getReference("/questions/${questionObject.id}/main/body/lastInteraction")

                refQuestionLastInteraction.setValue(timestamp).addOnSuccessListener {

                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.openedQuestionFragment,
                        "openedQuestionFragment"
                    ).addToBackStack("openedQuestionFragment").commit()


                    activity.subActive = activity.openedQuestionFragment

                    closeKeyboard(activity)

                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                    firebaseAnalytics.logEvent("question_answer_comment_added", null)
                }
            }
    }
}
