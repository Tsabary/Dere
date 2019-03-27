package co.getdere.Fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.Models.Images
import co.getdere.Models.SimpleString

import co.getdere.R
import co.getdere.ViewModels.SharedViewModelImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_to_bucket_row.view.*


class AddToBucketFragment : Fragment() {

    private lateinit var imageId: String
    private lateinit var sharedViewModelForImage: SharedViewModelImage

    val bucketsAdapter = GroupAdapter<ViewHolder>()
    val bucketsLayoutManager = LinearLayoutManager(this.context)

    val uid = FirebaseAuth.getInstance().uid


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_bucket, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = AddToBucketFragmentArgs.fromBundle(it)
            imageId = safeArgs.image
        }



        val recycler = view.findViewById<RecyclerView>(R.id.add_to_bucket_recycler)
        val newBucketInput = view.findViewById<EditText>(R.id.add_to_bucket_new_input)
        val bucketAddButton = view.findViewById<TextView>(R.id.add_to_bucket_new_button)

        recycler.adapter = bucketsAdapter
        recycler.layoutManager = bucketsLayoutManager

        val allBucketsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets")

        allBucketsRef.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                bucketsAdapter.add(SingleBucketSuggestion(p0.key!!, imageId))

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }


        })










        bucketAddButton.setOnClickListener {

            if (newBucketInput.text.isNotEmpty()) {

                val refBuckets = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets/")

                refBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.hasChild(newBucketInput.text.toString())) {
                            Toast.makeText(context, "A bucket with the same name already exists", Toast.LENGTH_LONG)
                                .show()

                        } else {

                            val ref = FirebaseDatabase.getInstance()
                                .getReference("/users/$uid/buckets/${newBucketInput.text}/$imageId")

                            ref.setValue(SimpleString(imageId))

                            val imageBucketingRef = FirebaseDatabase.getInstance().getReference("/images/$imageId/buckets/$uid")

                            imageBucketingRef.setValue(SimpleString(uid!!))

                            newBucketInput.text.clear()

                        }
                    }


                })


            } else {
                Toast.makeText(this.context, "Please give your new bucket a name", Toast.LENGTH_LONG).show()
            }


        }


    }


//    private fun listenToBuckets(imageId : String){
//
//
//        val refBuckets = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets/")
//
//        refBuckets.addChildEventListener(object : ChildEventListener{
//
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                val bucket = p0.getValue(String::class.java)
//
//                bucketsAdapter.add(SingleBucketSuggestion(bucket!!, imageId))
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//            }
//
//
//        })
//
//
//    }
//
//
//
//
//
























//    private fun checkIfBucketed(ranNum: Int) {
//
//        val refUserBucket = FirebaseDatabase.getInstance().getReference("/buckets/users/${currentUser.uid}")
//
//        refUserBucket.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                if (p0.hasChild(imageObject.id)) {
//                    if (ranNum == 1) {
//
//                        if (currentUser.uid == imageObject.photographer) {
//                            Toast.makeText(context, "You can't bucket your own photos", Toast.LENGTH_SHORT).show()
//                        } else {
//                            addToBucket.setImageResource(R.drawable.bucket)
//
//                            refUserBucket.child(imageObject.id).removeValue().addOnCompleteListener {
//                                val refImageBucketingList =
//                                    FirebaseDatabase.getInstance().getReference("/buckets/images/${imageObject.id}")
//
//                                refImageBucketingList.child(currentUser.uid).removeValue().addOnCompleteListener {
//                                    listenToBucketCount()
//                                }
//                            }
//                        }
//
//
//                    } else {
//                        addToBucket.setImageResource(R.drawable.bucket_saved)
//                    }
//
//
//                } else {
//                    if (ranNum == 1) {
//
//                        if (currentUser.uid == imageObject.photographer) {
//                            Toast.makeText(context, "You can't bucket your own photos", Toast.LENGTH_SHORT).show()
//                        } else {
//                            addToBucket.setImageResource(R.drawable.bucket_saved)
//
//                            val refCurrentUserBucket =
//                                FirebaseDatabase.getInstance()
//                                    .getReference("/buckets/users/${currentUser.uid}/${imageObject.id}")
//                            val bucketedImage = SimpleString(imageObject.id)
//                            refCurrentUserBucket.setValue(bucketedImage).addOnCompleteListener {
//                                val refCurrentImageBucket =
//                                    FirebaseDatabase.getInstance()
//                                        .getReference("/buckets/images/${imageObject.id}/${currentUser.uid}")
//                                val bucketingUser = SimpleString(currentUser.uid)
//                                refCurrentImageBucket.setValue(bucketingUser).addOnCompleteListener {
//                                    listenToBucketCount()
//                                }
//                            }
//                        }
//
//                    } else {
//                        addToBucket.setImageResource(R.drawable.bucket)
//
//                    }
//
//                }
//            }
//
//        })
//
//
//    }
//
//


}

class SingleBucketSuggestion(private val bucketName: String, val imageId: String) : Item<ViewHolder>() {


    val uid = FirebaseAuth.getInstance().uid


    override fun getLayout(): Int {
        return R.layout.add_to_bucket_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.add_to_bucket_name.text = bucketName

        viewHolder.itemView.add_to_bucket_add.setOnClickListener {

            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets/${bucketName}/$imageId")

            ref.setValue(SimpleString(imageId))

        }


    }

}