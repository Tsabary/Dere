package co.getdere.Fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import co.getdere.Models.FeedImage
import co.getdere.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feed.*
import java.util.zip.Inflater

class FeedFragment : Fragment() {

    val galleryAdapter = GroupAdapter<ViewHolder>()


    private fun setUpGalleryAdapter() {

        feed_gallary.adapter = galleryAdapter
        val galleryLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this.context, 3)
        feed_gallary.layoutManager =
            galleryLayoutManager //not sure about this suggestion, try without if problems occur

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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setupActionBar()
    }

    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }


//    private fun setupActionBar() {
//
//        val mToolbar = view!!.findViewById<Toolbar>(R.id.feed_toolbar)
//        AppCompatActivity().setSupportActionBar(mToolbar)
//    }
}
