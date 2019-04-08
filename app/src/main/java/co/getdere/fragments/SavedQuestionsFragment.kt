package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.groupieAdapters.SingleQuestion
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_saved_questions.view.*


class SavedQuestionsFragment : Fragment() {

    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var currentUser: Users

    lateinit var sharedViewModelForQuestion : SharedViewModelQuestion


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {

            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)

        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_saved_questions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Saved discussions"

        val questionsRecycler = view.saved_questions_recycler

        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQuestions()



        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleQuestion

            sharedViewModelForQuestion.questionObject.postValue(row.question)
        }
    }


    private fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/saved-questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionPathFromDB = p0.key


                if (singleQuestionPathFromDB != null) {


                    val refQuestionObject = FirebaseDatabase.getInstance()
                        .getReference("/questions/$singleQuestionPathFromDB/main/body")

                    refQuestionObject.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            val singleQuestionObjectFromDB = p0.getValue(Question::class.java)

                            if (singleQuestionObjectFromDB != null) {
                                questionsRecyclerAdapter.add(SingleQuestion(singleQuestionObjectFromDB))

                            }

                        }

                    })


                }


            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


}