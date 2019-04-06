package co.getdere.fragments


import android.content.Context
import android.os.Bundle
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


class AddToBucketFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelForImage: SharedViewModelImage

    lateinit var imageObject : Images
    private lateinit var currentUser: Users

    val bucketsAdapter = GroupAdapter<ViewHolder>()
    lateinit var bucketsLayoutManager : LinearLayoutManager


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)

            sharedViewModelForImage.sharedImageObject.observe(this, Observer {
                it?.let { image ->
                    imageObject = image
                    listenToBuckets()
                }})


            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

        }


//        arguments?.let {
//            val safeArgs = ImageFullSizeFragmentArgs.fromBundle(it)
//
////            val imageId = safeArgs.imageId
//
//            activityName = safeArgs.activityName
//
//
//
//        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_to_bucket, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        arguments?.let {
//            val safeArgs = AddToBucketFragmentArgs.fromBundle(it)
//            image = safeArgs.image
//            currentUser = safeArgs.currentUser
//        }


        val recycler = view.findViewById<RecyclerView>(R.id.add_to_bucket_recycler)
        val newBucketInput = view.findViewById<EditText>(R.id.add_to_bucket_new_input)
        val bucketAddButton = view.findViewById<TextView>(R.id.add_to_bucket_new_button)

        recycler.adapter = bucketsAdapter
        bucketsLayoutManager = LinearLayoutManager(this.context)
        recycler.layoutManager = bucketsLayoutManager



        bucketAddButton.setOnClickListener {

            if (newBucketInput.text.isNotEmpty()) {

                val refBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/")

                refBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.hasChild(newBucketInput.text.toString())) {
                            Toast.makeText(context, "A bucket with the same name already exists", Toast.LENGTH_LONG)
                                .show()

                        } else {

                            val ref = FirebaseDatabase.getInstance()
                                .getReference("/users/${currentUser.uid}/buckets/${newBucketInput.text}")

                            ref.setValue(mapOf(imageObject.id to true)).addOnSuccessListener {


                                val imageBucketingRef =
                                    FirebaseDatabase.getInstance()
                                        .getReference("/images/${imageObject.id}/buckets")

                                imageBucketingRef.setValue(mapOf(currentUser.uid to true)).addOnSuccessListener {

                                    newBucketInput.text.clear()

                                    listenToBuckets()

                                    changeReputation(
                                        8,
                                        imageObject.id,
                                        imageObject.id,
                                        currentUser.uid,
                                        currentUser.name,
                                        imageObject.photographer,
                                        TextView(context),
                                        "bucket"
                                    )

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


    private fun listenToBuckets() {

        bucketsAdapter.clear()

        val allBucketsRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")

        allBucketsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                for (ds in p0.children){

                    bucketsAdapter.add(SingleBucketSuggestion(ds.key!!, imageObject, currentUser))

                }

            }

        })


    }

}


class SingleBucketSuggestion(val bucketName: String, val image: Images, val currentUser: Users) : Item<ViewHolder>(),
    DereMethods {


    val uid = FirebaseAuth.getInstance().uid


    override fun getLayout(): Int {
        return R.layout.add_to_bucket_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.add_to_bucket_name.text = bucketName

        val actionText = viewHolder.itemView.add_to_bucket_add



        executeBucket(0, bucketName, image, currentUser, actionText, viewHolder.root.context)

        actionText.setOnClickListener {

            executeBucket(1, bucketName, image, currentUser, actionText, viewHolder.root.context)

        }


    }


    private fun executeBucket(
        case: Int,
        bucketName: String,
        image: Images,
        currentUser: Users,
        actionText: TextView,
        context: Context
    ) {


        val refUser = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")
        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")
        val refBucket = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets/$bucketName")
        val refImageBuckets =
            FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets")


        refUserBuckets.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(bucketName)) {

                    refBucket.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            if (p0.hasChild(image.id)) {

                                if (case == 1) {


                                    changeReputation(
                                        9,
                                        image.id,
                                        image.id,
                                        currentUser.uid,
                                        currentUser.name,
                                        image.photographer,
                                        TextView(context),
                                        "bucket"
                                    )


                                    refBucket.child(image.id).removeValue()
                                    refImageBuckets.child(currentUser.uid).removeValue().addOnSuccessListener {
                                        addReputationIfExistsInAnyBucket(context)
                                    }



                                    actionText.text = "ADD"
                                    actionText.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.green700
                                        )
                                    )


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
                                                    "bucket"
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
                                        "bucket"
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




    private fun addReputationIfExistsInAnyBucket(context : Context){

        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")

        refUserBuckets.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                if (p0.hasChild(image.id)){

                    changeReputation(
                        8,
                        image.id,
                        image.id,
                        currentUser.uid,
                        currentUser.name,
                        image.photographer,
                        TextView(context),
                        "bucket"
                    )

                    val refImageBuckets =
                        FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets")

                    refImageBuckets.setValue(mapOf(currentUser.uid to true))

                }

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }


        })

    }


}


//                        val refUserBuckets = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/buckets")
//
//                        refUserBuckets.addChildEventListener(object : ChildEventListener{
//
//                            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//
//                                var count = 0
//
//                                for (ds in p0.children){
//
//                                    if (ds.hasChild(image.id)){
//                                        count +=1
//                                    }
//                                }
//
//                                println(count)
//
//                                if (count > 1) {
//
//                                    refBucket.child(image.id).removeValue()
//                                    refImageBuckets.removeValue()
//
//
//
//                                } else {
//
//                                    changeReputation(
//                                        9,
//                                        image.id,
//                                        image.id,
//                                        currentUser.uid,
//                                        currentUser.name,
//                                        image.photographer,
//                                        TextView(context),
//                                        "bucket"
//                                    )
//
//                                    refBucket.child(image.id).removeValue()
//                                    refImageBuckets.removeValue()
//                                }
//
//
//                            }
//
//                            override fun onCancelled(p0: DatabaseError) {
//                            }
//
//                            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//                            }
//
//                            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                            }
//
//                            override fun onChildRemoved(p0: DataSnapshot) {
//                            }
//
//                        })
