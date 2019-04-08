package co.getdere.fragments


import android.content.Context
import android.os.Bundle
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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder


class DarkRoomFragment : Fragment() {

    private lateinit var localImageViewModel: LocalImageViewModel
    lateinit var sharedViewModelLocalImagePost: SharedViewModelLocalImagePost
//    lateinit var localImagePost: LocalImagePost


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            localImageViewModel = ViewModelProviders.of(this).get(LocalImageViewModel::class.java)
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.dark_room_recyclerview)
//        val adapter = LocalImageListAdapter(this.context!!)
        val adapter = GroupAdapter<ViewHolder>()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)

        localImageViewModel.allImagePosts.observe(this, Observer { images ->

            adapter.clear()
            Log.d("ClearInitiated", "and repopulating")

            images?.let {
                for (i in images) {
                    adapter.add(DarkRoomGroupieAdapter(i))
                }
            }
        })

        adapter.setOnItemClickListener { item, _ ->

            val adapterImage = item as DarkRoomGroupieAdapter

            sharedViewModelLocalImagePost.sharedImagePostObject.postValue(adapterImage.image)

            activity.switchVisibility(1)
        }

    }


    companion object {
        fun newInstance(): DarkRoomFragment = DarkRoomFragment()
        const val newWordActivityRequestCode = 1
    }
}


class DarkRoomGroupieAdapter(val image: LocalImagePost) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.feed_single_photo
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val photoHolder = viewHolder.itemView.findViewById<ImageView>(R.id.feed_single_photo_photo)
        Glide.with(viewHolder.root.context).load(image.imageUri).into(photoHolder)
    }


}
