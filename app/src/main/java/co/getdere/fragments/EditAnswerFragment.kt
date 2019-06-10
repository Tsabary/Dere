package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.groupieAdapters.CollectionPhoto
import co.getdere.interfaces.DereMethods
import co.getdere.models.Answers
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelAnswer
import co.getdere.viewmodels.SharedViewModelAnswerImages
import co.getdere.viewmodels.SharedViewModelCurrentUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_answer.*


class EditAnswerFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelAnswerImages: SharedViewModelAnswerImages
    lateinit var sharedViewModelAnswer: SharedViewModelAnswer

    lateinit var answer: Answers
    lateinit var currentUser: Users
    var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_answer, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(SharedViewModelAnswerImages::class.java)
            sharedViewModelAnswer = ViewModelProviders.of(it).get(SharedViewModelAnswer::class.java)
        }

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

                    FirebaseDatabase.getInstance().getReference("/images").child("$image/body")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                val imageObject = p0.getValue(Images::class.java)
                                if (imageObject != null) {
                                    imagesRecyclerAdapter.add(CollectionPhoto(imageObject, activity, "answer", 0))
                                    imageListFinal.add(imageObject.id)
                                }
                            }
                        })
                }


                updateAnswer.setOnClickListener {

                    if (answer_content.text.length > 15) {

                        activity.isEditAnswerActive = false

                        val answerRef = FirebaseDatabase.getInstance()
                            .getReference("/questions/${answer.questionId}/answers/${answer.answerId}/body")

                        answerRef.child("content").setValue(content.text.toString())
                        answerRef.child("photos").setValue(imageListFinal)

                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.openedQuestionFragment,
                            "openedQuestionFragment"
                        ).addToBackStack("openedQuestionFragment").commit()
                        activity.subActive = activity.openedQuestionFragment
                    } else {
                        Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })


        sharedViewModelAnswer.sharedAnswerObject.observe(this, Observer { observedQuestion ->
            observedQuestion?.let { answerObject ->

                answer = answerObject

                val answerImagesList = mutableListOf<String>()

                sharedViewModelAnswerImages.imageList.postValue(mutableListOf())


                answerObject.photos.forEach {

                    FirebaseDatabase.getInstance().getReference("/images/$it/body")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val imageObject = p0.getValue(Images::class.java)
                                if (imageObject != null) {
                                    answerImagesList.add(imageObject.id)
                                    sharedViewModelAnswerImages.imageList.postValue(answerImagesList)
                                }
                            }
                        })
                }
                content.setText(answerObject.content)
            }
        })

        addImage.setOnClickListener {
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.addImageToAnswer, "addImageToAnswer")
                .addToBackStack("addImageToAnswer").commit()
            activity.subActive = activity.addImageToAnswer
        }
    }
}
