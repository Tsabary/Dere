package co.getdere

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import co.getdere.fragments.DarkRoomEditFragment
import co.getdere.fragments.NewPhotoFragment
import co.getdere.roomclasses.LocalImagePost
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.content_camera.*
import kotlinx.android.synthetic.main.subcontent_camera.*
import androidx.lifecycle.ViewModelProviders
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import android.net.Uri
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File


class CameraActivity : AppCompatActivity() {

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout

    var currentPhotoUri = ""

    val fm = supportFragmentManager
    val subFm = supportFragmentManager

    lateinit var active: Fragment
    lateinit var subActive: Fragment

    lateinit var newPhotoFragment: NewPhotoFragment
    lateinit var darkRoomEditFragment: DarkRoomEditFragment

    var localImagePost = MutableLiveData<LocalImagePost>()

    var locationLat: Double = 0.0
    var locationLong: Double = 0.0
    var timeStamp: Long = 0
    var lastImageTaken = ""

    private lateinit var localImageViewModel: LocalImageViewModel
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {

            if (requestCode == 1){
                Log.d("ucroppp", "activityResult")

                val localImagePost = LocalImagePost(
                    timeStamp,
                    locationLong,
                    locationLat,
                    UCrop.getOutput(data!!)!!.path!!,
                    "",
                    "",
                    true
                )
                Log.d("photoActivity", "Took photo")

                localImageViewModel.insert(localImagePost)

                sharedViewModelLocalImagePost.sharedImagePostObject.postValue(localImagePost)
            }
//            else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
//                Log.d("Main", "Photo was selected")
//
//                val selectedPhotoUri = data.data
//
//                if (selectedPhotoUri != null) {
//                    val localImagePost = LocalImagePost(
//                        System.currentTimeMillis(),
//                        0.0,
//                        0.0,
//                        selectedPhotoUri.toString(),
//                        "",
//                        "",
//                        verified = false
//                    )
//
//                    localImageViewModel.insert(localImagePost)
//
//                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
//                    firebaseAnalytics.logEvent("image_uploaded_from_device", null)
//                }
//            }





//            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//                val resultUri = UCrop.getOutput(data!!)
//                Log.d("ucroppp", "all good")
//                Log.d("ucroppp", resultUri.toString())
//
//            } else if (resultCode == UCrop.RESULT_ERROR) {
//                val cropError = UCrop.getError(data!!)
//                Log.d("ucroppp", "someError")
//                Log.d("ucroppp", cropError.toString())
//            } else {
//                Log.d("ucroppp", "no conditions satisfied")
//            }
        } else {
            val imageFile = File(lastImageTaken)
            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    Log.d("deleteOperation", "deleted last image taken file")
                    sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(imageFile)
                        )
                    )
                } else {
                    Log.d("deleteOperation", "couldn't delete last image taken file")
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        localImageViewModel = ViewModelProviders.of(this).get(LocalImageViewModel::class.java)
        sharedViewModelLocalImagePost = ViewModelProviders.of(this).get(SharedViewModelLocalImagePost::class.java)

        mainFrame = camera_frame_container
        subFrame = camera_subcontents_frame_container

        newPhotoFragment = NewPhotoFragment()
        darkRoomEditFragment = DarkRoomEditFragment()
        subActive = darkRoomEditFragment

        fm.beginTransaction().add(R.id.camera_frame_container, newPhotoFragment, "newPhotoFragment").commit()
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

        if (mainFrame.visibility == View.GONE) {

            when (subActive) {
                darkRoomEditFragment -> {
                    subFm.beginTransaction().remove(darkRoomEditFragment).commit()
                    switchVisibility(0)
                }
            }


        } else {
            super.onBackPressed()
        }
    }
}
