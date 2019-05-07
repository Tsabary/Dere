package co.getdere.registerLogin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.interfaces.DereMethods
import com.google.android.gms.common.util.ArrayUtils.contains
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.tomer.fadingtextview.FadingTextView
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(), DereMethods {

    lateinit var loginButton: TextView
    lateinit var loginButtonBlinking: FadingTextView
    lateinit var loginButtonBlinkingBackground: TextView

    var texts = arrayOf("LOGGING IN", "LOGGING IN", "LOGGING IN")

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = login_button
        loginButtonBlinking = login_button_active_text
        loginButtonBlinkingBackground = login_button_active_background

        val forgotPassword = login_forgot_password

        loginButtonBlinking.setTexts(texts)
        loginButtonBlinking.setTimeout(500, TimeUnit.MILLISECONDS)

        loginButton.setOnClickListener {
            loginButton.isClickable = false
            loginButton.visibility = View.GONE
            loginButtonBlinking.visibility = View.VISIBLE
            loginButtonBlinkingBackground.visibility = View.VISIBLE
            closeKeyboard(this)

            performLogin()

        }
        val userEmail = login_email


        forgotPassword.setOnClickListener {
            val userEmailInput = userEmail.text.toString().trimEnd().replace("\\s".toRegex(), "")
            if (userEmailInput.contains("@") && userEmailInput.contains(".")) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(userEmailInput)
                    .addOnSuccessListener {
                        Toast.makeText(this, "A reset link has been sent to your email", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun backToRegistration(view: View) {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun performLogin() {

        val logEmail = login_email.text.toString().trimEnd()
        val logPass = login_password.text.toString().replace("\\s".toRegex(), "")

        Log.d("Main", "email is $logEmail")
        Log.d("Main", "pass is $logPass")

//        Patterns.EMAIL_ADDRESS.matcher(logEmail).matches()  <--- this methos was used before for the if statement but I've replaced it as I kept getting the invalid email error

        if (logEmail.contains("@") && logEmail.contains(".")) {

            if (logPass.length > 5) {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(logEmail, logPass).addOnCompleteListener {
                    if (it.isSuccessful) {

                        FirebaseInstanceId.getInstance().instanceId
                            .addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("FCM", "getInstanceId failed", task.exception)
                                    return@OnCompleteListener
                                }

                                // Get new Instance ID token
                                val token = task.result?.token

                                // Log and toast
                                Log.d("FCM", token)
                                val uid = FirebaseAuth.getInstance().uid
                                val userRef =
                                    FirebaseDatabase.getInstance().getReference("/users/$uid/services/firebase-token")
                                userRef.setValue(token)

                                FirebaseMessaging.getInstance().subscribeToTopic(uid).addOnSuccessListener {

                                    if(hasNoPermissions()){
                                        requestPermission()
                                    } else {
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    }
                                }
                            })


                    }
                    //else if successful
                    registerFail()
                    Log.d("Login", "Failed to log in a user")

                }.addOnFailureListener {
                    registerFail()
                    Log.d("Main", "Failed to log in user : ${it.message}")
                }
            } else {
                registerFail()
                Toast.makeText(this, "Your password needs to be at least 6 characters long", Toast.LENGTH_LONG)
                    .show()
            }


        } else {
            registerFail()
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
        }

    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun registerFail() {
        loginButton.isClickable = true
        loginButton.visibility = View.VISIBLE
        loginButtonBlinking.visibility = View.GONE
        loginButtonBlinkingBackground.visibility = View.GONE
    }


}
