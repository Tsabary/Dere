package co.getdere.fragments


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity

import co.getdere.R
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import co.getdere.viewmodels.SharedViewModelTags
import kotlinx.android.synthetic.main.fragment_photo_editor.*
import java.io.File


class ApprovePhotoFragment : Fragment() {


    private lateinit var localImageViewModel: LocalImageViewModel
    private lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    lateinit var localImagePost: LocalImagePost


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelLocalImagePost = ViewModelProviders.of(it).get(SharedViewModelLocalImagePost::class.java)
            localImageViewModel = ViewModelProviders.of(it).get(LocalImageViewModel::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModelLocalImagePost.sharedImagePostObject.observe(this, Observer {
            it?.let { localImageObject ->
                localImagePost = localImageObject
            }
        })


        val activity = activity as CameraActivity

        photo_editor_keep.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive)
                .add(R.id.camera_subcontents_frame_container, activity.darkRoomEditFragment, "darkRoomEditFragment")
                .commit()
        }

        photo_editor_discard.setOnClickListener {
            activity.switchVisibility(0)
            localImageViewModel.delete(localImagePost)

            val imageFile = File(localImagePost.imageUri)
            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    Log.d("deleteOperation", "deleted big file")

                    activity.sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(imageFile)
                        )
                    )


                } else {
                    Log.d("deleteOperation", "couldn't delete big file")
                }
            }
        }
    }


    companion object {
        fun newInstance(): ApprovePhotoFragment = ApprovePhotoFragment()
    }

}
