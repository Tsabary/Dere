package co.getdere.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Answers
import co.getdere.models.Question
import co.getdere.models.Users

import co.getdere.R
import co.getdere.groupieAdapters.CollectionPhoto
import co.getdere.models.Images
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_answer.*
import kotlinx.android.synthetic.main.fragment_answer.view.*


class AnswerFragment : Fragment(), DereMethods {

    lateinit var question: Question
    lateinit var currentUser: Users
    var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()
    var imageListFinal = mutableListOf<String>()
    lateinit var answerContent: EditText

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
//                    imageListFinal.clear()
//                    imagesRecyclerAdapter.clear()
                    sharedViewModelAnswerImages.imageList.postValue(mutableListOf())
                    answerContent.text.clear()
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
        val answerButton = answer_btn
        answerButton.text = getString(R.string.answer)

        val imagesRecycler = answer_photos_recycler
        val imagesRecyclerLayoutManager = GridLayoutManager(this.context, 3)

        imagesRecycler.adapter = imagesRecyclerAdapter
        imagesRecycler.layoutManager = imagesRecyclerLayoutManager

        answerContent = view.answer_content

        sharedViewModelAnswerImages.imageList.observe(this, Observer {
            it?.let { existingImageList ->

                imagesRecyclerAdapter.clear()
                imageListFinal.clear()

                imagesRecycler.visibility = if (existingImageList.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                val imagesRef = FirebaseDatabase.getInstance().getReference("/images")

                for (image in existingImageList) {

                    imagesRef.child("$image/body")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                val imageObject  = p0.getValue(Images::class.java)
                                if (imageObject != null){
                                    imagesRecyclerAdapter.add(CollectionPhoto(imageObject, activity, "answer", 0))
                                    imageListFinal.add(imageObject.id)
                                }
                            }
                        })


                }
            }
        })

        answerButton.setOnClickListener {

            if (answer_content.text.length > 15) {

                val answersRef = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers")

                answersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        var answersPerUser = 0

                        for (answer in p0.children) {

                            val answerObject = answer.child("body").getValue(Answers::class.java)

                            if (answerObject!!.author == currentUser.uid) {
                                answersPerUser++
                            }
                        }

                        if (answersPerUser == 0) {
                            postAnswer(answerContent.text.toString(), System.currentTimeMillis(), activity)
                        } else {
                            Toast.makeText(
                                activity,
                                "You've already answered this question, please edit your answer instead",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                })

            } else {
                Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT).show()
            }
        }


        addImage.setOnClickListener {
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.addImageToAnswer, "addImageToAnswer")
                .addToBackStack("addImageToAnswer").commit()
            activity.subActive = activity.addImageToAnswer
        }
    }

    private fun postAnswer(content: String, timestamp: Long, activity: Activity) {

        activity as MainActivity

        val ref = FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/").push()

        val refAnswerBody =
            FirebaseDatabase.getInstance().getReference("/questions/${question.id}/answers/${ref.key}/body")

        val newAnswer = Answers(ref.key!!, question.id, content, timestamp, currentUser.uid, imageListFinal)

        refAnswerBody.setValue(newAnswer)
            .addOnSuccessListener {
                Log.d("postAnswerActivity", "Saved answer to Firebase Database")

                if (currentUser.uid != question.author) {
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
                }


                val refQuestionLastInteraction =
                    FirebaseDatabase.getInstance().getReference("/questions/${question.id}/main/body/lastInteraction")

                refQuestionLastInteraction.setValue(timestamp).addOnSuccessListener {
                    activity.openedQuestionFragment.listenToAnswers(question.id)
                    activity.subActive = activity.openedQuestionFragment
                    closeKeyboard(activity)
                    answerContent.text.clear()

                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                    firebaseAnalytics.logEvent("question_answer_added", null)
                    activity.subFm.popBackStack("answerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                }


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }
}
