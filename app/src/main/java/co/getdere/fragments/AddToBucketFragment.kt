package co.getdere.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.getString
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
import co.getdere.models.Buckets
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelImage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_to_collection_row.view.*
import kotlinx.android.synthetic.main.fragment_add_to_collection.*


class AddToBucketFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var imageObject: Images
    private lateinit var currentUser: Users

    lateinit var bucketsRecycler: RecyclerView
    val bucketsAdapter = GroupAdapter<ViewHolder>()

    private lateinit var newBucketInput: EditText
    var newBucket = Buckets()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_collection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
            sharedViewModelForImage.sharedImageObject.observe(this, Observer { images ->
                images?.let { image ->
                    imageObject = image
                }
            })
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        add_to_collection_title.text = getString(R.string.add_to_bucket)

        bucketsRecycler = add_to_collection_recycler
        bucketsRecycler.adapter = bucketsAdapter
        bucketsRecycler.layoutManager = LinearLayoutManager(this.context)

        listenToBuckets()

        newBucketInput = add_to_collection_new_input
        newBucketInput.hint = "Name your bucket"
        val bucketAddButton = add_to_collection_new_button

        bucketAddButton.setOnClickListener {

            if (newBucketInput.text.isNotEmpty()) {

                val bucketName = newBucketInput.text.toString().trimEnd()
                val allBuckets = "AllBuckets"

                val currentTime = System.currentTimeMillis()

                val refBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/")

                refBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {

                        var bucketNameExists = 0
                        for (bucket in p0.children) {

                            if (bucket.child("body/title").value == bucketName) {
                                bucketNameExists++
                            }
                        }

                        if (bucketNameExists > 0) {
                            Toast.makeText(context, "A bucket with the same name already exists", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            bucketsRecycler.visibility = View.VISIBLE

                            val bucketImages: MutableMap<String, Long> = mutableMapOf()
                            bucketImages[imageObject.id] = currentTime

                            val newKey = refBuckets.push().key

                            if (newKey != null) {
                                newBucket = Buckets(
                                    newKey,
                                    false,
                                    currentUser.uid,
                                    bucketName,
                                    "",
                                    bucketImages,
                                    System.currentTimeMillis()
                                )

                                refBuckets.child("$newKey/body").setValue(newBucket)
                                    .addOnSuccessListener {

                                        FirebaseDatabase.getInstance()
                                            .getReference("/images/${imageObject.id}/buckets/${currentUser.uid}")
                                            .setValue(currentTime).addOnSuccessListener {

                                                if (p0.hasChild(allBuckets)) {
                                                    refBuckets.child("$allBuckets/body/images${imageObject.id}")
                                                        .setValue(currentTime).addOnSuccessListener {
                                                            finishAddingToBucket()
                                                        }
                                                } else {
                                                    val allBucketsBucket = Buckets(
                                                        allBuckets,
                                                        false,
                                                        currentUser.uid,
                                                        "All Buckets",
                                                        "",
                                                        bucketImages,
                                                        System.currentTimeMillis()
                                                    )

                                                    refBuckets.child("$allBuckets/body")
                                                        .setValue(allBucketsBucket).addOnSuccessListener {
                                                            finishAddingToBucket()
                                                        }
                                                }
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


    private fun finishAddingToBucket() {
        val activity = activity as MainActivity

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
                newBucket,
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
            "purchasedItineraryObject",
            activity
        )

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        firebaseAnalytics.logEvent("image_bucketed", null)
    }


    private fun listenToBuckets() {

        bucketsAdapter.clear()

        FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild("buckets")) {
                        bucketsRecycler.visibility = View.VISIBLE

                        for (ds in p0.child("buckets").children) {
                            if (ds.key != "AllBuckets") {
                                val bucket = ds.child("body").getValue(Buckets::class.java)

                                if (bucket != null){
                                    bucketsAdapter.add(
                                        SingleBucketSuggestion(
                                            bucket,
                                            imageObject,
                                            currentUser,
                                            activity as MainActivity
                                        )
                                    )
                                }
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
    val bucket: Buckets,
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
        viewHolder.itemView.add_to_collection_name.setText(bucket.title)

        val actionText = viewHolder.itemView.add_to_collection_add

        executeBucket(0, bucket, image, currentUser, actionText, viewHolder.root.context, activity as MainActivity)

        actionText.setOnClickListener {
            executeBucket(1, bucket, image, currentUser, actionText, viewHolder.root.context, activity)
        }
    }


    private fun executeBucket(
        case: Int,
        bucket: Buckets,
        image: Images,
        currentUser: Users,
        actionText: TextView,
        context: Context,
        activity: MainActivity
    ) {

        val currentTime = System.currentTimeMillis()

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")
        val refImageBuckets =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets/${currentUser.uid}")


        refUserBuckets.child("${bucket.id}/body/images").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild(image.id)) {

                    if (case == 1) {

                        refUserBuckets.child("${bucket.id}/body/images/${image.id}").removeValue()
                            .addOnSuccessListener {

                                refUserBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0allBuckets: DataSnapshot) {
                                        var existsInOtherBuckets = 0

                                        for (bucketSnap in p0allBuckets.children) {
                                            if (bucketSnap.key != "AllBuckets") {
                                                if (bucketSnap.child("body/images").hasChild(image.id)) {
                                                    existsInOtherBuckets++
                                                }
                                            }
                                        }

                                        if (existsInOtherBuckets == 0) {

                                            refImageBuckets.removeValue().addOnSuccessListener {
                                                refUserBuckets.child("AllBuckets/body/images/${image.id}")
                                                    .removeValue().addOnSuccessListener {

                                                        changeReputation(
                                                            9,
                                                            image.id,
                                                            image.id,
                                                            currentUser.uid,
                                                            currentUser.name,
                                                            image.photographer,
                                                            TextView(context),
                                                            "purchasedItineraryObject",
                                                            activity
                                                        )

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
                                    }
                                })
                            }

                        actionText.text = context.getString(R.string.add)
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.green700
                            )
                        )

                        activity.profileLoggedInUserFragment.listenToImagesFromBucket()

                    } else {
                        actionText.text = context.getString(R.string.remove)
                        actionText.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.gray500
                            )
                        )
                    }

                } else {

                    if (case == 1) {

                        refUserBuckets.child("${bucket.id}/body/images/${image.id}").setValue(currentTime)
                            .addOnSuccessListener {

                                refImageBuckets.setValue(currentTime)
                                    .addOnSuccessListener {

                                        refUserBuckets.child("AllBuckets/body/images/${image.id}")
                                            .setValue(currentTime)
                                            .addOnSuccessListener {

                                                for (t in image.tags) {
                                                    val refUserTags = FirebaseDatabase.getInstance()
                                                        .getReference("users/${currentUser.uid}/interests/$t")
                                                    refUserTags.setValue(true)
                                                }

                                                actionText.text = context.getString(R.string.remove)
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
                                                    "purchasedItineraryObject",
                                                    activity
                                                )

                                                activity.profileLoggedInUserFragment.listenToImagesFromBucket()

                                                checkIfBucketed(
                                                    activity.imageFullSizeFragment.collectButton,
                                                    image,
                                                    currentUser.uid
                                                )

//this next function is to check if it's the first time the user has bucketed the image, otherwise it won't log it to analytics
                                                refUserBuckets.addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError) {}

                                                    override fun onDataChange(p0allBuckets: DataSnapshot) {

                                                        var existsInOtherBuckets = 0

                                                        for (bucketSnap in p0allBuckets.children) {
                                                            if (bucketSnap.key != "AllBuckets") {
                                                                if (bucketSnap.child("body/images").hasChild(image.id)) {
                                                                    existsInOtherBuckets++
                                                                }
                                                            }
                                                        }

                                                        if (existsInOtherBuckets == 1) {
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
                        actionText.text = context.getString(R.string.add)
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

