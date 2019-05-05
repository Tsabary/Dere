package co.getdere.interfaces

import android.app.Activity
import android.content.Context
import android.location.Location
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import co.getdere.R
import co.getdere.models.Images
import co.getdere.models.Notification
import co.getdere.models.ReputationScore
import co.getdere.otherClasses.FCMMethods
import co.getdere.otherClasses.FCMMethods.sendMessageTopic
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
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
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(postVotesSnapshot: DataSnapshot) {

                if (postVotesSnapshot.hasChild(initiatorId)) {

                    when (postVotesSnapshot.child(initiatorId).getValue(Int::class.java)) {

                        1 -> {
                            if (event == 1) {

                                when (vote) {

                                    "up" -> return
                                    "down" -> {
//                                        defaultView(upvoteView, downvoteView)

                                        refVotes.child(initiatorId).removeValue().addOnSuccessListener {
                                            //                                            setVotesCount(specificPostId, mainPostId, votesView, postType)


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
//                                        upView(upvoteView, downvoteView)
                                        refVotes.child(initiatorId).setValue(1).addOnSuccessListener {
                                            //                                            setVotesCount(specificPostId, mainPostId, votesView, postType)

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
                                            }
                                        }
                                    }
                                    "down" -> {
//                                        downView(upvoteView, downvoteView)

                                        refVotes.child(initiatorId).setValue(-1).addOnSuccessListener {
                                            //                                            setVotesCount(specificPostId, mainPostId, votesView, postType)

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
//                                        defaultView(upvoteView, downvoteView)

                                        refVotes.child(initiatorId).removeValue().addOnSuccessListener {
                                            //                                            setVotesCount(specificPostId, mainPostId, votesView, postType)

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
//                                setVotesCount(specificPostId, mainPostId, votesView, postType)

                                refVotes.child(initiatorId).setValue(1).addOnSuccessListener {
                                    //                                    setVotesCount(specificPostId, mainPostId, votesView, postType)

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
                                    }
                                }
                            }
                            "down" -> {
                                downView(upvoteView, downvoteView)
//                                setVotesCount(specificPostId, mainPostId, votesView, postType)

                                refVotes.child(initiatorId).setValue(-1).addOnSuccessListener {
                                    //                                    setVotesCount(specificPostId, mainPostId, votesView, postType)

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


    fun listenToLikeCount(likeCount: TextView, image: Images) {

        val refImage = FirebaseDatabase.getInstance().getReference("/images/${image.id}")

        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.hasChild("likes")) {

                    val refImageBucketingList =
                        FirebaseDatabase.getInstance().getReference("/images/${image.id}/likes")

                    refImageBucketingList.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

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
        activity: Activity
    ) {

        if (event == 1) {
            executeLikeForFastResponse(likeButton, likeCount)
        }

        val allUserRef = FirebaseDatabase.getInstance().getReference("/users/$initiatorId")

        allUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("likes")) {

                    if (p0.child("likes").hasChild(image.id)) {

                        val allUserLikesRef =
                            FirebaseDatabase.getInstance().getReference("/users/$initiatorId/likes/${image.id}")

                        if (event == 1) {

                            allUserLikesRef.removeValue().addOnSuccessListener {

                                val refImageLikes =
                                    FirebaseDatabase.getInstance()
                                        .getReference("/images/${image.id}/likes/$initiatorId")

                                refImageLikes.removeValue().addOnSuccessListener {
                                    //                                                likeButton.setImageResource(R.drawable.heart)

//                                    listenToLikeCount(likeCount, image)

                                    changeReputation(
                                        15,
                                        image.id,
                                        image.id,
                                        initiatorId,
                                        initiatorName,
                                        receiverId,
                                        userReputationView,
                                        "like", activity
                                    )

                                }
                            }

                        } else {
                            likeButton.setImageResource(R.drawable.heart_active)
                            likeButton.tag = "liked"
                        }

                    } else {

                        if (event == 1) {

                            val refUserLikes =
                                FirebaseDatabase.getInstance()
                                    .getReference("/users/$initiatorId/likes/${image.id}")

                            refUserLikes.setValue(true).addOnSuccessListener {

                                val refImageLikes =
                                    FirebaseDatabase.getInstance()
                                        .getReference("/images/${image.id}/likes/$initiatorId")

                                refImageLikes.setValue(true).addOnSuccessListener {

                                    //                                                likeButton.setImageResource(R.drawable.heart_active)

//                                    listenToLikeCount(likeCount, image)

                                    changeReputation(
                                        14,
                                        image.id,
                                        image.id,
                                        initiatorId,
                                        initiatorName,
                                        receiverId,
                                        userReputationView,
                                        "like", activity
                                    )

                                }

                            }

                        } else {
                            likeButton.setImageResource(R.drawable.heart)
                            likeButton.tag = "notLiked"
                        }
                    }
                } else {
                    if (event == 1) {

                        val refUserLikes =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/$initiatorId/likes/${image.id}")

                        refUserLikes.setValue(true).addOnSuccessListener {

                            val refImageLikes =
                                FirebaseDatabase.getInstance()
                                    .getReference("/images/${image.id}/likes/$initiatorId")

                            refImageLikes.setValue(true).addOnSuccessListener {

                                //                                likeButton.setImageResource(R.drawable.heart_active)
//                                likeButton.tag = "liked"

//                                    likeButton.setImageResource(R.drawable.heart_active)

//                                listenToLikeCount(likeCount, image)

                                changeReputation(
                                    14,
                                    image.id,
                                    image.id,
                                    initiatorId,
                                    initiatorName,
                                    receiverId,
                                    userReputationView,
                                    "like", activity
                                )
                            }

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
            override fun onCancelled(p0: DatabaseError) {

            }

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
        Log.d("Image Bucketed", "function called")


        val refUserBucket = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets")

        refUserBucket.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                if (p0.hasChild(image.id)) {
                    bucketButton.setImageResource(R.drawable.bucket_saved)
                } else {
                    bucketButton.setImageResource(R.drawable.bucket)

                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }

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

        if (postType == 0 || postType == 1 && initiatorId != receiverId) {


            val refUserBoardNotifications =
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId$initiatorId$scenarioType")
            refUserBoardNotifications.setValue(
                Notification(
                    postType,
                    scenarioType,
                    initiatorId,
                    initiatorName,
                    mainPostId,
                    specificPostId,
                    System.currentTimeMillis(),
                    0
                )
            )

        } else if (initiatorId != receiverId) {

            val refUserGalleryNotifications =
                FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId$initiatorId$scenarioType")

            refUserGalleryNotifications.setValue(
                Notification(
                    postType,
                    scenarioType,
                    initiatorId,
                    initiatorName,
                    mainPostId,
                    specificPostId,
                    System.currentTimeMillis(),
                    0
                )
            )

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
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(0, 0, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
            }


            //    1 : question upvote is removed -5 to receiver
            1 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}0")

                refReceiverReputation.removeValue()
                notificationRef.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //  2 : answer upvoted +10 to receiver +notification  // type 1
            2 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 10)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(1, 2, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "upvoted your answer", initiatorName)
                Log.d("check if execute", "yes it does")
            }

            // 3 : answer upvote is removed -10 to receiver
            3 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}2")

                refReceiverReputation.removeValue()
                notificationRef.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //  4 : question/answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
            4 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -2)
                refReceiverReputation.setValue(valueForReceiver)
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, -1)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(0, 4, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
            }


            // 5 : question/answer downvote is removed +2 for receiver +1 for initiator
            5 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}4")
                notificationRef.removeValue()
                refReceiverReputation.removeValue()
                refInitiatorReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //6 : answer given +2 to initiator
            6 -> {
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, 2)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)

                sendNotification(1, 6, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "answered your question", initiatorName)
                Log.d("check if execute", "yes it does")
            }


            //7 : answer removed -2 to initiator            ***not implemented yet***
            7 -> {
                refInitiatorReputation.removeValue()

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}6")
                notificationRef.removeValue()

                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
            }


            // 8 : photo bucketed +15 to receiver +notification  // type 2
            8 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 15)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(2, 8, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "bucketed your photo", initiatorName)
                Log.d("check if execute", "yes it does")
            }


            //9 : photo unbucketed -15 to receiver
            9 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}8")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //10 : question saved +5 to receiver +notification  // type 0
            10 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(0, 10, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
            }


            //11 : question unsaved - 5 to receiver
            11 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}10")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //12 : comment receives a like +1 to receiver +notification  // type 3
            12 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 1)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(3, 12, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "liked your comment", initiatorName)
                Log.d("check if execute", "yes it does")
            }


            //13 : comment like removed -1 to receiver
            13 -> {

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}12")
                notificationRef.removeValue()


                refReceiverReputation.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }


            //14 : photo receives a like +2 to receiver +notification  // type 2
            14 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 2)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(2, 14, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "liked your photo", initiatorName)
                Log.d("check if execute", "yes it does")
            }

            //15 : photo like removed -2 to receiver
            15 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}14")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
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
                Log.d("check if execute", "yes it does")
            }


            //17 : photo comment removed
            17 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}16")
                notificationRef.removeValue()
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
                Log.d("check if execute", "yes it does")
            }


            //19: comment on answer removed // not implemented yet
            19 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}18")
                notificationRef.removeValue()
            }


            //20 : profile receives a follow
            20 -> {
                sendNotification(4, 20, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

                sendMessageTopic(receiverId, initiatorId, mainPostId, activity, "started following you", initiatorName)
                Log.d("check if execute", "yes it does")
            }


            //21 : profile unfollowed
            21 -> {
                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}20")

                notificationRef.removeValue()
            }


        }
    }


    fun updateUserFinalReputation(id: String, ref: DatabaseReference, userReputationView: TextView) {

        val refTest = FirebaseDatabase.getInstance().getReference("/users/$id/reputation")

        refTest.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var count = 0

                for (ds in p0.children) {
                    val score = ds.getValue(ReputationScore::class.java)
                    count += score!!.points
                    Log.d("checkIfCountWorks", count.toString())
                }

                val userObject = FirebaseDatabase.getInstance().getReference("/users/$id/profile")

                if (count < 0) {
                    userObject.updateChildren(mapOf("reputation" to "0"))
                    userReputationView.text = "(0)"
                    Log.d("checkIfCountWorks", count.toString())
                } else {
                    userObject.updateChildren(mapOf("reputation" to count.toLong()))
                    userReputationView.text = "(${numberCalculation(count.toLong())})"
                    Log.d("checkIfCountWorks", count.toString())
                }
            }


            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }


