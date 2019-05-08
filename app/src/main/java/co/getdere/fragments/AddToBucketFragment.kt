package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Images
import co.getdere.models.Users
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_to_bucket_row.view.*
import kotlinx.android.synthetic.main.fragment_add_to_bucket.*


class AddToBucketFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var imageObject: Images
    private lateinit var currentUser: Users
    lateinit var recycler: RecyclerView

    val bucketsAdapter = GroupAdapter<ViewHolder>()
    private lateinit var bucketsLayoutManager: LinearLayoutManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForImage.sharedImageObject.observe(this, Observer { images ->
                images?.let { image ->
                    imageObject = image
                }
            })
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_bucket, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        recycler = add_to_bucket_recycler
        recycler.adapter = bucketsAdapter
        bucketsLayoutManager = LinearLayoutManager(this.context)
        recycler.layoutManager = bucketsLayoutManager

        listenToBuckets()

        val newBucketInput = add_to_bucket_new_input
        val bucketAddButton = add_to_bucket_new_button

        bucketAddButton.setOnClickListener {

            if (newBucketInput.text.isNotEmpty()) {

                val bucketName = newBucketInput.text.toString().trimEnd()
                val allBuckets = "All Buckets"

                val refBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/")

                refBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.hasChild(bucketName)) {
                            Toast.makeText(context, "A bucket with the same name already exists", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            refBuckets.child("$bucketName/${imageObject.id}").setValue(System.currentTimeMillis())
                                .addOnSuccessListener {

                                    refBuckets.child("$allBuckets/${imageObject.id}")
                                        .setValue(System.currentTimeMillis()).addOnSuccessListener {
                                        sharedViewModelForImage.sharedImageObject.postValue(Images())
                                        sharedViewModelForImage.sharedImageObject.postValue(imageObject)

                                        val imageBucketingRef =
                                            FirebaseDatabase.getInstance()
                                                .getReference("/images/${imageObject.id}/buckets/${currentUser.uid}")

                                        imageBucketingRef.setValue(true).addOnSuccessListener {

                                            for (t in imageObject.tags) {
                                                val refUserTags = FirebaseDatabase.getInstance()
                                                    .getReference("users/${currentUser.uid}/interests/$t")
                                                refUserTags.setValue(true)
                                            }

                                            closeKeyboard(activity)
                                            activity.profileLoggedInUserFragment.listenToImagesFromBucket()
                                            newBucketInput.text.clear()

                                            bucketsAdapter.clear()
                                            listenToBuckets()

                                            changeReputation(
                                                8,
                                                imageObject.id,
                                                imageObject.id,
                                                currentUser.uid,
                                                currentUser.name,
                                                imageObject.photographer,
                                                TextView(context),
                                                "bucket",
                                                activity
                                            )
                                        }
                                    }
                                }
                        }
                    }
                })


            } else {
                Toast.makeText(this.context, "Please give your new bucket a name", Toast.LENGTH_LONG).show()
            }


        }

    }


    fun listenToBuckets() {

        bucketsAdapter.clear()

        val allBucketsRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")

        allBucketsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("buckets")) {
                    recycler.visibility = View.VISIBLE

                    for (ds in p0.child("buckets").children) {
                        if (ds.key != "All Buckets") {
                            bucketsAdapter.add(
                                SingleBucketSuggestion(
                                    ds.key!!,
                                    imageObject,
                                    currentUser,
                                    activity as MainActivity
                                )
                            )
                        } else {
                            continue
                        }
                    }
                }
            }
        })
    }
}


