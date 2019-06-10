package co.getdere.interfaces

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.fragments.SingleComment
import co.getdere.models.*
import co.getdere.otherClasses.FCMMethods
import co.getdere.otherClasses.FCMMethods.sendMessageTopic
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.*
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import mumayank.com.airlocationlibrary.AirLocation


interface DereMethods : FCMMethods {


    //following methods are for the forum_icon
/*
    post types:

    0: question
    1: answer
    2: image
    3: comment

    events:
    0: check
    1 : click
*/

    fun executeVote(
        vote: String,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        receiverId: String,
        postType: Int,
        votesView: TextView,
        upvoteView: ImageView,
        downvoteView: ImageView,
        specificPostId: String,
        event: Int,
        userReputationView: TextView,
        activity: Activity
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        if (event == 0) {
            setVotesCount(specificPostId, mainPostId, votesView, postType)
        } else {
            setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote, upvoteView, downvoteView)
        }


        val refVotes = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$mainPostId/main/votes")
        } else {
            FirebaseDatabase.getInstance()
                .getReference("/questions/$mainPostId/answers/$specificPostId/votes")
        }

        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(postVotesSnapshot: DataSnapshot) {

                if (postVotesSnapshot.hasChild(initiatorId)) {

                    when (postVotesSnapshot.child(initiatorId).getValue(Int::class.java)) {

                        1 -> {
                            if (event == 1) {

                                when (vote) {

                                    "up" -> return
                                    "down" -> {

                                        refVotes.child(initiatorId).removeValue().addOnSuccessListener {

                                            if (postType == 0) {
                                                changeReputation(
                                                    1,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("question_upvote_cancelled", null)

                                            } else {
                                                changeReputation(
                                                    3,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("answer_upvote_cancelled", null)
                                            }
                                        }
                                    }
                                }
                            } else {
                                upView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)
                            }
                        }

                        0 -> {
                            if (event == 1) {
                                when (vote) {
                                    "up" -> {
                                        refVotes.child(initiatorId).setValue(1).addOnSuccessListener {

                                            if (postType == 0) {
                                                changeReputation(
                                                    0,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("question_upvote", null)
                                            } else {
                                                changeReputation(
                                                    2,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("answer_upvote", null)
                                            }
                                        }
                                    }
                                    "down" -> {

                                        refVotes.child(initiatorId).setValue(-1).addOnSuccessListener {

                                            if (postType == 0) {
                                                changeReputation(
                                                    4,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("question_downvote", null)

                                            } else {
                                                changeReputation(
                                                    4,
                                                    specificPostId,
                                                    mainPostId,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "vote",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("question_downvote", null)
                                            }
                                        }
                                    }
                                }
                            } else {
                                defaultView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)
                            }
                        }

                        -1 -> {
                            if (event == 1) {
                                when (vote) {
                                    "up" -> {
                                        refVotes.child(initiatorId).removeValue().addOnSuccessListener {

                                            changeReputation(
                                                5,
                                                specificPostId,
                                                mainPostId,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "vote",
                                                activity
                                            )

                                            if (postType == 0) {
                                                firebaseAnalytics.logEvent("question_downvote_cancelled", null)
                                            } else {
                                                firebaseAnalytics.logEvent("answer_downvote_cancelled", null)
                                            }
                                        }
                                    }
                                    "down" -> return
                                }
                            } else {
                                downView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)
                            }
                        }
                    }

                } else {

                    if (event == 1) {
                        when (vote) {
                            "up" -> {
                                upView(upvoteView, downvoteView)

                                refVotes.child(initiatorId).setValue(1).addOnSuccessListener {

                                    if (postType == 0) {
                                        changeReputation(
                                            0,
                                            specificPostId,
                                            mainPostId,
                                            initiatorId,
                                            initiatorName,
                                            receiverId,
                                            userReputationView,
                                            "vote",
                                            activity
                                        )

                                        firebaseAnalytics.logEvent("question_upvote", null)
                                    } else {
                                        changeReputation(
                                            2,
                                            specificPostId,
                                            mainPostId,
                                            initiatorId,
                                            initiatorName,
                                            receiverId,
                                            userReputationView,
                                            "vote",
                                            activity
                                        )

                                        firebaseAnalytics.logEvent("answer_upvote", null)
                                    }
                                }
                            }
                            "down" -> {
                                downView(upvoteView, downvoteView)

                                refVotes.child(initiatorId).setValue(-1).addOnSuccessListener {

                                    if (postType == 0) {
                                        changeReputation(
                                            4,
                                            specificPostId,
                                            mainPostId,
                                            initiatorId,
                                            initiatorName,
                                            receiverId,
                                            userReputationView,
                                            "vote",
                                            activity
                                        )

                                        firebaseAnalytics.logEvent("question_downvote", null)
                                    } else {
                                        changeReputation(
                                            4,
                                            specificPostId,
                                            mainPostId,
                                            initiatorId,
                                            initiatorName,
                                            receiverId,
                                            userReputationView,
                                            "vote",
                                            activity
                                        )

                                        firebaseAnalytics.logEvent("answer_downvote", null)
                                    }
                                }
                            }
                        }
                    } else {
                        defaultView(upvoteView, downvoteView)
                        setVotesCount(specificPostId, mainPostId, votesView, postType)
                    }
                }
            }
        })
    }

    fun upView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_active)
        upvoteView.tag = "active"
        downvoteView.setImageResource(R.drawable.arrow_down_default)
        downvoteView.tag = "unactive"
    }

    fun defaultView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        upvoteView.tag = "unactive"
        downvoteView.setImageResource(R.drawable.arrow_down_default)
        downvoteView.tag = "unactive"
    }

    fun downView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        upvoteView.tag = "unactive"
        downvoteView.setImageResource(R.drawable.arrow_down_active)
        downvoteView.tag = "active"
    }

    fun setVotesCount(postId: String, questionId: String, votesView: TextView, postType: Int) {
        val refVotes = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/main")
        } else {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/$postId")
        }

        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("votes")) {
                    var count = 0

                    for (vote in p0.child("votes").children) {
                        val score = vote.value.toString().toInt()
                        count += score
                        votesView.text = numberCalculation(count.toLong())
                    }
                } else {
                    votesView.text = "0"
                }
            }
        })
    }

    fun setVotesCountSimplifiedAfterClickForFastResponse(
        votesView: TextView,
        vote: String,
        upvoteView: ImageView,
        downvoteView: ImageView
    ) {
        val voteCount = votesView.text.toString().toInt()
        var updatedCount = 0
        if (voteCount < 999) {
            if (vote == "up") {
                if (downvoteView.tag == "active") {
                    downvoteView.tag = "unactive"
                    upvoteView.tag = "unactive"
                    defaultView(upvoteView, downvoteView)
                    updatedCount = voteCount + 1
                    votesView.text = updatedCount.toString()
                } else if (downvoteView.tag == "unactive" && upvoteView.tag == "unactive") {
                    upvoteView.tag = "active"
                    updatedCount = voteCount + 1
                    votesView.text = updatedCount.toString()
                    upView(upvoteView, downvoteView)
                }
            } else {
                if (upvoteView.tag == "active") {
                    upvoteView.tag = "unactive"
                    downvoteView.tag = "unactive"
                    defaultView(upvoteView, downvoteView)
                    updatedCount = voteCount - 1
                    votesView.text = updatedCount.toString()
                } else if (downvoteView.tag == "unactive" && upvoteView.tag == "unactive") {
                    downvoteView.tag = "active"
                    downView(upvoteView, downvoteView)
                    updatedCount = voteCount - 1
                    votesView.text = updatedCount.toString()
                }
            }
        }
    }


    //following methods are for images

    fun listenToImageComments(
        image: Images,
        commentsRecyclerAdapter: GroupAdapter<ViewHolder>,
        commentsRecycler: RecyclerView,
        divider: View,
        currentUser: Users,
        activity: MainActivity
    ) {

        commentsRecyclerAdapter.clear()

        FirebaseDatabase.getInstance().getReference("/images/${image.id}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChild("comments")) {
                        commentsRecycler.visibility = View.VISIBLE
                        divider.visibility = View.VISIBLE

                        for (comment in p0.child("comments").children) {
                            val singleCommentFromDB = comment.child("body").getValue(Comments::class.java)

                            if (singleCommentFromDB != null) {
                                commentsRecyclerAdapter.add(
                                    SingleComment(
                                        singleCommentFromDB,
                                        image,
                                        currentUser,
                                        activity
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
    }

    fun listenToLikeCount(likeCount: TextView, image: Images) {

        FirebaseDatabase.getInstance().getReference("/images/${image.id}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChild("likes")) {

                        val refImageBucketingList =
                            FirebaseDatabase.getInstance().getReference("/images/${image.id}/likes")

                        refImageBucketingList.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                var count = 0

                                for (ds in p0.children) {
                                    count += 1
                                    likeCount.text = numberCalculation(count.toLong())
                                }
                            }
                        })

                    } else {
                        likeCount.text = "0"
                    }
                }
            })
    }

    fun likedView(likeButton: ImageButton, likeCount: TextView) {
        likeButton.setImageResource(R.drawable.heart_active)
        likeButton.tag = "liked"

        val currentLikeCount = if (likeCount.text.isNotEmpty()) {
            likeCount.text.toString().toInt()
        } else {
            0
        }

        if (currentLikeCount < 999) {
            val updatedLikeCount = currentLikeCount + 1
            likeCount.text = updatedLikeCount.toString()
        }
    }

    fun notLikedView(likeButton: ImageButton, likeCount: TextView) {
        likeButton.setImageResource(R.drawable.heart)
        likeButton.tag = "notLiked"

        val currentLikeCount = if (likeCount.text.isNotEmpty()) {
            likeCount.text.toString().toInt()
        } else {
            0
        }

        if (currentLikeCount < 999) {
            val updatedLikeCount = currentLikeCount - 1
            likeCount.text = updatedLikeCount.toString()
        }
    }

    fun executeLikeForFastResponse(likeButton: ImageButton, likeCount: TextView) {
        if (likeButton.tag == "liked") {
            notLikedView(likeButton, likeCount)
        } else {
            likedView(likeButton, likeCount)
        }
    }


    fun executeLike(
        image: Images,
        initiatorId: String,
        likeCount: TextView,
        likeButton: ImageButton,
        event: Int,
        initiatorName: String,
        receiverId: String,
        userReputationView: TextView,
        activity: Activity,
        source: String
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        if (event == 1) {
            executeLikeForFastResponse(likeButton, likeCount)
        }

        FirebaseDatabase.getInstance().getReference("/users/$initiatorId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild("likes")) {

                        if (p0.child("likes").hasChild(image.id)) {

                            if (event == 1) {

                                FirebaseDatabase.getInstance().getReference("/users/$initiatorId/likes/${image.id}")
                                    .removeValue().addOnSuccessListener {

                                        FirebaseDatabase.getInstance()
                                            .getReference("/images/${image.id}/likes/$initiatorId").removeValue()
                                            .addOnSuccessListener {

                                                changeReputation(
                                                    15,
                                                    image.id,
                                                    image.id,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "like",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("image_unliked", null)
                                            }
                                    }
                            } else {
                                likeButton.setImageResource(R.drawable.heart_active)
                                likeButton.tag = "liked"
                            }

                        } else {

                            if (event == 1) {

                                FirebaseDatabase.getInstance()
                                    .getReference("/users/$initiatorId/likes/${image.id}").setValue(true)
                                    .addOnSuccessListener {

                                        FirebaseDatabase.getInstance()
                                            .getReference("/images/${image.id}/likes/$initiatorId").setValue(true)
                                            .addOnSuccessListener {

                                                changeReputation(
                                                    14,
                                                    image.id,
                                                    image.id,
                                                    initiatorId,
                                                    initiatorName,
                                                    receiverId,
                                                    userReputationView,
                                                    "like",
                                                    activity
                                                )

                                                firebaseAnalytics.logEvent("image_liked", null)
                                            }
                                    }

                            } else {
                                if (source == "staggered") {
                                    likeButton.setImageResource(R.drawable.heart_white)
                                } else {
                                    likeButton.setImageResource(R.drawable.heart)
                                }
                                likeButton.tag = "notLiked"
                            }
                        }
                    } else {
                        if (event == 1) {

                            FirebaseDatabase.getInstance()
                                .getReference("/users/$initiatorId/likes/${image.id}").setValue(true)
                                .addOnSuccessListener {

                                    FirebaseDatabase.getInstance()
                                        .getReference("/images/${image.id}/likes/$initiatorId").setValue(true)
                                        .addOnSuccessListener {

                                            changeReputation(
                                                14,
                                                image.id,
                                                image.id,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "like",
                                                activity
                                            )

                                            firebaseAnalytics.logEvent("image_liked", null)
                                        }

                                }

                        } else {
                            if (source == "staggered") {
                                likeButton.setImageResource(R.drawable.heart_white)
                            } else {
                                likeButton.setImageResource(R.drawable.heart)
                            }
                            likeButton.tag = "notLiked"
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }


    fun executeCommentLike(
        image: Images,
        initiatorId: String,
        likeCount: TextView,
        likeButton: ImageButton,
        event: Int,
        initiatorName: String,
        receiverId: String,
        userReputationView: TextView,
        activity: Activity,
        comment: Comments
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        if (event == 1) {
            executeLikeForFastResponse(likeButton, likeCount)
        }

        val commentRef = FirebaseDatabase.getInstance().getReference("/images/${image.id}/comments/${comment.id}")

        commentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("likes")) {
                    if (p0.child("likes").hasChild(initiatorId)) {

                        if (event == 1) {
                            commentRef.child("likes/$initiatorId").removeValue().addOnSuccessListener {

                                changeReputation(
                                    13,
                                    image.id,
                                    image.id,
                                    initiatorId,
                                    initiatorName,
                                    receiverId,
                                    userReputationView,
                                    "photoCommentLike",
                                    activity
                                )

                                firebaseAnalytics.logEvent("image_comment_unliked", null)
                            }

                        } else {
                            likeButton.setImageResource(R.drawable.heart_active)
                            likeButton.tag = "liked"
                        }

                    } else {
                        if (event == 1) {
                            commentRef.child("likes/$initiatorId").setValue(true).addOnSuccessListener {

                                changeReputation(
                                    12,
                                    image.id,
                                    image.id,
                                    initiatorId,
                                    initiatorName,
                                    receiverId,
                                    userReputationView,
                                    "photoCommentLike",
                                    activity
                                )

                                firebaseAnalytics.logEvent("image_comment_liked", null)
                            }
                        } else {
                            likeButton.setImageResource(R.drawable.heart)
                            likeButton.tag = "notLiked"
                        }
                    }
                } else {
                    if (event == 1) {
                        commentRef.child("likes/$initiatorId").setValue(true).addOnSuccessListener {

                            changeReputation(
                                12,
                                image.id,
                                image.id,
                                initiatorId,
                                initiatorName,
                                receiverId,
                                userReputationView,
                                "photoCommentLike",
                                activity
                            )

                            firebaseAnalytics.logEvent("image_comment_liked", null)
                        }
                    } else {
                        likeButton.setImageResource(R.drawable.heart)
                        likeButton.tag = "notLiked"
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    fun listenToCommentCount(commentCount: TextView, image: Images) {

        val refImage = FirebaseDatabase.getInstance().getReference("/images/${image.id}")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                commentCount.text = if (p0.hasChild("comments")) {
                    numberCalculation(p0.child("comments").childrenCount)
                } else {
                    "0"
                }
            }
        })
    }


    fun listenToBucketCount(bucketCount: TextView, image: Images) {

        FirebaseDatabase.getInstance().getReference("/images/${image.id}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.hasChild("buckets")) {

                        FirebaseDatabase.getInstance().getReference("/images/${image.id}/buckets")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {
                                    var count = 0

                                    for (ds in p0.children) {
                                        count += 1
                                        bucketCount.text = numberCalculation(count.toLong())
                                    }
                                }
                            })
                    } else {
                        bucketCount.text = "0"
                    }
                }
            })
    }


    fun checkIfBucketed(bucketButton: ImageView, image: Images, uid: String) {

        FirebaseDatabase.getInstance().getReference("/users/$uid/buckets/AllBuckets/body/images")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild(image.id)) {
                        bucketButton.setImageResource(R.drawable.bucket_saved)
                    } else {
                        bucketButton.setImageResource(R.drawable.bucket)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    fun checkIfInItinerary(collectButton: ImageView, image: Images, uid: String) {
        collectButton.setImageResource(R.drawable.itinerary)

        FirebaseDatabase.getInstance().getReference("/users/$uid/itineraries")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (itinerary in p0.children) {
                        FirebaseDatabase.getInstance().getReference("/itineraries/${itinerary.key}/body/images")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.hasChild(image.id)) {
                                        collectButton.setImageResource(R.drawable.itinerary_saved)
                                    }
                                }
                            })
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }


