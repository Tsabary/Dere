package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.util.*


class EditProfileFragment : Fragment() {


    lateinit var user: Users
    lateinit var userImage: CircleImageView
    lateinit var tagLineInput: EditText
    lateinit var userNameInput: EditText
    lateinit var userInstagramInput: EditText
    lateinit var saveButton: TextView

    lateinit var sharedViewModelForCurrentUser: SharedViewModelCurrentUser

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)

            user = sharedViewModelForCurrentUser.currentUserObject
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(co.getdere.R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userImage = edit_profile_image
        tagLineInput = edit_profile_description
        userNameInput = edit_profile_name
        userInstagramInput = edit_profile_instagram

        saveButton = edit_profile_save

        setUpUserDetails()


        userImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        saveButton.setOnClickListener {
            saveButton.isClickable = false
            edit_profile_loading.visibility = View.VISIBLE
            uploadImageToFirebase(user.image)
        }

    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Main", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap =
                MediaStore.Images.Media.getBitmap((activity as MainActivity).contentResolver, selectedPhotoUri)
            userImage.setImageBitmap(bitmap)
        }
    }


    private fun performUpdateProfile(imageUri: String) {

        val userRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/profile")


        if (userNameInput.length() > 2) {
            userRef.child("name").setValue(userNameInput.text.toString().trimEnd()).addOnSuccessListener {


                userRef.child("tagline").setValue(tagLineInput.text.toString().trimEnd()).addOnSuccessListener {

                    userRef.child("image").setValue(imageUri).addOnSuccessListener {

                        val userStaxRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/stax/")

                        userStaxRef.child("instagram").setValue(userInstagramInput.text.toString().trimEnd())
                            .addOnSuccessListener {

                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        val user = p0.getValue(Users::class.java)


                                        sharedViewModelForCurrentUser.currentUserObject = user!!

                                        val activity = activity as MainActivity

                                        activity.fm.beginTransaction().detach(activity.profileLoggedInUserFragment)
                                            .attach(activity.profileLoggedInUserFragment).commit()

                                        activity.switchVisibility(0)

                                        saveButton.isClickable = true
                                        edit_profile_loading.visibility = View.GONE
                                    }

                                })
                            }.addOnFailureListener {
                                saveButton.isClickable = true
                                edit_profile_loading.visibility = View.GONE
                            }

                    }.addOnFailureListener {
                        saveButton.isClickable = true
                        edit_profile_loading.visibility = View.GONE
                    }

                }.addOnFailureListener {
                    saveButton.isClickable = true
                    edit_profile_loading.visibility = View.GONE
                }
            }.addOnFailureListener {
                saveButton.isClickable = true
                edit_profile_loading.visibility = View.GONE
            }
        } else {
            Toast.makeText(this.context, "Name can't be less than 2 letters", Toast.LENGTH_LONG).show()
        }


    }


    private fun uploadImageToFirebase(currentImageUri: String) {

        if (selectedPhotoUri != null) {

            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/userprofile/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully uploaded image ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener { newImageUri ->
                        Log.d("RegisterActivity", "File location: $newImageUri")

                        performUpdateProfile(newImageUri.toString())
                    }

                }.addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload image to server $it")

                }

        } else {
            performUpdateProfile(currentImageUri)
        }


    }


    private fun setUpUserDetails() {

        val userInstagramRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/stax/instagram")


        Glide.with(this).load(if(user.image.isNotEmpty()){user.image}else{
            R.drawable.user_profile}).into(userImage)
        tagLineInput.setText(user.tagline)
        userNameInput.setText(user.name)

        userInstagramRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val instagramHandle = p0.value.toString()
                if (instagramHandle == "null") {
                    userInstagramInput.setText("")
                } else
                    userInstagramInput.setText(instagramHandle)
            }


        })
    }

}