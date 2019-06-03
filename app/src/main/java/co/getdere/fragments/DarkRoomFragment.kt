package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.CameraActivity

import co.getdere.R
import co.getdere.roomclasses.LocalImagePost
import co.getdere.roomclasses.LocalImageViewModel
import co.getdere.viewmodels.SharedViewModelLocalImagePost
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.feed_single_photo.view.*
import kotlinx.android.synthetic.main.fragment_dark_room.*
import java.io.File


class DarkRoomFragment : Fragment() {

    private lateinit var localImageViewModel: LocalImageViewModel
    private lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
    val adapter = GroupAdapter<ViewHolder>()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            localImageViewModel = ViewModelProviders.of(it).get(LocalImageViewModel::class.java)
            sharedViewModelLocalImagePost = ViewModelProviders.of(it).get(SharedViewModelLocalImagePost::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dark_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as CameraActivity

        val recyclerView = dark_room_recyclerview
        val addImageFab = dark_room_fab

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)

        localImageViewModel.allImagePosts.observe(this, Observer { images ->
            adapter.clear()
            images?.let {
                for (i in images) {
                    adapter.add(DarkRoomGroupieAdapter(i))
                    activity.localImagePost.postValue(i)
                }
            }
        })

        adapter.setOnItemClickListener { item, _ ->

            val adapterImage = item as DarkRoomGroupieAdapter

            sharedViewModelLocalImagePost.sharedImagePostObject.postValue(adapterImage.image)
            activity.subFm.beginTransaction().add(R.id.feed_subcontents_frame_container, activity.darkRoomEditFragment, "darkRoomEditFragment").addToBackStack("darkRoomEditFragment").commit()
            activity.subActive = activity.darkRoomEditFragment

            activity.switchVisibility(1)
        }


        addImageFab.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Main", "Photo was selected")

            val selectedPhotoUri = data.data

            if (selectedPhotoUri != null) {
                val localImagePost = LocalImagePost(
                    System.currentTimeMillis(),
                    0.0,
                    0.0,
                    selectedPhotoUri.toString(),
                    "",
                    "",
                    verified = false
                )

                localImageViewModel.insert(localImagePost)

                val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                firebaseAnalytics.logEvent("image_uploaded_from_device", null)
            }
        }
    }


    companion object {
        fun newInstance(): DarkRoomFragment = DarkRoomFragment()
    }
}


class DarkRoomGroupieAdapter(val image: LocalImagePost) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val photoHolder = viewHolder.itemView.feed_single_photo_photo
        Glide.with(viewHolder.root.context).load(image.imageUri).into(photoHolder)
    }
}
