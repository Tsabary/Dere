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
import co.getdere.groupieAdapters.AnswerPhoto
import co.getdere.groupieAdapters.FeedImage
import co.getdere.groupieAdapters.ImageSelector
import co.getdere.models.Images
import co.getdere.viewmodels.SharedViewModelAnswer
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_layout.*
import kotlinx.android.synthetic.main.answer_layout.view.*
import kotlinx.android.synthetic.main.fragment_answer.*
import kotlinx.android.synthetic.main.fragment_answer.view.*


class EditAnswerFragment : Fragment(), DereMethods {

    lateinit var currentUser: Users
    var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    lateinit var sharedViewModelAnswerImages: SharedViewModelAnswerImages
    lateinit var sharedViewModelAnswer: SharedViewModelAnswer
    lateinit var answer: Answers



    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(SharedViewModelAnswerImages::class.java)
            sharedViewModelAnswer = ViewModelProviders.of(it).get(SharedViewModelAnswer::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_answer, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val addImage = answer_add_photos_button
        val updateAnswer = answer_btn

        val imagesRecycler = answer_photos_recycler
        val imagesRecyclerLayoutManager = GridLayoutManager(this.context, 3)

        imagesRecycler.adapter = imagesRecyclerAdapter
        imagesRecycler.layoutManager = imagesRecyclerLayoutManager

        val content = answer_content

        sharedViewModelAnswerImages.imageList.observe(this, Observer {
            it?.let { existingImageList ->

                val imageListFinal = mutableListOf<String>()

                imagesRecyclerAdapter.clear()

                imagesRecycler.visibility = if (existingImageList.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                for (image in existingImageList) {
                    imagesRecyclerAdapter.add(AnswerPhoto(image, activity))
                    imageListFinal.add(image.id)
                }


                updateAnswer.setOnClickListener {

                    if (answer_content.text.length > 15) {

                        activity.isEditAnswerActive = false

                        val answerRef = FirebaseDatabase.getInstance().getReference("/questions/${answer.questionId}/answers/${answer.answerId}/body")

                        answerRef.child("content").setValue(content.text.toString())
                        answerRef.child("photos").setValue(imageListFinal)

                        activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment).commit()
                        activity.subActive = activity.openedQuestionFragment

                    } else {
                        Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT).show()
                    }
                }



            }
        })


        sharedViewModelAnswer.sharedAnswerObject.observe(this, Observer { observedQuestion ->
            observedQuestion?.let { answerObject ->

                answer = answerObject

                var answerImagesList = mutableListOf<Images>()

                sharedViewModelAnswerImages.imageList.postValue(mutableListOf())


                answerObject.photos.forEach {

                    val singleImageRef = FirebaseDatabase.getInstance().getReference("/images/$it/body")

                    singleImageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val imageObject = p0.getValue(Images::class.java)
                            answerImagesList.add(imageObject!!)
                            sharedViewModelAnswerImages.imageList.postValue(answerImagesList)
                            Log.d("imageList", answerImagesList.toString())
                        }
                    })
                }

                content.setText(answerObject.content)


            }
        })







        addImage.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.addImageToAnswer).commit()
            activity.subActive = activity.addImageToAnswer
        }
    }

}
