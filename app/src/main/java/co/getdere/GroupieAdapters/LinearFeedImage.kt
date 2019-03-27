package co.getdere.GroupieAdapters

import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import co.getdere.Fragments.FeedFragmentDirections
import androidx.navigation.fragment.findNavController
import co.getdere.Fragments.FeedFragment
import co.getdere.Interfaces.DereMethods

import co.getdere.Models.Images
import co.getdere.Models.SimpleString
import co.getdere.Models.Users
import co.getdere.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.linear_feed_post.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class LinearFeedImage(val image: Images, val initiatorName : String) : Item<ViewHolder>(), DereMethods {

    val uid = FirebaseAuth.getInstance().uid

    override fun getLayout(): Int {
        return R.layout.linear_feed_post
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val bucketCount = viewHolder.itemView.linear_feed_bucket_count
        val bucketButton = viewHolder.itemView.linear_feed_bucket

        val likeButton = viewHolder.itemView.linear_feed_like
        val likeCount = viewHolder.itemView.linear_feed_like_count

        val authorReputation = viewHolder.itemView.linear_feed_author_reputation


        val requestOption = RequestOptions()
            .placeholder(R.color.gray500).centerCrop()

        Glide.with(viewHolder.root.context).load(image.imageBig).transition(DrawableTransitionOptions.withCrossFade())
            .apply(requestOption).into(viewHolder.itemView.linear_feed_image)

        viewHolder.itemView.linear_feed_timestamp.text = PrettyTime().format(Date(image.timestamp))





        listenToBucketCount(bucketCount)
        checkIfBucketed(0, viewHolder, bucketButton, bucketCount)

        listenToLikeCount(likeCount, image)
        executeLike(image, uid!!, likeCount, likeButton, 0, initiatorName, image.photographer, authorReputation)



        val refAuthor = FirebaseDatabase.getInstance().getReference("/users/${image.photographer}/profile")

        refAuthor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)

                if (user != null) {

                    Glide.with(viewHolder.root.context).load(user.image)
                        .into(viewHolder.itemView.linear_feed__author_image)

                    viewHolder.itemView.linear_feed_author_name.text = user.name
                }
            }
        })






        likeButton.setOnClickListener {


            executeLike(image, uid, likeCount, likeButton, 1, initiatorName, image.photographer, authorReputation)

        }









        bucketButton.setOnClickListener {


            val refUserBucket = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets")

            refUserBucket.addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                    if (p0.hasChild(image.id)) {

                        Toast.makeText(viewHolder.root.context, "Remove from bucket action", Toast.LENGTH_SHORT)
                            .show()

                    } else {
                        if (uid == image.photographer) {
                            Toast.makeText(
                                viewHolder.root.context,
                                "You can't bucket your own photos",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {


                            val action =
                                FeedFragmentDirections.actionDestinationFeedToDestinationAddToBucket(image.id)

                            viewHolder.itemView.rootView.findNavController().navigate(action)
                        }

                    }
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
        }


    }


    private fun listenToBucketCount(bucketCount: TextView) {

        val refImage = FirebaseDatabase.getInstance().getReference("/images/${image.id}")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("buckets")) {

                    val refImageBucketingList =
                        FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets")

                    refImageBucketingList.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            var count = 0

                            for (ds in p0.children) {
                                count += 1
                                bucketCount.text = count.toString()
                            }
                        }

                    })


                } else {
                    bucketCount.text = ""
                }

            }

        })


    }


    private fun checkIfBucketed(ranNum: Int, viewHolder: ViewHolder, addToBucket: ImageButton, bucketCount: TextView) {

        val refUserBucket = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets")

        refUserBucket.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                if (p0.hasChild(image.id)) {
                    if (ranNum == 1) {

                        if (uid == image.photographer) {
                            Toast.makeText(
                                viewHolder.root.context,
                                "You can't bucket your own photos",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(viewHolder.root.context, "Remove from bucket action", Toast.LENGTH_SHORT)
                                .show()
                        }


                    } else {
                        addToBucket.setImageResource(R.drawable.bucket_saved)
                    }


                } else {
                    if (ranNum == 1) {

                        if (uid == image.photographer) {
                            Toast.makeText(
                                viewHolder.root.context,
                                "You can't bucket your own photos",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {


                            val action =
                                FeedFragmentDirections.actionDestinationFeedToDestinationAddToBucket(image.id)

                            viewHolder.root.findNavController().navigate(action)

                        }

                    } else {
                        addToBucket.setImageResource(R.drawable.bucket)

                    }

                }


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

    }


}
