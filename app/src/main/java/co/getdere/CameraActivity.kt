package co.getdere

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.getdere.fragments.DarkRoomEditFragment
import co.getdere.fragments.NewPhotoFragment
import co.getdere.fragments.PhotoEditorFragment
import co.getdere.roomclasses.LocalImagePost
import kotlinx.android.synthetic.main.content_camera.*
import kotlinx.android.synthetic.main.subcontent_camera.*


class CameraActivity : AppCompatActivity() {

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    var currentPhotoUri = ""

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    lateinit var newPhotoFragment: NewPhotoFragment
    lateinit var photoEditorFragment: PhotoEditorFragment
    lateinit var darkRoomEditFragment : DarkRoomEditFragment

    var localImagePost = MutableLiveData<LocalImagePost>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mainFrame = camera_frame_container
        subFrame = camera_subcontents_frame_container

        newPhotoFragment = NewPhotoFragment()
        photoEditorFragment = PhotoEditorFragment()
        darkRoomEditFragment = DarkRoomEditFragment()

        fm.beginTransaction().add(R.id.camera_frame_container, newPhotoFragment, "newPhotoFragment").commit()
        active = newPhotoFragment


        subFm.beginTransaction().add(R.id.camera_subcontents_frame_container, photoEditorFragment, "photoEditorFragment").commit()
        subActive = photoEditorFragment

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

        if (mainFrame.visibility == View.GONE){

            when (subActive) {
                darkRoomEditFragment -> {subFm.beginTransaction().remove(darkRoomEditFragment).commit()
                switchVisibility(0)}
            }


        } else{
            super.onBackPressed()
        }
    }
}
