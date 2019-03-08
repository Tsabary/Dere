package co.getdere.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import co.getdere.MainActivity
import co.getdere.Models.Question
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class NewQuestionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_new_question, container, false)

        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionTitle: EditText = view.findViewById(R.id.new_question_title)
        val questionDetails = view.findViewById<EditText>(R.id.new_question_details)
        val questionTags = view.findViewById<EditText>(R.id.new_question_tags)
        val questionButton = view.findViewById<Button>(R.id.new_question_btn)


        questionButton.setOnClickListener {
            postQuestion(
                questionTitle.text.toString(),
                questionDetails.text.toString(),
                questionTags.text.toString(),
                System.currentTimeMillis().toString()
            )

        }
    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

    private fun postQuestion(title: String, details: String, tags: String, timestamp: String) {

        val uid = FirebaseAuth.getInstance().uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("/questions").push()

        val newQuestion = Question(ref.key!!, title, details, tags, timestamp, uid)

        ref.setValue(newQuestion)
            .addOnSuccessListener {
                Log.d("postQuestionActivity", "Saved question to Firebase Database")
                val action = NewQuestionFragmentDirections.actionDestinationNewQuestionToDestinationBoard()
                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }

}