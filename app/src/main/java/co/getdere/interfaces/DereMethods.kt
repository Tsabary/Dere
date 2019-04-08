package co.getdere.interfaces

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import co.getdere.models.*
import co.getdere.R
import com.google.firebase.database.*
import android.view.inputmethod.InputMethodManager


interface DereMethods {


    //following methods are for the forum_icon


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
        userReputationView: TextView
    ) {

        val refVotes = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$mainPostId/main/votes")
        } else {
            FirebaseDatabase.getInstance().getReference("/questions/$mainPostId/answers/$specificPostId/votes")
        }

        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(postVotesSnapshot: DataSnapshot) {

                setVotesCount(specificPostId, mainPostId, votesView, postType)



                if (postVotesSnapshot.hasChild(initiatorId)) {

                    val voteValue = postVotesSnapshot.child(initiatorId).getValue(Int::class.java)

                    when (voteValue) {

                        1 -> {
                            if (event == 1 && initiatorId != receiverId) {

                                when (vote) {

                                    "up" -> return
                                    "down" -> {
                                        defaultView(upvoteView, downvoteView)

                                        refVotes.setValue(mapOf(initiatorId to 0)).addOnSuccessListener {
                                            setVotesCount(specificPostId, mainPostId, votesView, postType)
//                                            setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)
                                        }
                                        if (postType == 0) {
                                            changeReputation(
                                                1,
                                                specificPostId,
                                                mainPostId,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "vote"
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
                                                "vote"
                                            )
                                        }
                                    }
                                }
                            } else {
                                upView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)
                            }
                        }

                        0 -> {
                            if (event == 1 && initiatorId != receiverId) {
                                when (vote) {
                                    "up" -> {
                                        upView(upvoteView, downvoteView)
                                        refVotes.setValue(mapOf(initiatorId to 1)).addOnSuccessListener {
                                            setVotesCount(specificPostId, mainPostId, votesView, postType)
//                                            setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)

                                        }


                                        if (postType == 0) {
                                            changeReputation(
                                                0,
                                                specificPostId,
                                                mainPostId,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "vote"
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
                                                "vote"
                                            )
                                        }
                                    }
                                    "down" -> {
                                        downView(upvoteView, downvoteView)
                                        setVotesCount(specificPostId, mainPostId, votesView, postType)
//                                        setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)


                                        refVotes.setValue(mapOf(initiatorId to -1)).addOnSuccessListener {
                                            setVotesCount(specificPostId, mainPostId, votesView, postType)
                                        }
                                        if (postType == 0) {
                                            changeReputation(
                                                4,
                                                specificPostId,
                                                mainPostId,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "vote"
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
                                                "vote"
                                            )
                                        }
                                    }
                                }
                            } else {
                                defaultView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)

                            }

                        }

                        -1 -> {
                            if (event == 1 && initiatorId != receiverId) {
                                when (vote) {
                                    "up" -> {
                                        defaultView(upvoteView, downvoteView)

                                        refVotes.setValue(mapOf(initiatorId to 0)).addOnSuccessListener {
                                            setVotesCount(specificPostId, mainPostId, votesView, postType)
//                                            setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)

                                        }

                                        changeReputation(
                                            5,
                                            specificPostId,
                                            mainPostId,
                                            initiatorId,
                                            initiatorName,
                                            receiverId,
                                            userReputationView,
                                            "vote"
                                        )

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

                    if (event == 1 && initiatorId != receiverId) {
                        when (vote) {
                            "up" -> {
                                upView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)


                                refVotes.setValue(mapOf(initiatorId to 1)).addOnSuccessListener {
                                    setVotesCount(specificPostId, mainPostId, votesView, postType)
                                }
                                if (postType == 0) {
                                    changeReputation(
                                        0,
                                        specificPostId,
                                        mainPostId,
                                        initiatorId,
                                        initiatorName,
                                        receiverId,
                                        userReputationView,
                                        "vote"
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
                                        "vote"
                                    )
                                }
                            }
                            "down" -> {
                                downView(upvoteView, downvoteView)
                                setVotesCount(specificPostId, mainPostId, votesView, postType)


                                refVotes.setValue(mapOf(initiatorId to -1)).addOnSuccessListener {
                                    setVotesCount(specificPostId, mainPostId, votesView, postType)
                                }
                                if (postType == 0) {
                                    changeReputation(
                                        4,
                                        specificPostId,
                                        mainPostId,
                                        initiatorId,
                                        initiatorName,
                                        receiverId,
                                        userReputationView,
                                        "vote"
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
                                        "vote"
                                    )
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
        downvoteView.setImageResource(R.drawable.arrow_down_default)
    }

    fun defaultView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        downvoteView.setImageResource(R.drawable.arrow_down_default)
    }

    fun downView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        downvoteView.setImageResource(R.drawable.arrow_down_active)
    }

    fun setVotesCount(postId: String, questionId: String, votesView: TextView, postType: Int) {


        val refVotes = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/main/votes")
        } else {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/$postId/votes")
        }


        refVotes.addChildEventListener(object : ChildEventListener {

            var count = 0

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val rating = p0.value.toString().toInt()
                count += rating
                votesView.text = numberCalculation(count.toLong())
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

    fun setVotesCountSimplifiedAfterClickForFastResponse(votesView: TextView, action: String) {

        if (action == "up") {
            val voteCount = votesView.text.toString().toInt() + 1
            votesView.text = voteCount.toString()
        } else {
            val voteCount = votesView.text.toString().toInt() - 1
            votesView.text = voteCount.toString()
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


    fun executeLike(
        image: Images,
        initiatorId: String,
        likeCount: TextView,
        likeButton: ImageButton,
        event: Int,
        initiatorName: String,
        receiverId: String,
        userReputationView: TextView
    ) {

        val allUserRef = FirebaseDatabase.getInstance().getReference("/users/$initiatorId")



        allUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("likes")) {

                    val allUserLikesRef = FirebaseDatabase.getInstance().getReference("/users/$initiatorId/likes")

                    allUserLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.hasChild(image.id)) {

                                if (event == 1 && initiatorId != receiverId) {

                                    allUserLikesRef.child(image.id).removeValue().addOnSuccessListener {

                                        val refImageLikes =
                                            FirebaseDatabase.getInstance()
                                                .getReference("/images/${image.id}/likes/$initiatorId")

                                        refImageLikes.removeValue().addOnSuccessListener {
                                            likeButton.setImageResource(R.drawable.heart)

                                            listenToLikeCount(likeCount, image)

                                            changeReputation(
                                                15,
                                                image.id,
                                                image.id,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "like"
                                            )

                                        }
                                    }

                                } else {

                                    likeButton.setImageResource(R.drawable.heart_active)


                                }

                            } else {

                                if (event == 1 && initiatorId != receiverId) {

                                    val refUserLikes =
                                        FirebaseDatabase.getInstance()
                                            .getReference("/users/$initiatorId/likes")

                                    refUserLikes.setValue(mapOf(image.id to true)).addOnSuccessListener {

                                        val refImageLikes =
                                            FirebaseDatabase.getInstance()
                                                .getReference("/images/${image.id}/likes")

                                        refImageLikes.setValue(mapOf(initiatorId to true)).addOnSuccessListener {

                                            likeButton.setImageResource(R.drawable.heart_active)

                                            listenToLikeCount(likeCount, image)

                                            changeReputation(
                                                14,
                                                image.id,
                                                image.id,
                                                initiatorId,
                                                initiatorName,
                                                receiverId,
                                                userReputationView,
                                                "like"
                                            )

                                        }

                                    }

                                } else {
                                    likeButton.setImageResource(R.drawable.heart)
                                }


                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }


                    })


                } else {
                    if (event == 1 && initiatorId != receiverId) {

                        val refUserLikes =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/$initiatorId/likes")

                        refUserLikes.setValue(mapOf(image.id to true)).addOnSuccessListener {

                            val refImageLikes =
                                FirebaseDatabase.getInstance()
                                    .getReference("/images/${image.id}/likes")

                            refImageLikes.setValue(mapOf(initiatorId to true)).addOnSuccessListener {

                                likeButton.setImageResource(R.drawable.heart_active)

                                listenToLikeCount(likeCount, image)

                                changeReputation(
                                    14,
                                    image.id,
                                    image.id,
                                    initiatorId,
                                    initiatorName,
                                    receiverId,
                                    userReputationView,
                                    "like"
                                )
                            }

                        }

                    } else {
                        likeButton.setImageResource(R.drawable.heart)

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

                if (p0.hasChild("comments")) {

                    commentCount.text = numberCalculation(p0.child("comments").childrenCount)

                } else {
                    commentCount.text = "0"
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


    fun checkIfBucketed(addToBucket: ImageView, image: Images, uid: String) {

        val refUserBucket = FirebaseDatabase.getInstance().getReference("/users/$uid/buckets")

        refUserBucket.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(image.id)) {
                    addToBucket.setImageResource(R.drawable.bucket_saved)
                } else {
                    addToBucket.setImageResource(R.drawable.bucket)
                }
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
                    specificPostId
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
                    specificPostId
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
        action: String
    ) {

        val refReceiverReputation =
            FirebaseDatabase.getInstance()
                .getReference("/users/$receiverId/reputation/$specificPostId$initiatorId$action")
        val refInitiatorReputation =
            FirebaseDatabase.getInstance().getReference("/users/$initiatorId/reputation/$specificPostId$action")

        when (scenarioType) {

            0 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(0, 0, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
            }

            1 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -5)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}0")

                refReceiverReputation.removeValue()
                notificationRef.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            2 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 10)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(1, 2, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            3 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -10)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}2")

                refReceiverReputation.removeValue()
                notificationRef.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            4 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -2)
                refReceiverReputation.setValue(valueForReceiver)
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, -1)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
                sendNotification(0, 4, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            5 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 2)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}4")
                notificationRef.removeValue()


                refReceiverReputation.removeValue()


//                val valueForInitiator = ReputationScore(specificPostId, initiatorId, 1)
//                refInitiatorReputation.setValue(valueForInitiator)

                refInitiatorReputation.removeValue()


                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
            }

            6 -> {
                val valueForInitiator = ReputationScore(specificPostId, initiatorId, 2)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)

                sendNotification(1, 6, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            7 -> {
//                val valueForInitiator = ReputationScore(specificPostId, initiatorId, -2)
//                refInitiatorReputation.setValue(valueForInitiator)

                refInitiatorReputation.removeValue()

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}6")
                notificationRef.removeValue()


                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)


            }

            8 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 15)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(2, 8, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            9 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -15)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}8")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            10 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(0, 10, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            11 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -5)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}10")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            12 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 1)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(3, 12, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            13 -> {

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}12")
                notificationRef.removeValue()


                refReceiverReputation.removeValue()
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            14 -> {
                val valueForReceiver = ReputationScore(specificPostId, initiatorId, 2)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                sendNotification(2, 14, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)

            }

            15 -> {
//                val valueForReceiver = ReputationScore(specificPostId, initiatorId, -2)
//                refReceiverReputation.setValue(valueForReceiver)

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}14")
                notificationRef.removeValue()

                refReceiverReputation.removeValue()

                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            16 -> {


                sendNotification(2, 16, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)


            }

            17 -> {

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/gallery/$mainPostId$specificPostId${initiatorId}16")
                notificationRef.removeValue()
            }

            18 -> {


                sendNotification(1, 18, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)


            }

            19 -> {

                val notificationRef = FirebaseDatabase.getInstance()
                    .getReference("/users/$receiverId/notifications/board/$mainPostId$specificPostId${initiatorId}18")
                notificationRef.removeValue()
            }

            20 -> {

                sendNotification(4, 20, initiatorId, initiatorName, mainPostId, specificPostId, receiverId)
            }

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
                    userObject.updateChildren(mapOf("reputation" to count.toString()))
                    userReputationView.text = "(${count.toString()})"
                    Log.d("checkIfCountWorks", count.toString())
                }
            }


            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

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
