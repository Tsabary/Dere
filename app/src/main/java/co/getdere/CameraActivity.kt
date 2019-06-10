package co.getdere

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import co.getdere.fragments.DarkRoomEditFragment
import co.getdere.fragments.NewPhotoFragment
import co.getdere.fragments.SingleTagForList
import co.getdere.fragments.SingleTagSuggestion
import co.getdere.models.Users
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import co.getdere.viewmodels.SharedViewModelTags
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.content_camera.*
import kotlinx.android.synthetic.main.subcontent_camera.*


class CameraActivity : AppCompatActivity() {

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    lateinit var newPhotoFragment: NewPhotoFragment
    lateinit var darkRoomEditFragment: DarkRoomEditFragment
    var localImagePost = MutableLiveData<LocalImagePost>()

    private lateinit var localImageViewModel: LocalImageViewModel
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    lateinit var sharedViewModelTags: SharedViewModelTags
    lateinit var sharedViewModelCurrentUser: SharedViewModelCurrentUser

    var tags: MutableList<SingleTagForList> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        fetchCurrentUser(FirebaseAuth.getInstance().uid!!)

        localImageViewModel = ViewModelProviders.of(this).get(LocalImageViewModel::class.java)
        sharedViewModelLocalImagePost = ViewModelProviders.of(this).get(SharedViewModelLocalImagePost::class.java)
        sharedViewModelTags = ViewModelProviders.of(this).get(SharedViewModelTags::class.java)
        sharedViewModelCurrentUser = ViewModelProviders.of(this).get(SharedViewModelCurrentUser::class.java)

        mainFrame = camera_frame_container
        subFrame = camera_subcontents_frame_container

        newPhotoFragment = NewPhotoFragment()
        darkRoomEditFragment = DarkRoomEditFragment()

        fm.beginTransaction().add(mainFrame.id, newPhotoFragment, "newPhotoFragment").commit()
        active = newPhotoFragment

        FirebaseDatabase.getInstance().getReference("/tags")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    for (tag in p0.children) {
                        val tagName = tag.key.toString()
                        val count = tag.childrenCount.toInt()
                        tags.add(SingleTagForList(tagName, count))
                        sharedViewModelTags.tagList = tags
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    fun switchVisibility(case: Int) {

        if (case == 0) {
            mainFrame.visibility = View.VISIBLE
            subFrame.visibility = View.GONE
        } else {
            mainFrame.visibility = View.GONE
            subFrame.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (mainFrame.visibility == 8) {
            subFm.beginTransaction()
                .remove(darkRoomEditFragment).commit()
            switchVisibility(0)
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchCurrentUser(uid: String) {
        FirebaseDatabase.getInstance().getReference("/users/$uid/profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.getValue(Users::class.java) != null) {
                        val currentUser = p0.getValue(Users::class.java)
                        if (currentUser != null) {
                            sharedViewModelCurrentUser.currentUserObject = currentUser
                        }
                    }
                }
            })
    }
}