class SingleBucketSuggestion(
    val bucketName: String,
    val image: Images,
    val currentUser: Users,
    val activity: Activity
) : Item<ViewHolder>(),
    DereMethods {


    val uid = FirebaseAuth.getInstance().uid


    override fun getLayout(): Int {
        return R.layout.add_to_bucket_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.add_to_bucket_name.text = bucketName

        val actionText = viewHolder.itemView.add_to_bucket_add

        executeBucket(0, bucketName, image, currentUser, actionText, viewHolder.root.context, activity as MainActivity)

        actionText.setOnClickListener {
            executeBucket(1, bucketName, image, currentUser, actionText, viewHolder.root.context, activity)
        }


    }


    private fun executeBucket(
        case: Int,
        bucketName: String,
        image: Images,
        currentUser: Users,
        actionText: TextView,
        context: Context,
        activity: MainActivity
    ) {


        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")
        val refImageBuckets =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets/${currentUser.uid}")


        refUserBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(bucketName)) {

                    refUserBuckets.child(bucketName).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            if (p0.hasChild(image.id)) {

                                if (case == 1) {

                                    refUserBuckets.child(bucketName).child(image.id).removeValue()
                                        .addOnSuccessListener {

                                            refUserBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onCancelled(p0: DatabaseError) {

                                                }

                                                override fun onDataChange(p0allBuckets: DataSnapshot) {

                                                    var existsInOtherBuckets = 0

                                                    for (bucket in p0allBuckets.children) {
                                                        if (bucket.key != "All Buckets") {
                                                            if (bucket.hasChild(image.id)) {
                                                                existsInOtherBuckets++
                                                                Log.d("existanceCountReached", existsInOtherBuckets.toString())
                                                            }
                                                        }

                                                        Log.d("existanceCount", existsInOtherBuckets.toString())
                                                    }

                                                    if (existsInOtherBuckets == 0) {
                                                        changeReputation(
                                                            9,
                                                            image.id,
                                                            image.id,
                                                            currentUser.uid,
                                                            currentUser.name,
                                                            image.photographer,
                                                            TextView(context),
                                                            "bucket",
                                                            activity
                                                        )

                                                        refImageBuckets.removeValue().addOnSuccessListener {
                                                            refUserBuckets.child("All Buckets/${image.id}").removeValue()
//                                                addReputationIfExistsInAnyBucket(context)
                                                        }
                                                    }

                                                }
                                            })


                                        }

                                    actionText.text = "ADD"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green700
                                        )
                                    )

                                    activity.sharedViewModelImage.sharedImageObject.postValue(Images())
                                    activity.sharedViewModelImage.sharedImageObject.postValue(image)

                                    activity.profileLoggedInUserFragment.listenToImagesFromBucket()
                                    activity.bucketFragment.listenToBuckets()

                                } else {
                                    actionText.text = "REMOVE"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.gray500
                                        )
                                    )
                                }

                            } else {

                                if (case == 1) {


                                    val ref =
                                        FirebaseDatabase.getInstance()
                                            .getReference("/users/$uid/buckets/$bucketName/${image.id}")

                                    ref.setValue(System.currentTimeMillis()).addOnSuccessListener {

                                        refImageBuckets.setValue(currentUser.uid)
                                            .addOnSuccessListener {

                                                refUserBuckets.child("All Buckets/${image.id}")
                                                    .setValue(System.currentTimeMillis())
                                                    .addOnSuccessListener {
                                                        for (t in image.tags) {
                                                            val refUserTags = FirebaseDatabase.getInstance()
                                                                .getReference("users/${currentUser.uid}/interests/$t")
                                                            refUserTags.setValue(true)
                                                        }

                                                        actionText.text = "REMOVE"
                                                        actionText.setTextColor(
                                                            ContextCompat.getColor(
                                                                context,
                                                                R.color.gray500
                                                            )
                                                        )

                                                        changeReputation(
                                                            8,
                                                            image.id,
                                                            image.id,
                                                            currentUser.uid,
                                                            currentUser.name,
                                                            image.photographer,
                                                            TextView(context),
                                                            "bucket",
                                                            activity
                                                        )

                                                        activity.sharedViewModelImage.sharedImageObject.postValue(Images())
                                                        activity.sharedViewModelImage.sharedImageObject.postValue(image)

                                                        activity.profileLoggedInUserFragment.listenToImagesFromBucket()
                                                        activity.bucketFragment.listenToBuckets()
                                                    }
                                            }
                                    }


                                } else {
                                    actionText.text = "ADD"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green700
                                        )
                                    )
                                }

                            }

                        }


                    })


                } else {

                    if (case == 1) {


                        val ref =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/$uid/buckets/$bucketName")

                        ref.setValue(mapOf(image.id to true)).addOnSuccessListener {

                            refImageBuckets.setValue(mapOf(currentUser.uid to true))
                                .addOnSuccessListener {

                                    actionText.text = "REMOVE"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.gray500
                                        )
                                    )

                                    changeReputation(
                                        8,
                                        image.id,
                                        image.id,
                                        currentUser.uid,
                                        currentUser.name,
                                        image.photographer,
                                        TextView(context),
                                        "bucket",
                                        activity
                                    )
                                }
                        }


                    } else {
                        actionText.text = "ADD"
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.green700
                            )
                        )
                    }

                }


            }


        })


    }


//    private fun addReputationIfExistsInAnyBucket(context: Context) {
//
//        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")
//
//        refUserBuckets.addChildEventListener(object : ChildEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//            }
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//
//                if (p0.hasChild(image.id)) {
//
//                    changeReputation(
//                        8,
//                        image.id,
//                        image.id,
//                        currentUser.uid,
//                        currentUser.name,
//                        image.photographer,
//                        TextView(context),
//                        "bucket",
//                        activity
//                    )
//
//                    val refImageBuckets =
//                        FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets")
//
//                    refImageBuckets.setValue(mapOf(currentUser.uid to true))
//
//                }
//
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//            }
//
//
//        })
//
//    }


}

