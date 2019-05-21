package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_to_collection_row.view.*
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

        add_to_collection_title.text = "Add to bucket"

        recycler = add_to_collection_recycler
        recycler.adapter = bucketsAdapter
        bucketsLayoutManager = LinearLayoutManager(this.context)
        recycler.layoutManager = bucketsLayoutManager

        listenToBuckets()

        val newBucketInput = add_to_collection_new_input
        newBucketInput.hint = "Name your bucket"
        val bucketAddButton = add_to_collection_new_button

        bucketAddButton.setOnClickListener {

            if (newBucketInput.text.isNotEmpty()) {

                val bucketName = newBucketInput.text.toString().trimEnd()
                val allBuckets = "All Buckets"

                val refBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/")

                refBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        var bucketNameExists = 0
                        for (bucket in p0.children){

                            if (bucket.child("body/title").value == bucketName){
                                bucketNameExists++
                            }
                        }




                        if (p0.hasChild(bucketName)) {
                            Toast.makeText(context, "A collection with the same name already exists", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            recycler.visibility = View.VISIBLE

                            refBuckets.child("$bucketName/${imageObject.id}").setValue(System.currentTimeMillis())
                                .addOnSuccessListener {

                                    refBuckets.child("$allBuckets/${imageObject.id}")
                                        .setValue(System.currentTimeMillis()).addOnSuccessListener {

                                            val imageBucketingRef =
                                                FirebaseDatabase.getInstance()
                                                    .getReference("/images/${imageObject.id}/buckets/${currentUser.uid}")

                                            imageBucketingRef.setValue(true).addOnSuccessListener {

                                                checkIfBucketed(
                                                    activity.imageFullSizeFragment.collectButton,
                                                    imageObject,
                                                    currentUser.uid
                                                )

                                                for (t in imageObject.tags) {
                                                    val refUserTags = FirebaseDatabase.getInstance()
                                                        .getReference("users/${currentUser.uid}/interests/$t")
                                                    refUserTags.setValue(true)
                                                }

                                                bucketsAdapter.add(
                                                    SingleBucketSuggestion(
                                                        bucketName,
                                                        imageObject,
                                                        currentUser,
                                                        activity
                                                    )
                                                )




                                                closeKeyboard(activity)
                                                activity.profileLoggedInUserFragment.listenToImagesFromBucket()
                                                newBucketInput.text.clear()

                                                changeReputation(
                                                    8,
                                                    imageObject.id,
                                                    imageObject.id,
                                                    currentUser.uid,
                                                    currentUser.name,
                                                    imageObject.photographer,
                                                    TextView(context),
                                                    "collection",
                                                    activity
                                                )

                                                val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
                                                firebaseAnalytics.logEvent("image_bucketed", null)
                                            }
                                        }
                                }
                        }
                    }
                })


            } else {
                Toast.makeText(this.context, "Please give your new collection a name", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun listenToBuckets() {

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

    companion object {
        fun newInstance(): AddToBucketFragment = AddToBucketFragment()
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
        return R.layout.add_to_collection_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.add_to_collection_name.setText(bucketName)

        val actionText = viewHolder.itemView.add_to_collection_add

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

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

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
                                                                Log.d(
                                                                    "existanceCountReached",
                                                                    existsInOtherBuckets.toString()
                                                                )
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
                                                            "collection",
                                                            activity
                                                        )

                                                        refImageBuckets.removeValue().addOnSuccessListener {
                                                            refUserBuckets.child("All Buckets/${image.id}")
                                                                .removeValue().addOnSuccessListener {
                                                                    firebaseAnalytics.logEvent("image_unbucketed", null)
                                                                    checkIfBucketed(
                                                                        activity.imageFullSizeFragment.collectButton,
                                                                        image,
                                                                        currentUser.uid
                                                                    )
                                                                    activity.profileLoggedInUserFragment.listenToImagesFromBucket()
                                                                }
                                                        }
                                                    }
//                                                    else {
//                                                        checkIfBucketed(
//                                                            activity.imageFullSizeFragment.collectButton,
//                                                            image,
//                                                            currentUser.uid
//                                                        )
//                                                    } //not needed because if it is not 0 then it is definitely still part of a collection

                                                }
                                            })


                                        }

                                    actionText.text = "Add"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green700
                                        )
                                    )

//                                    activity.sharedViewModelImage.sharedImageObject.postValue(Images())
//                                    activity.sharedViewModelImage.sharedImageObject.postValue(image) //not needed

                                    activity.profileLoggedInUserFragment.listenToImagesFromBucket()
//                                    activity.addToBucketFragment.listenToBuckets()

                                } else {
                                    actionText.text = "Remove"
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

                                                        actionText.text = "Remove"
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
                                                            "collection",
                                                            activity
                                                        )
//
//                                                        activity.sharedViewModelImage.sharedImageObject.postValue(Images())
//                                                        activity.sharedViewModelImage.sharedImageObject.postValue(image) // not needed

                                                        activity.profileLoggedInUserFragment.listenToImagesFromBucket()

//                                                        activity.addToBucketFragment.listenToBuckets()

                                                        checkIfBucketed(
                                                            activity.imageFullSizeFragment.collectButton,
                                                            image,
                                                            currentUser.uid
                                                        )

//this next function is to check if it's the first time the user has bucketed the image, otherwise it won't log it to analytics
                                                        refUserBuckets.addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError) {

                                                            }

                                                            override fun onDataChange(p0allBuckets: DataSnapshot) {

                                                                var existsInOtherBuckets = 0

                                                                for (bucket in p0allBuckets.children) {
                                                                    if (bucket.key != "All Buckets") {
                                                                        if (bucket.hasChild(image.id)) {
                                                                            existsInOtherBuckets++
                                                                            Log.d(
                                                                                "existanceCountReached",
                                                                                existsInOtherBuckets.toString()
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                if (existsInOtherBuckets <= 1) {
                                                                    firebaseAnalytics.logEvent(
                                                                        "image_bucketed",
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                        })
                                                    }
                                            }
                                    }

                                } else {
                                    actionText.text = "Add"
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

                        ref.child(image.id).setValue(true).addOnSuccessListener {

                            refImageBuckets.setValue(true)
                                .addOnSuccessListener {

                                    actionText.text = "Remove"
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
                                        "collection",
                                        activity
                                    )

                                    checkIfBucketed(
                                        activity.imageFullSizeFragment.collectButton,
                                        image,
                                        currentUser.uid
                                    )

                                }
                        }


                    } else {
                        actionText.text = "Add"
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
}