//    fun sendMessageTopic(receiverId: String, initiatorId : String, post : String, activity : Activity) {
//
//        val notificationTitle = "This is the title that would appear"
//        val notificationMessage = "This is the message that would appear"
//
//        val notification = JSONObject()
//        val notificationBody = JSONObject()
//        try {
//            notificationBody.put("title", notificationTitle)
//            notificationBody.put("message", notificationMessage)
//            notificationBody.put("initiator", initiatorId)
//            notificationBody.put("post", post)
//
//            notification.put("to", receiverId)
//            notification.put("data", notificationBody)
//        } catch (e: JSONException) {
//            Log.e("NOTIFICATION TAG", "onCreate: $e")
//        }
//        sendTopicNotification(notification, activity)
//    }
//
//    fun sendTopicNotification(notification: JSONObject, activity : Activity) {
//
//        val FCM_API = "https://fcm.googleapis.com/fcm/send"
//        val serverKey =
//            "AAAAA6gibkM:APA91bHpYv3QKAF4_wfCpRB4dTT11pRr_uZUcLUpgv6OEO-acl7qvnSK0vFyA8iXloJfuQIBS3q61zgGRZ6iFgBrHMpIflEyeTNgD_LJbQZtMnPHlVdRoe-OK8dNUGss7YfR9Hf-4f7L"
//
//        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
//            Response.Listener<JSONObject> { response -> Log.i("NOTIFICATION TAG", "onResponse: $response") },
//            Response.ErrorListener {
//                Toast.makeText(activity, "Request error $it", Toast.LENGTH_LONG).show()
//                Log.i("NOTIFICATION TAG", "onErrorResponse: Didn't work because $it")
//            }) {
//
//            @Throws(AuthFailureError::class)
//            override fun getHeaders(): Map<String, String> {
//                val params = HashMap<String, String>()
//                params["Authorization"] = serverKey
//                params["Content-Type"] = "application/json"
//                return params
//            }
//        }
//        MySingleton.getInstance(activity).addToRequestQueue(jsonObjectRequest)
//    }


    fun sendCloudMessage(userId: String) {

        val receiverRef = FirebaseDatabase.getInstance().getReference("/users/$userId/services/firebase-token")

        receiverRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val registrationToken = p0.getValue(String::class.java)

                val senderId = "15705730627"
                val msgID = registrationToken + System.currentTimeMillis()

                val fm = FirebaseMessaging.getInstance()

                fm.send(
                    RemoteMessage.Builder("$senderId@gcm.googleapis.com")
                        .setMessageId(msgID)
                        .addData("my_message", "Hello World")
                        .addData("my_action", "SAY_HELLO")
                        .build()
                )

                Log.d("tokencomplete", p0.toString())
                Log.d("tokenonly", registrationToken)

            }

        })

    }


// See documentation on defining a message payload.

// Send a message to the device corresponding to the provided
// registration token.


    fun closeKeyboard(activity: Activity) {

        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.getWindowToken(), 0)
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
            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {

            }

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
12 : comment receives a like +1 to receiver +notification  // type 3
13 : comment like removed -1 to receiver
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
