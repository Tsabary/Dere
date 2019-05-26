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
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        localImageViewModel = ViewModelProviders.of(this).get(LocalImageViewModel::class.java)
        sharedViewModelLocalImagePost = ViewModelProviders.of(this).get(SharedViewModelLocalImagePost::class.java)

        mainFrame = camera_frame_container
        subFrame = camera_subcontents_frame_container

        newPhotoFragment = NewPhotoFragment()
        darkRoomEditFragment = DarkRoomEditFragment()

        fm.beginTransaction().add(mainFrame.id, newPhotoFragment, "newPhotoFragment").commit()
        active = newPhotoFragment

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
}
