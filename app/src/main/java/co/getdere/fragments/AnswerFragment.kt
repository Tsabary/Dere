package co.getdere.fragments

import android.app.Activity
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
import androidx.recyclerview.widget.GridLayoutManager
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Answers
import co.getdere.models.Question
import co.getdere.models.Users

import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_layout.*
import kotlinx.android.synthetic.main.answer_layout.view.*
import kotlinx.android.synthetic.main.fragment_answer.*
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment(), DereMethods {

    lateinit var question: Question
    lateinit var currentUser: Users
    var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()
    var imageListFinal = mutableListOf<String>()

    lateinit var sharedViewModelAnswerImages: SharedViewModelAnswerImages

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(SharedViewModelAnswerImages::class.java)

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

        val activity = activity as MainActivity

        val addImage = answer_add_photos_button

        val imagesRecycler = answer_photos_recycler
        val imagesRecyclerLayoutManager = GridLayoutManager(this.context, 3)

        imagesRecycler.adapter = imagesRecyclerAdapter
        imagesRecycler.layoutManager = imagesRecyclerLayoutManager

        val content = view.answer_content

        sharedViewModelAnswerImages.imageList.observe(this, Observer {
            it?.let { existingImageList ->

                imagesRecyclerAdapter.clear()
                imageListFinal.clear()

                for (image in existingImageList) {
                    imagesRecyclerAdapter.add(FeedImage(image))
                    imageListFinal.add(image.id)
                }
            }
        })

        view.answer_btn.setOnClickListener {

            if (answer_content.text.length > 15) {
                postAnswer(content.text.toString(), System.currentTimeMillis(), activity)
            } else {
                Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT).show()
            }
        }


        addImage.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.addImageToAnswer).commit()
            activity.subActive = activity.addImageToAnswer
        }
    }

    private fun postAnswer(content: String, timestamp: Long, activity : Activity) {

        val ref = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/").push()

        val refAnswerBody =
            FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/${ref.key}/body")

        val newAnswer = Answers(ref.key!!, question.id, content, timestamp, currentUser.uid, imageListFinal)

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
                    "answer",
                    activity
                )

                val refQuestionLastInteraction =
                    FirebaseDatabase.getInstance().getReference("/questions/${question.id}/main/body/lastInteraction")

                refQuestionLastInteraction.setValue(timestamp).addOnSuccessListener {

                    val activity = activity as MainActivity

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
