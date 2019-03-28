package co.getdere.Fragments


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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.getdere.MainActivity
import co.getdere.Models.Users
import co.getdere.ViewModels.SharedViewModelCurrentUser
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.view.*
import java.util.*


class EditProfileFragment : Fragment() {


    lateinit var user: Users
    lateinit var userImage: CircleImageView
    lateinit var tagLineInput: EditText
    lateinit var userNameInput: EditText
    lateinit var userInstagramInput: EditText

    lateinit var sharedViewModelForCurrentUser: SharedViewModelCurrentUser

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForCurrentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java)
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

        arguments?.let {
            val safeArgs = EditProfileFragmentArgs.fromBundle(it)
            user = safeArgs.user
        }


        userImage = view.findViewById<CircleImageView>(co.getdere.R.id.edit_profile_image)
        tagLineInput = view.findViewById<EditText>(co.getdere.R.id.edit_profile_description)
        userNameInput = view.findViewById<EditText>(co.getdere.R.id.edit_profile_name)
        userInstagramInput = view.findViewById<EditText>(co.getdere.R.id.edit_profile_instagram)

        val saveButton = view.findViewById<TextView>(co.getdere.R.id.edit_profile_save)
        val cancelButton = view.findViewById<TextView>(co.getdere.R.id.edit_profile_cancel)


        setUpUserDetails()


        userImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }






        saveButton.setOnClickListener {

            uploadImageToFirebase(user.image)

        }

    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Main", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap((activity as MainActivity).contentResolver, selectedPhotoUri)
            userImage.setImageBitmap(bitmap)
//            register_photo_pick.alpha = 0f

        }
    }


    private fun performUpdateProfile(imageUri: String) {

        val userRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/profile")


        if (userNameInput.length() > 2) {
            userRef.updateChildren(mapOf("name" to userNameInput.text.toString())).addOnSuccessListener {


                userRef.updateChildren(mapOf("tagline" to tagLineInput.text.toString())).addOnSuccessListener {

                    userRef.updateChildren(mapOf("image" to imageUri)).addOnSuccessListener {


                        val userStaxRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/stax/")

                        userStaxRef.setValue(mapOf("instagram" to userInstagramInput.text.toString()))
                            .addOnSuccessListener {

                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        val user = p0.getValue(Users::class.java)


                                        sharedViewModelForCurrentUser.currentUserObject = user!!

//
                                        val action =
                                            EditProfileFragmentDirections.actionDestinationEditProfileToDestinationProfileLoggedInUser()
                                        findNavController().navigate(action)
                                    }

                                })
                            }


                    }

                }
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


        Glide.with(this).load(user.image).into(userImage)
        tagLineInput.setText(user.tagline)
        userNameInput.setText(user.name)

        userInstagramRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                userInstagramInput.setText(p0.value.toString())
            }


        })
    }

}