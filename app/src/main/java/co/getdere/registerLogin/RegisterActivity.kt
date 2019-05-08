package co.getdere.registerLogin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.getdere.MainActivity
import co.getdere.models.Users
import co.getdere.R
import co.getdere.interfaces.DereMethods
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.tomer.fadingtextview.FadingTextView
import kotlinx.android.synthetic.main.activity_register.*
import me.echodev.resizer.Resizer
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.TimeUnit


class RegisterActivity : AppCompatActivity(), DereMethods {

    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    private lateinit var userConfirmPassword: String


    private lateinit var registerButton: TextView
    private lateinit var registerButtonBlinking: FadingTextView
    private lateinit var registerButtonBlinkingBackground: TextView

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private var texts = arrayOf("CREATING ACCOUNT", "CREATING ACCOUNT")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton = register_button
        registerButtonBlinking = register_button_active_text
        registerButtonBlinkingBackground = register_button_regular_active_background

        registerButtonBlinking.setTexts(texts)
        registerButtonBlinking.setTimeout(500, TimeUnit.MILLISECONDS)

        register_to_login.setOnClickListener {
            haveAccountClicked()
        }

        registerButton.setOnClickListener {
            registerButton.visibility = View.GONE
            registerButtonBlinking.visibility = View.VISIBLE
            registerButtonBlinkingBackground.visibility = View.VISIBLE
            closeKeyboard(this)
            performRegister(this)
        }

        register_circular_image_view.setOnClickListener {

            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }

        val userConfirmPasswordInput = register_password_confirmation
        userConfirmPasswordInput.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userConfirmPassword = register_password_confirmation.text.toString().replace("\\s".toRegex(), "")
                userPassword = register_password.text.toString().replace("\\s".toRegex(), "")
                if (userPassword == userConfirmPassword){
                    register_password_match.visibility = View.VISIBLE
                } else {
                    register_password_match.visibility = View.GONE
                }

            }

        })
    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Main", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            register_circular_image_view.setImageBitmap(bitmap)
        }
    }

    private fun haveAccountClicked() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun performRegister(activity: Activity) {

        userName = register_name.text.toString().trimEnd()
        userEmail = register_email.text.toString().replace("\\s".toRegex(), "")
        userConfirmPassword = register_password_confirmation.text.toString().replace("\\s".toRegex(), "")
        userPassword = register_password.text.toString().replace("\\s".toRegex(), "")

        Log.d("Main", "email is $userEmail")
        Log.d("Main", "pass is $userPassword")

        if (selectedPhotoUri != null) {

            if (userName.length > 3) {
                if (userEmail.contains("@") && userEmail.contains(".")) {
                    if (userConfirmPassword == userPassword) {
                        if (userPassword.length > 5) {

                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPassword)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.d(
                                            "RegisterActivity",
                                            "Succesflly created a user using uid: ${it.result?.user?.uid}"
                                        )
                                        uploadImageToFirebase(it.result?.user?.uid!!, activity)

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
                                this,
                                "Your password needs to be at least 6 characters long",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    } else {
                        registerFail()
                        Toast.makeText(this, "Your passwords don't match", Toast.LENGTH_LONG).show()
                    }
                } else {
                    registerFail()
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
                }
            } else {
                registerFail()
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_LONG).show()
            }
        } else {
            registerFail()
            Toast.makeText(this, "Please choose a profile photo", Toast.LENGTH_LONG)
                .show()
        }


    }


    private fun registerFail() {
        registerButton.visibility = View.VISIBLE
        registerButtonBlinking.visibility = View.GONE
        registerButtonBlinkingBackground.visibility = View.GONE
    }

    private fun uploadImageToFirebase(userUid: String, activity: Activity) {

        if (selectedPhotoUri == null) return

        val imageFile = File.createTempFile("DereProfilePictureFile", "temporary")
        val myInputStream = activity.contentResolver.openInputStream(Uri.parse(selectedPhotoUri.toString()))

        FileUtils.copyInputStreamToFile(myInputStream, imageFile)

        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"
        val resizedImage = Resizer(this)
            .setTargetLength(1200)
            .setQuality(100)
            .setOutputFormat("PNG")
            .setOutputFilename("DereProfilePicture")
            .setOutputDirPath(path)
            .setSourceImage(imageFile)
            .resizedFile


        val ref = FirebaseStorage.getInstance().getReference("/images/users-profile-image/$userUid")

//        ref.putFile(selectedPhotoUri!!)
        ref.putFile(
            Uri.fromFile(
                resizedImage
            )!!
        )
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { profileImageUri ->
                    Log.d("RegisterActivity", "File location: $profileImageUri")

                    addUserToFirebaseDatabase(profileImageUri.toString())

                    val resizedImageFile = File(profileImageUri.path)
                    if (resizedImageFile.exists()) {
                        if (resizedImageFile.delete()) {
                            Log.d("deleteOperation", "deleted big file")
                        } else {
                            Log.d("deleteOperation", "couldn't delete big file")
                        }
                    }
                }

            }.addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image to server $it")
            }
    }

    override fun onStart() {
        super.onStart()

        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            FirebaseAuth.getInstance().currentUser!!.reload().addOnSuccessListener {
                val updatedUser = FirebaseAuth.getInstance().currentUser
                if (updatedUser!!.isEmailVerified) {

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }

    private fun addUserToFirebaseDatabase(userImageUrl: String) {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/profile")

        userName = register_name.text.toString().trimEnd()

        val newUser =
            Users(uid!!, userName, userEmail, userImageUrl, 0, "Watch me as I get Dere", System.currentTimeMillis())

        ref.setValue(newUser)
            .addOnSuccessListener {

                val staxRef = FirebaseDatabase.getInstance().getReference("/users/$uid/stax")
                staxRef.setValue("").addOnSuccessListener {
                    Log.d("RegisterActivity", "Saved user to Firebase Database")

                    val user = FirebaseAuth.getInstance().currentUser

                    user!!.sendEmailVerification().addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Please check your email and click the link in our message",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }
            }.addOnFailureListener {
                Log.d("RegisterActivity", "Failed to save user to database")
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
    }

}
