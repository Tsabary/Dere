package co.getdere.Interfaces

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import co.getdere.Models.BoardNotification
import co.getdere.Models.ReputationScore
import co.getdere.Models.SimpleInt
import co.getdere.R
import com.google.firebase.database.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

interface DereMethods {

    fun executeVote(
        vote: String,
        questionId: String,
        initiatorId: String,
        initiatorName: String,
        receiverId: String,
        postType: Int,
        votesView: TextView,
        upvoteView: ImageView,
        downvoteView: ImageView,
        postId: String,
        event: Int,
        userReputationView: TextView
    ) {


        val refVotes = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/main/votes")
        } else {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/$postId/votes")
        }

        val refUserVote = if (postType == 0) {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/main/votes/$initiatorId")
        } else {
            FirebaseDatabase.getInstance().getReference("/questions/$questionId/answers/$postId/votes/$initiatorId")
        }



        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(postVotesSnapshot: DataSnapshot) {

                setVotesCount(postId, questionId, votesView, postType)

                if (postVotesSnapshot.hasChild(initiatorId)) {

                    refUserVote.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val voteValue = p0.getValue(SimpleInt::class.java)!!.score

                            when (voteValue) {

                                1 -> {
                                    if (event == 1) {
                                        when (vote) {

                                            "up" -> return
                                            "down" -> {
                                                defaultView(upvoteView, downvoteView)

                                                refUserVote.setValue(SimpleInt(0)).addOnSuccessListener {
//                                                    setVotesCount(postId, questionId, votesView, postType)
                                                    setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)
                                                }
                                                if (postType == 0) {
                                                    changeReputation(
                                                        1,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                } else {
                                                    changeReputation(
                                                        3,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        upView(upvoteView, downvoteView)
                                        setVotesCount(postId, questionId, votesView, postType)
                                    }
                                }

                                0 -> {
                                    if (event == 1) {
                                        when (vote) {
                                            "up" -> {
                                                upView(upvoteView, downvoteView)
                                                refUserVote.setValue(SimpleInt(1)).addOnSuccessListener {
//                                                    setVotesCount(postId, questionId, votesView, postType)
                                                    setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)

                                                }


                                                if (postType == 0) {
                                                    changeReputation(
                                                        0,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                } else {
                                                    changeReputation(
                                                        2,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                }
                                            }
                                            "down" -> {
                                                downView(upvoteView, downvoteView)
//                                                setVotesCount(postId, questionId, votesView, postType)
                                                setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)



                                                refUserVote.setValue(SimpleInt(-1)).addOnSuccessListener {
                                                    setVotesCount(postId, questionId, votesView, postType)
                                                }
                                                if (postType == 0) {
                                                    changeReputation(
                                                        4,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                } else {
                                                    changeReputation(
                                                        4,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        defaultView(upvoteView, downvoteView)
                                        setVotesCount(postId, questionId, votesView, postType)

                                    }

                                }

                                -1 -> {
                                    if (event == 1) {
                                        when (vote) {
                                            "up" -> {
                                                defaultView(upvoteView, downvoteView)

                                                refUserVote.setValue(SimpleInt(0)).addOnSuccessListener {
//                                                    setVotesCount(postId, questionId, votesView, postType)
                                                    setVotesCountSimplifiedAfterClickForFastResponse(votesView, vote)

                                                }
                                                if (postType == 0) {
                                                    changeReputation(
                                                        5,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                } else {
                                                    changeReputation(
                                                        5,
                                                        postId,
                                                        initiatorId,
                                                        receiverId,
                                                        userReputationView
                                                    )
                                                }
                                            }
                                            "down" -> return
                                        }
                                    } else {
                                        downView(upvoteView, downvoteView)
                                        setVotesCount(postId, questionId, votesView, postType)
                                    }
                                }
                            }

                        }
                    })


                } else {

                    if (event == 1) {
                        when (vote) {
                            "up" -> {
                                upView(upvoteView, downvoteView)
                                setVotesCount(postId, questionId, votesView, postType)


                                refUserVote.setValue(SimpleInt(1)).addOnSuccessListener {
                                    setVotesCount(postId, questionId, votesView, postType)
                                }
                                if (postType == 0) {
                                    changeReputation(0, postId, initiatorId, receiverId, userReputationView)
                                } else {
                                    changeReputation(2, postId, initiatorId, receiverId, userReputationView)
                                }
                            }
                            "down" -> {
                                downView(upvoteView, downvoteView)
                                setVotesCount(postId, questionId, votesView, postType)


                                refUserVote.setValue(SimpleInt(-1)).addOnSuccessListener {
                                    setVotesCount(postId, questionId, votesView, postType)
                                }
                                if (postType == 0) {
                                    changeReputation(4, postId, initiatorId, receiverId, userReputationView)
                                } else {
                                    changeReputation(4, postId, initiatorId, receiverId, userReputationView)
                                }
                            }
                        }
                    } else {
                        defaultView(upvoteView, downvoteView)
                        setVotesCount(postId, questionId, votesView, postType)


                    }

                }
            }
        })


// these following chunk is just responsible to send the notification
        val refUserNotifications =
            FirebaseDatabase.getInstance().getReference("/users/$receiverId/notifications/board").push()
        refUserNotifications.setValue(BoardNotification(postType, initiatorName, questionId, initiatorId))
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

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                // this code works now but I m not certain why. For my understanding the children are simpleInts, but when getting them as Longs it works.

                var count = 0

                for (ds in p0.children) {
                    val rating = ds.getValue(Long::class.java)
                    count += rating!!.toInt()
                    votesView.text = count.toString()
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

    fun setVotesCountSimplifiedAfterClickForFastResponse(votesView: TextView, action : String) {

        if (action == "up"){
           val voteCount = votesView.text.toString().toInt() +1
            votesView.text = voteCount.toString()
        } else {
            val voteCount = votesView.text.toString().toInt() -1
            votesView.text = voteCount.toString()
        }


    }


    fun changeReputation(
        type: Int,
        postId: String,
        initiatorId: String,
        receiverId: String,
        userReputationView: TextView
    ) {

        val refReceiverReputation = FirebaseDatabase.getInstance().getReference("/users/$receiverId/reputation").push()
        val refInitiatorReputation =
            FirebaseDatabase.getInstance().getReference("/users/$initiatorId/reputation").push()

        when (type) {

            0 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            1 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, -5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            2 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 10)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            3 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, -10)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            4 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, -2)
                refReceiverReputation.setValue(valueForReceiver)
                val valueForInitiator = ReputationScore(postId, initiatorId, -1)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
            }

            5 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 2)
                refReceiverReputation.setValue(valueForReceiver)
                val valueForInitiator = ReputationScore(postId, initiatorId, 1)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
            }

            6 -> {
                val valueForInitiator = ReputationScore(postId, initiatorId, 2)
                refInitiatorReputation.setValue(valueForInitiator)
                updateUserFinalReputation(initiatorId, refInitiatorReputation, userReputationView)
            }

            7 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 15)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            8 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, -15)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            9 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            10 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, -5)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }

            11 -> {
                val valueForReceiver = ReputationScore(postId, initiatorId, 2)
                refReceiverReputation.setValue(valueForReceiver)
                updateUserFinalReputation(receiverId, refReceiverReputation, userReputationView)
            }
        }
    }


    fun updateUserFinalReputation(id: String, ref: DatabaseReference, userReputationView: TextView) {

        val refTest = FirebaseDatabase.getInstance().getReference("/$id/reputation")

        refTest.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

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
                    userReputationView.text = "reputation: 0"
                    Log.d("checkIfCountWorks", count.toString())
                } else {
                    userObject.updateChildren(mapOf("reputation" to count.toString()))
                    userReputationView.text = "reputation: ${count.toString()}"
                    Log.d("checkIfCountWorks", count.toString())
                }
            }
        })
    }


}

/*

0 : question upvote +5 to receiver
1 : question upvote is removed -5 to receiver
2 : answer upvoted +10 to receiver
3 : answer upvote is removed -10 to receiver
4 : question/answer downvote -2 for receiver -1 for initiator
5 : question answer downvote is removed +2 for receiver +1 for initiator
6 : answer given +2 to initiator
7 : photo bucketed +15 to receiver
8 : photo unbucketed -15 to receiver
9 : question saved +5 to receiver
10 : question unsaved - 5 to receiver
11 : comment receives a like +2 to receiver



*/
