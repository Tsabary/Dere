package co.getdere.registerLogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.getdere.MainActivity
import co.getdere.models.Users
import co.getdere.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            register_button.isClickable = false
            performRegister()
        }

        register_photo_pick.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Main", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            register_circular_image_view.setImageBitmap(bitmap)
            register_photo_pick.alpha = 0f

        }
    }

    fun haveAccountClicked(view: View) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun performRegister() {

        userEmail = register_email.text.toString()
        userPassword = register_password.text.toString()

        Log.d("Main", "email is $userEmail")
        Log.d("Main", "pass is $userPassword")

//        Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()

        if (selectedPhotoUri != null) {


            if (userEmail.contains("@") && userEmail.contains(".")) {

                if (userPassword.length > 5) {

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(
                                    "RegisterActivity",
                                    "Succesflly created a user using uid: ${it.result?.user?.uid}"
                                )
                                uploadImageToFirebase()

                                return@addOnCompleteListener
                            } else {
                                register_button.isClickable = true
                                Log.d("RegisterActivity", "Failed creating a user using uid")
                            }

                        }.addOnFailureListener {
                        register_button.isClickable = true
                        Log.d("RegisterActivity", "Failed to create user : ${it.message}")
                    }
                } else {
                    register_button.isClickable = true
                    Toast.makeText(this, "Your password needs to be at least 6 characters long", Toast.LENGTH_LONG)
                        .show()
                }

            } else {
                register_button.isClickable = true
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
            }
        } else {
            register_button.isClickable = true
            Toast.makeText(this, "Please choose a profile photo", Toast.LENGTH_LONG)
                .show()
        }


    }

    private fun uploadImageToFirebase() {

        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/userprofile/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File location: $it")

                    addUserToFirebaseDatabase(it.toString())

                }

            }.addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image to server $it")

            }

    }

    private fun addUserToFirebaseDatabase(userImageUrl: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")

        userName = register_name.text.toString()

        val newUser = Users(uid, userName, userEmail, userImageUrl, "0", "Watch me as I get Dere", System.currentTimeMillis())

        ref.setValue(newUser)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user to Firebase Database")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                Log.d("RegisterActivity", "Failed to save user to database")
            }

    }

}
