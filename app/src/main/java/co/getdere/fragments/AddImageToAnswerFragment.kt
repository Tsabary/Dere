package co.getdere.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.groupieAdapters.ImageSelector
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.viewmodels.*
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_add_image_to_collection.*

class AddImageToAnswerFragment : Fragment() {

    lateinit var sharedViewModelAnswerImages: SharedViewModelAnswerImages
    lateinit var currentUser: Users
    val galleryAdapter = GroupAdapter<ViewHolder>()
    var myImageList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_image_to_collection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity

        val galleryRecycler = add_image_to_collection_recycler
        galleryRecycler.adapter = galleryAdapter
        galleryRecycler.layoutManager = GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)

        activity.let {
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(SharedViewModelAnswerImages::class.java)
            sharedViewModelAnswerImages.imageList.observe(activity, Observer { mutableList ->
                mutableList?.let { existingImageList ->
                    myImageList = existingImageList
                }
            })
        }

        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/images")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    for (imagePath in p0.children) {

                        FirebaseDatabase.getInstance().getReference("/images/${imagePath.key}/body")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {
                                    val imageObject = p0.getValue(Images::class.java)
                                    if (imageObject != null) {
                                        galleryAdapter.add(ImageSelector(imageObject, activity, "answer", 0))
                                    }
                                }
                            })
                    }
                }
            })

        galleryAdapter.setOnItemClickListener { item, _ ->
            val image = item as ImageSelector

            if (!myImageList.contains(image.image.id)) {
                myImageList.add(image.image.id)
                sharedViewModelAnswerImages.imageList.postValue(myImageList)
            } else {
                myImageList.remove(image.image.id)
                sharedViewModelAnswerImages.imageList.postValue(myImageList)
            }

            activity.subFm.popBackStack("addImageToAnswer", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity.subActive = activity.answerFragment
        }
    }

    companion object {
        fun newInstance(): AddImageToAnswerFragment = AddImageToAnswerFragment()
    }
}
