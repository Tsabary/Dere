package co.getdere.fragments


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.RegisterLoginActivity
import co.getdere.models.Users
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_register_login_screens.*

class RegisterFragment : Fragment() {
    lateinit var userName: EditText
    lateinit var userEmail: EditText
    lateinit var userPassword: EditText
    lateinit var userConfirmPassword: EditText
    lateinit var button: TextView
    lateinit var loadingAnimation: ConstraintLayout

    lateinit var textUserName: String
    lateinit var textUserEmail: String

    private lateinit var firebaseAuth: FirebaseAuth

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_login_screens, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val googleLogin = register_fragment_google_login

        register_fragment_forgot_password.visibility = View.GONE
        register_fragment_button.text = "Sign up"

        userName = register_fragment_name
        userEmail = register_fragment_email
        userPassword = register_fragment_password
        userConfirmPassword = register_fragment_confirm_password
        button = register_fragment_button
        loadingAnimation = register_fragment_spinner


        googleLogin.setOnClickListener {
            configureGoogleSignIn()
        }

        button.setOnClickListener {
            performRegister()
        }


        userConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (userPassword.text.toString().replace("\\s".toRegex(), "") == userConfirmPassword.text.toString().replace("\\s".toRegex(), "")){
                    register_fragment_password_check.visibility = View.VISIBLE
                } else {
                    register_fragment_password_check.visibility = View.GONE
                }
            }

        })

    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                val user = firebaseAuth.currentUser
                if (user != null){
                    addUserToFirebaseDatabase(user.displayName!!, user.email!!, 1)
                }
            } else {
                Toast.makeText(this.context, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity as RegisterLoginActivity, mGoogleSignInOptions)

        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account !=null){
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this.context, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }




    private fun performRegister() {
        button.isClickable = false
        loadingAnimation.visibility = View.VISIBLE

        textUserName = userName.text.toString().trimEnd()
        textUserEmail = userEmail.text.toString().replace("\\s".toRegex(), "")
        val textUserPassword = userPassword.text.toString().replace("\\s".toRegex(), "")
        val textUserConfirmPassword = userConfirmPassword.text.toString().replace("\\s".toRegex(), "")


        if (textUserName.length > 4) {
            if (textUserEmail.contains("@") && textUserEmail.contains(".")) {
                if (textUserConfirmPassword == textUserPassword) {
                    if (textUserPassword.length > 7) {

                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(textUserEmail, textUserPassword)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.d(
                                        "RegisterActivity",
                                        "Succesflly created a user using uid: ${it.result?.user?.uid}"
                                    )

                                    addUserToFirebaseDatabase(textUserName, textUserEmail, 0)

                                    return@addOnCompleteListener
                                } else {
                                    registerFail()
                                    Log.d("RegisterActivity", "Failed creating a user using uid")
                                }

                            }.addOnFailureListener {
                                registerFail()
                                Log.d("RegisterActivity", "Failed to create user : ${it.message}")
                            }
                    } else {
                        registerFail()
                        Toast.makeText(
                            this.context,
                            "Your password needs to be at least 6 characters long",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    registerFail()
                    Toast.makeText(this.context, "Your passwords don't match", Toast.LENGTH_LONG).show()
                }
            } else {
                registerFail()
                Toast.makeText(this.context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
            }
        } else {
            registerFail()
            Toast.makeText(this.context, "Please enter a valid name", Toast.LENGTH_LONG).show()
        }
    }


    private fun registerFail() {
        button.isClickable = true
        loadingAnimation.visibility = View.GONE
    }


    private fun addUserToFirebaseDatabase(userNameForDatabase:String, userEmailForDatabase : String, case : Int) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")


        val newUser =
            Users(uid!!, userNameForDatabase, userEmailForDatabase, "", 0, "Watch me as I get Dere", System.currentTimeMillis())

        ref.setValue(newUser)
            .addOnSuccessListener {

                val staxRef = FirebaseDatabase.getInstance().getReference("/users/$uid/stax")
                staxRef.setValue("").addOnSuccessListener {
                    Log.d("RegisterActivity", "Saved user to Firebase Database")

                    if (case == 0){
                        val user = FirebaseAuth.getInstance().currentUser

                        user!!.sendEmailVerification().addOnSuccessListener {
                            Toast.makeText(
                                this.context,
                                "Please check your email and click the link in our message",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val intent = Intent(this.context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }


                }
            }.addOnFailureListener {
                Log.d("RegisterActivity", "Failed to save user to database")
            }

    }


    override fun onStart() {
        super.onStart()

        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            FirebaseAuth.getInstance().currentUser!!.reload().addOnSuccessListener {
                val updatedUser = FirebaseAuth.getInstance().currentUser
                if (updatedUser!!.isEmailVerified) {

                    val intent = Intent(this.context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }


    companion object {
        fun newInstance(): RegisterFragment = RegisterFragment()
    }
}
