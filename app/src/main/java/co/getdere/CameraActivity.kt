package co.getdere

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_camera.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontent_camera.*
import kotlinx.android.synthetic.main.subcontents_main.*
import mumayank.com.airlocationlibrary.AirLocation
import pub.devrel.easypermissions.EasyPermissions.onRequestPermissionsResult



class CameraActivity : AppCompatActivity() {

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    lateinit var newPhotoFragment : NewPhotoFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mainFrame = camera_frame_container
        subFrame = camera_subcontents_frame_container

        newPhotoFragment = NewPhotoFragment()

        fm.beginTransaction().add(R.id.camera_frame_container, newPhotoFragment, "newPhotoFragment").commit()

    }



}
