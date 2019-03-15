package co.getdere.Interfaces

import android.widget.ImageView
import android.widget.TextView
import co.getdere.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface DereMethods {

    open fun executeVote(vote: String, answerId : String, uid : String, answerVotesView : TextView, upvoteView : ImageView, downvoteView : ImageView) {


        val refVotes = FirebaseDatabase.getInstance().getReference("/votes/$answerId")
        refVotes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val refUserVote = FirebaseDatabase.getInstance().getReference("/votes/$answerId/$uid")

                if (p0.hasChild(uid)) {
                    var voteValue = 0
                    refUserVote.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            voteValue = p0.getValue().toString().toInt()

                            when (voteValue) {

                                1 -> {
                                    when (vote) {
                                        "up" -> refUserVote.setValue(1)
                                        "down" -> refUserVote.setValue(0)
                                        else -> refUserVote.setValue(0)
                                    }
                                }

                                0 -> {
                                    when (vote) {
                                        "up" -> refUserVote.setValue(1)
                                        "down" -> refUserVote.setValue(-1)
                                        else -> refUserVote.setValue(0)

                                    }
                                }

                                -1 -> {
                                    when (vote) {
                                        "up" -> refUserVote.setValue(0)
                                        "down" -> refUserVote.setValue(-1)
                                        else -> refUserVote.setValue(0)

                                    }
                                }
                            }

                        }

                    })


                } else {
                    when (vote) {
                        "up" -> refUserVote.setValue(1)
                        "down" -> refUserVote.setValue(-1)
                        else -> refUserVote.setValue(0)
                    }
                }
            }


        })











        refVotes.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var count = 0

                for (ds in p0.getChildren()) {
                    val rating = ds.getValue(Int::class.java)
                    count += rating!!
                    answerVotesView.text = count.toString()
                }

                if (p0.hasChild(uid)) {
                    val refUserVote = FirebaseDatabase.getInstance().getReference("/votes/$answerId/$uid")
                    var voteInt = 0
                    refUserVote.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            voteInt = p0.getValue().toString().toInt()

                            when (voteInt) {
                                1 -> {
                                    upvoteView.setImageResource(R.drawable.arrow_up_active)
                                    downvoteView.setImageResource(R.drawable.arrow_down_default)
                                }
                                0 -> {
                                    upvoteView.setImageResource(R.drawable.arrow_up_default)
                                    downvoteView.setImageResource(R.drawable.arrow_down_default)
                                }
                                -1 -> {
                                    upvoteView.setImageResource(R.drawable.arrow_up_default)
                                    downvoteView.setImageResource(R.drawable.arrow_down_active)
                                }
                            }

                        }

                    })


                }
            }


        })






    }



}