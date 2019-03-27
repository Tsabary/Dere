package co.getdere.Fragments


import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.getdere.Models.Users
import co.getdere.R
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*


class EditProfileFragment : Fragment() {


    lateinit var user: Users

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

        val userRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/profile")

        val userImage = view.findViewById<CircleImageView>(R.id.edit_profile_image)
        val tagLineInput = view.findViewById<EditText>(R.id.edit_profile_description)
        val userNameInput = view.findViewById<EditText>(R.id.edit_profile_name)
        val userInstagramInput = view.findViewById<EditText>(R.id.edit_profile_instagram)

        val saveButton = view.findViewById<TextView>(R.id.edit_profile_save)
        val cancelButton = view.findViewById<TextView>(R.id.edit_profile_cancel)




        Glide.with(this).load(user.image).into(userImage)
        tagLineInput.setText(user.tagline)
        userNameInput.setText(user.name)

        saveButton.setOnClickListener {
            if (userNameInput.length() > 2){
                userRef.updateChildren(mapOf("name" to userNameInput.text.toString()))
                userRef.updateChildren(mapOf("tagline" to userNameInput.text.toString()))

                if (userInstagramInput.text.isNotEmpty()){

                    val userStaxRef = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/stax/")

                    userStaxRef.setValue(mapOf("instagram" to userInstagramInput.text.toString()))

                }





            } else {
                Toast.makeText(this.context, "Name can't be less than 2 letters", Toast.LENGTH_LONG).show()
            }



        }

    }
}