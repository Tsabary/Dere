package co.getdere

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import co.getdere.Models.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_main.*

class NewQuestionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_question)

        val questionTitle : EditText = findViewById(R.id.new_question_title)
        val questionDetails = findViewById<EditText>(R.id.new_question_details)
        val questionTags = findViewById<EditText>(R.id.new_question_tags)
        val questionButton = findViewById<Button>(R.id.new_question_btn)


        questionButton.setOnClickListener {

            postQuestion(questionTitle.text.toString(), questionDetails.text.toString(), questionTags.text.toString(), System.currentTimeMillis().toString())
        }


    }


    private fun postQuestion(title : String, details : String, tags : String, timestamp : String) {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/questions").push()

        val newQuestion = Question(title, details, tags, timestamp)

        ref.setValue(newQuestion)
            .addOnSuccessListener {
                Log.d("postQuestionActivity", "Saved question to Firebase Database")

            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }



    }



}

//            mainActivity.view_pager.setCurrentItem(0)