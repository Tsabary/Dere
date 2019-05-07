package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import co.getdere.CameraActivity

import co.getdere.R
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import kotlinx.android.synthetic.main.fragment_photo_editor.*


class PhotoEditorFragment : Fragment() {





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity= activity as CameraActivity

        photo_editor_keep.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive)
                .add(R.id.camera_subcontents_frame_container, activity.darkRoomEditFragment, "darkRoomEditFragment").commit()
        }

    }


    companion object {
        fun newInstance(): PhotoEditorFragment = PhotoEditorFragment()
    }

}
