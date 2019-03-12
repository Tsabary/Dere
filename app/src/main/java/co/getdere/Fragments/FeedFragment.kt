package co.getdere.Fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.getdere.CameraActivity
import co.getdere.MainActivity
import co.getdere.Models.FeedImage
import co.getdere.R
import co.getdere.RegisterLogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : Fragment() {

    val galleryAdapter = GroupAdapter<ViewHolder>()
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    lateinit var mainActivity : Activity


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpGalleryAdapter()
        setHasOptionsMenu(true)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Feed"

    }

    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }


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


        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.feed_navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.destination_camera -> {

                if (hasNoPermissions()) {
                    requestPermission()
                } else {

                    val intent = Intent(this.context, CameraActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }

        }

        return super.onOptionsItemSelected(item)

    }


    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(mainActivity, permissions, 0)
    }


}
