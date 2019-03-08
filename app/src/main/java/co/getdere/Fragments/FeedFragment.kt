package co.getdere.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.getdere.Models.FeedImage
import co.getdere.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : Fragment() {

    val galleryAdapter = GroupAdapter<ViewHolder>()


    private fun setUpGalleryAdapter() {

        feed_gallary.adapter = galleryAdapter
        val galleryLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)
        feed_gallary.layoutManager = galleryLayoutManager //not sure about this suggestion, try without if problems occur

        val dummyUri =
            "https://firebasestorage.googleapis.com/v0/b/dere-3d530.appspot.com/o/20150923_100950.jpg?alt=media&token=97f4b02c-75d9-4d5d-bc86-a3ffaa3a0011"

        val imageUri = Uri.parse(dummyUri)
        if (imageUri != null) {
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))
            galleryAdapter.add(FeedImage(imageUri))


        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpGalleryAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feed, container, false)



    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }

}