//following methods are general for the reputation and notifications

    fun sendNotification(
        postType: Int,
        scenarioType: Int,
        initiatorId: String,
        initiatorName: String,
        mainPostId: String,
        specificPostId: String,
        receiverId: String
    ) {

        val refUserGalleryNotifications =
            FirebaseDatabase.getInstance()
                .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId$initiatorId$scenarioType")

        val refUserBoardNotifications =
            FirebaseDatabase.getInstance()
                .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId$initiatorId$scenarioType")

        val initiatorRef = FirebaseDatabase.getInstance().getReference("/users/$initiatorId/profile/image")

        if (postType == 0 || postType == 1 && initiatorId != receiverId) {

            initiatorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val userImage = p0.getValue(String::class.java)

                    if (userImage != null) {
                        refUserBoardNotifications.setValue(
                            NotificationBoard(
                                postType,
                                scenarioType,
                                initiatorId,
                                initiatorName,
                                userImage,
                                mainPostId,
                                specificPostId,
                                System.currentTimeMillis(),
                                0
                            )
                        )
                    }
                }
            })


        } else if (postType == 2 || postType == 3 && initiatorId != receiverId) {

            FirebaseDatabase.getInstance().getReference("/images/$mainPostId/body/imageSmall")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        val mainImage = p0.getValue(String::class.java)

                        if (mainImage != null) {
                            initiatorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {

                                    val userImage = p0.getValue(String::class.java)

                                    if (userImage != null) {
                                        refUserGalleryNotifications.setValue(
                                            NotificationFeed(
                                                postType,
                                                scenarioType,
                                                initiatorId,
                                                initiatorName,
                                                userImage,
                                                mainPostId,
                                                mainImage,
                                                specificPostId,
                                                System.currentTimeMillis(),
                                                0
                                            )
                                        )
                                    }
                                }
                            })
                        }
                    }
                })
        } else {
            initiatorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    val userImage = p0.getValue(String::class.java)
                    if (userImage != null) {
                        refUserGalleryNotifications.setValue(
                            NotificationFeed(
                                postType,
                                scenarioType,
                                initiatorId,
                                initiatorName,
                                userImage!!,
                                mainPostId,
                                "",
                                specificPostId,
                                System.currentTimeMillis(),
                                0
                            )
                        )
                    }
                }
            })
        }
    }

    fun changeReputation(
        scenarioType: Int,
        specificPostId: String,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        receiverId: String,
        userReputationView: TextView,
        action: String,
        activity: Activity
    ) {

        val refReceiverReputation =
            FirebaseDatabase.getInstance()
                .getReference("/users/$receiverId/reputation/$specificPostId$initiatorId$action")
        val refInitiatorReputation =
            FirebaseDatabase.getInstance().getReference("/users/$initiatorId/reputation/$specificPostId$action")

        when (scenarioType) {


            //   0 : question upvote +5 to receiver +notification // type 0
            0 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(0, 0, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                }
            }


            //    1 : question upvote is removed -5 to receiver
            1 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}0")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //  2 : answer upvoted +10 to receiver +notification  // type 1
            2 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 10)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(1, 2, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "upvoted your answer",
                        initiatorName
                    )
                }
            }

            // 3 : answer upvote is removed -10 to receiver
            3 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}2")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //  4 : question/answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
            4 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -2)
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, -1)

                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    refInitiatorReputation.setValue(valueForInitiator).addOnSuccessListener {
                        updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        sendNotification(0, 4, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    }
                }
            }


            // 5 : question/answer downvote is removed +2 for receiver +1 for initiator
            5 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}4")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            refInitiatorReputation.removeValue().addOnSuccessListener {
                                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                            }
                        }
                    }
            }


            //6 : answer given +2 to initiator
            6 -> {
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, 2)
                refInitiatorReputation.setValue(valueForInitiator).addOnSuccessListener {
                    updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
                    sendNotification(1, 6, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "answered your question",
                        initiatorName
                    )
                }
            }


            //7 : answer removed -2 to initiator            ***not implemented yet***
            7 -> {
                refInitiatorReputation.removeValue().addOnSuccessListener {
                    FirebaseDatabase.getInstance()
                        .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}6")
                        .removeValue().addOnSuccessListener {
                            updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
                        }
                }
            }


            // 8 : photo bucketed +15 to receiver +notification  // type 2
            8 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 15)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(2, 8, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "bucketed your photo",
                        initiatorName
                    )
                }
            }


            //9 : photo unbucketed -15 to receiver
            9 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}8")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //10 : question saved +5 to receiver +notification  // type 0
            10 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(0, 10, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                }
            }


            //11 : question unsaved - 5 to receiver
            11 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}10")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //12 : comment receives a like +1 to receiver +notification  // type 3
            12 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 1)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(3, 12, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "liked your comment", initiatorName)
                }
            }


            //13 : comment like removed -1 to receiver
            13 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}12")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //14 : photo receives a like +2 to receiver +notification  // type 2
            14 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 2)
                refReceiverReputation.setValue(valueForReceiver).addOnSuccessListener {
                    updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                    sendNotification(2, 14, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                    sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "liked your photo", initiatorName)
                }
            }

            //15 : photo like removed -2 to receiver
            15 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}14")
                    .removeValue().addOnSuccessListener {
                        refReceiverReputation.removeValue().addOnSuccessListener {
                            updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                        }
                    }
            }


            //16 : photo receives a comment
            16 -> {
                sendNotification(2, 16, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(
                    receiverId,
                    initiatorId,
                    mainPostId,
                    activity,
                    "commented on your photo",
                    initiatorName
                )
            }


            //17 : photo comment removed
            17 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}16")
                    .removeValue()
            }


            //18 : answer receives a comment
            18 -> {
                sendNotification(1, 18, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(
                    receiverId,
                    initiatorId,
                    mainPostId,
                    activity,
                    "commented on your answer",
                    initiatorName
                )
            }


            //19: comment on answer removed // not implemented yet
            19 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}18")
                    .removeValue()
            }


            //20 : profile receives a follow
            20 -> {
                sendNotification(4, 20, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "started following you", initiatorName)
            }


            //21 : profile unfollowed
            21 -> {
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}20")
                    .removeValue()
            }
        }
    }


    fun updateUserFinalReputation(id: String, ref: DatabaseReference, userReputationView: TextView) {

        FirebaseDatabase.getInstance().getReference("/users/$id/reputation")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    var count = 0

                    for (ds in p0.children) {
                        val score = ds.getValue(ReputationScore::class.java)
                        count += score!!.points
                    }

                    val userObject = FirebaseDatabase.getInstance().getReference("/users/$id/profile")

                    if (count < 0) {
                        userObject.updateChildren(mapOf("reputation" to "0"))
                        userReputationView.text = "(0)"
                    } else {
                        userObject.updateChildren(mapOf("reputation" to count.toLong()))
                        userReputationView.text = "(${numberCalculation(count.toLong())})"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        var gpsEnabled = false

        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            //do nothing...
        }
        return gpsEnabled
    }

    fun closeKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    fun showKeyboard(activity: Activity) {

        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    fun numberCalculation(number: Long): String {
        if (number < 1000)
            return "" + number
        val exp = (Math.log(number.toDouble()) / Math.log(1000.0)).toInt()
        return String.format("%.1f %c", number / Math.pow(1000.0, exp.toDouble()), "kMBTPE"[exp - 1])
    }


    fun panToCurrentLocation(activity: Activity, myMapboxMap: MapboxMap) {
        var airLocation: AirLocation? = null
        airLocation = AirLocation(activity, true, true, object : AirLocation.Callbacks {
            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {}

            override fun onSuccess(location: Location) {

                val position = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(10.0)
                    .build()

                myMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000)
            }
        })
    }
}

/*

reputation scenarios:

0 : question upvote +5 to receiver +notification // type 0
1 : question upvote is removed -5 to receiver
2 : answer upvoted +10 to receiver +notification  // type 1
3 : answer upvote is removed -10 to receiver
4 : question/answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
5 : question/answer downvote is removed +2 for receiver +1 for initiator
6 : answer given +2 to initiator
7 : answer removed -2 to initiator            ***not implemented yet***
8 : photo bucketed +15 to receiver +notification  // type 2
9 : photo unbucketed -15 to receiver
10 : question saved +5 to receiver +notification  // type 0
11 : question unsaved - 5 to receiver
12 : photo comment receives a like +1 to receiver +notification  // type 3
13 : photo comment like removed -1 to receiver
14 : photo receives a like +2 to receiver +notification  // type 2
15 : photo like removed -2 to receiver
16 : photo receives a comment
17 : photo comment removed
18 : answer receives a comment
19: comment on answer removed // not implemented yet
20 : profile receives a follow
21 : profile unfollowed




post types:

0: question
1: answer
2: image
3: comment



*/
