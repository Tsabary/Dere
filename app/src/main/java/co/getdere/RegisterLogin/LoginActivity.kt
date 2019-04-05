package co.getdere.RegisterLogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import co.getdere.FeedActivity
import co.getdere.MainActivity
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {

            performLogin()

        }

    }


    fun backToRegistration (view : View) {
        val registerIntent = Intent (this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun performLogin(){

        val logEmail = login_email.text.toString()
        val logPass = login_password.text.toString()

        Log.d("Main", "email is $logEmail")
        Log.d("Main", "pass is $logPass")

//        Patterns.EMAIL_ADDRESS.matcher(logEmail).matches()  <--- this methos was used before for the if statement but I've replaced it as I kept getting the invalid email error

        if (logEmail.contains("@") && logEmail.contains(".")) {

            if (logPass.length > 5) {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(logEmail, logPass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("Login", "Successfully logged a user in using uid: ${it.result?.user?.uid}")
                        val intent = Intent(this, FeedActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        return@addOnCompleteListener
                    }
                    //else if successful
                    Log.d("Login", "Failed to log in a user")

                }.addOnFailureListener {
                    Log.d("Main", "Failed to create user : ${it.message}")
                }
            } else {
                Toast.makeText(this, "Your password needs to be at least 6 characters long", Toast.LENGTH_LONG)
                    .show()
            }


        } else {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
        }

    }


}
