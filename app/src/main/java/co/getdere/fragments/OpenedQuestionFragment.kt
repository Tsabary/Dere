package co.getdere.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.*
import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.viewmodels.*
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.SharingHelper
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle
import kotlinx.android.synthetic.main.answer_comment_layout.view.*
import kotlinx.android.synthetic.main.answer_layout.view.*
import kotlinx.android.synthetic.main.fragment_opened_question.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class OpenedQuestionFragment : Fragment(), DereMethods {

    private lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var questionObject: Question
    lateinit var currentUserObject: Users


    lateinit var saveButton: TextView
    lateinit var openedQuestionAuthorReputation: TextView
    private lateinit var deleteBox: ConstraintLayout
    private lateinit var editDeleteBox: ConstraintLayout


    private lateinit var buo: BranchUniversalObject
    private lateinit var lp: LinkProperties

    val answersAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_opened_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            currentUserObject = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }

        val shareButton = opened_question_share
        saveButton = opened_question_save
        val answerButton = opened_question_answer_btn
        val answersRecycler = opened_question_answers_recycler
        val openedQuestionTitle = opened_question_title
        val openedQuestionContent = opened_question_content
        val openedQuestionTimeStamp = opened_question_timestamp
        val openedQuestionTags = opened_question_tags
        val openedQuestionUpVote = opened_question_upvote
        val openedQuestionDownVote = opened_question_downvote
        val openedQuestionVotes = opened_question_votes
        val openedQuestionAuthorImage = opened_question_author_image
        val openedQuestionAuthorName = opened_question_author_name
        deleteBox = opened_question_delete_box
        editDeleteBox = opened_question_edit_delete_container
        val deleteButton = opened_question_delete
        val editButton = opened_question_edit
        val removeButton = opened_question_remove
        val cancelButton = opened_question_remove
        openedQuestionAuthorReputation = opened_question_author_reputation


        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question
                checkIfQuestionSaved(0, activity)

                openedQuestionTitle.text = questionObject.title
                openedQuestionContent.text = questionObject.details
                openedQuestionTimeStamp.text = PrettyTime().format(Date(questionObject.timestamp))
                openedQuestionTags.text = questionObject.tags.joinToString()
                listenToAnswers(questionObject.id)

                if (question.author == currentUserObject.uid || currentUserObject.uid == getString(R.string.get_dere_uid)) {
                    editDeleteBox.visibility = View.VISIBLE

                } else {
                    editDeleteBox.visibility = View.GONE
                }

                deleteButton.setOnClickListener {
                    deleteBox.visibility = View.VISIBLE
                }

                editButton.setOnClickListener {
                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.editQuestionFragment,
                        "editQuestionFragment"
                    ).addToBackStack("editQuestionFragment")
                        .commit()
                    activity.subActive = activity.editQuestionFragment
                }

                removeButton.setOnClickListener {
                    FirebaseDatabase.getInstance().getReference("/questions/${question.id}").removeValue()
                        .addOnSuccessListener {

                            FirebaseDatabase.getInstance()
                                .getReference("/users/${currentUserObject.uid}/questions/${question.id}").removeValue()
                                .addOnSuccessListener {
                                    for (tag in question.tags) {
                                        FirebaseDatabase.getInstance().getReference("/tags/$tag/${question.id}")
                                            .removeValue()
                                    }

                                    activity.switchVisibility(0)
                                    activity.boardFragment.listenToQuestions()

                                    firebaseAnalytics.logEvent("question_answer_removed", null)
                                }
                        }
                }

                cancelButton.setOnClickListener {
                    deleteBox.visibility = View.GONE
                }

                executeVote(
                    "checkStatus",
                    questionObject.id,
                    currentUserObject.uid,
                    currentUserObject.name,
                    questionObject.author,
                    0,
                    openedQuestionVotes,
                    openedQuestionUpVote,
                    openedQuestionDownVote,
                    questionObject.id,
                    0,
                    openedQuestionAuthorReputation,
                    activity
                )


                buo = BranchUniversalObject()
                    .setCanonicalIdentifier(question.id)
                    .setTitle(question.title)
                    .setContentDescription("")
                    .setContentImageUrl("https://img1.10bestmedia.com/Images/Photos/352450/GettyImages-913753556_55_660x440.jpg")
                    .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setContentMetadata(ContentMetadata().addCustomMetadata("type", "question"))

                lp = LinkProperties()
                    .setFeature("sharing")
                    .setCampaign("content 123 launch")
                    .setStage("new user")


            }
        }
        )






        sharedViewModelRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(
                    if (user.image.isNotEmpty()) {
                        user.image
                    } else {
                        R.drawable.user_profile
                    }
                ).into(openedQuestionAuthorImage)
                openedQuestionAuthorName.text = user.name

                openedQuestionAuthorReputation.text = "(${numberCalculation(user.reputation)})"

                openedQuestionAuthorImage.setOnClickListener {
                    goToProfile(activity, user)
                }

                openedQuestionAuthorName.setOnClickListener {
                    goToProfile(activity, user)
                }
            }
        }
        )


        openedQuestionUpVote.setOnClickListener {
            if (currentUserObject.uid != questionObject.author) {
                executeVote(
                    "up",
                    questionObject.id,
                    currentUserObject.uid,
                    currentUserObject.name,
                    questionObject.author,
                    0,
                    openedQuestionVotes,
                    openedQuestionUpVote,
                    openedQuestionDownVote,
                    questionObject.id,
                    1,
                    openedQuestionAuthorReputation,
                    activity
                )
            }
        }

        openedQuestionDownVote.setOnClickListener {
            if (currentUserObject.uid != questionObject.author) {
                executeVote(
                    "down",
                    questionObject.id,
                    currentUserObject.uid,
                    currentUserObject.name,
                    questionObject.author,
                    0,
                    openedQuestionVotes,
                    openedQuestionUpVote,
                    openedQuestionDownVote,
                    questionObject.id,
                    1,
                    openedQuestionAuthorReputation,
                    activity
                )
            }
        }


        saveButton.setOnClickListener {
            checkIfQuestionSaved(1, activity)
        }

        shareButton.setOnClickListener {
            val ss = ShareSheetStyle(activity, "Check this out!", "Get Dere and start collecting destinations")
                .setCopyUrlStyle(resources.getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                .setMoreOptionStyle(resources.getDrawable(android.R.drawable.ic_menu_search), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                .setAsFullWidthStyle(true)
                .setSharingTitle("Share With")

            buo.showShareSheet(activity, lp, ss, object : Branch.BranchLinkShareListener {
                override fun onShareLinkDialogLaunched() {}
                override fun onShareLinkDialogDismissed() {}
                override fun onLinkShareResponse(sharedLink: String, sharedChannel: String, error: BranchError) {}
                override fun onChannelSelected(channelName: String) {
                    firebaseAnalytics.logEvent("question_shared_$channelName", null)
                }
            })
        }

        answerButton.setOnClickListener {
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.answerFragment, "answerFragment")
                .addToBackStack("answerFragment")
                .commit()
            activity.subActive = activity.answerFragment
        }

        answersRecycler.adapter = answersAdapter
        answersRecycler.layoutManager = LinearLayoutManager(this.context)
    }

    private fun goToProfile(activity: MainActivity, user: Users) {
        if (user.uid != currentUserObject.uid) {
            activity.subFm.beginTransaction().add(
                R.id.feed_subcontents_frame_container,
                activity.profileRandomUserFragment,
                "profileRandomUserFragment"
            ).addToBackStack("profileRandomUserFragment")
                .commit()
            activity.subActive = activity.profileRandomUserFragment
        } else {
            activity.navigateToProfile()
        }
    }

    fun listenToAnswers(qId: String) {
        answersAdapter.clear()
        FirebaseDatabase.getInstance().getReference("/questions/$qId/answers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (answer in p0.children) {

                        val singleAnswerFromDB = answer.child("body").getValue(Answers::class.java)

                        if (singleAnswerFromDB != null) {
                            answersAdapter.add(
                                SingleAnswer(
                                    singleAnswerFromDB,
                                    currentUserObject,
                                    activity as MainActivity
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
    }

    private fun checkIfQuestionSaved(event: Int, activity: Activity) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        val refCurrentUserSavedQuestions =
            FirebaseDatabase.getInstance().getReference("/users/${currentUserObject.uid}/saved-questions")
        val questionsListOfUsersWhoSaved = FirebaseDatabase.getInstance()
            .getReference("/questions/${questionObject.id}/main/saves/${currentUserObject.uid}")

        refCurrentUserSavedQuestions.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(questionObject.id)) {
                    if (event == 1) {
                        saveButton.text = getString(R.string.save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900))

                        refCurrentUserSavedQuestions.child(questionObject.id).removeValue().addOnSuccessListener {

                            questionsListOfUsersWhoSaved.removeValue().addOnSuccessListener {
                                changeReputation(
                                    11,
                                    questionObject.id,
                                    questionObject.id,
                                    currentUserObject.uid,
                                    currentUserObject.name,
                                    questionObject.author,
                                    openedQuestionAuthorReputation,
                                    "questionsave",
                                    activity
                                )

                                firebaseAnalytics.logEvent("question_unsaved", null)
                                (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                            }
                        }

                    } else {
                        saveButton.text = getString(R.string.saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700))
                    }
                } else {
                    if (event == 1) {

                        saveButton.text = getString(R.string.saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700))

                        FirebaseDatabase.getInstance()
                            .getReference("/users/${currentUserObject.uid}/saved-questions/${questionObject.id}")
                            .setValue(true)
                            .addOnSuccessListener {

                                questionsListOfUsersWhoSaved.setValue(true).addOnSuccessListener {
                                    for (t in questionObject.tags) {
                                        FirebaseDatabase.getInstance()
                                            .getReference("users/${currentUserObject.uid}/interests/$t").setValue(true)
                                    }

                                    changeReputation(
                                        10,
                                        questionObject.id,
                                        questionObject.id,
                                        currentUserObject.uid,
                                        currentUserObject.name,
                                        questionObject.author,
                                        openedQuestionAuthorReputation,
                                        "questionsave",
                                        activity
                                    )

                                    firebaseAnalytics.logEvent("question_saved", null)
                                    (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                                }
                            }

                    } else {
                        saveButton.text = getString(R.string.save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900))
                    }
                }
            }
        })
    }
}


class SingleAnswer(
    val answer: Answers, val currentUser: Users, val activity: MainActivity
) : Item<ViewHolder>(), DereMethods {

    val commentsAdapter = GroupAdapter<ViewHolder>()
    val imagesAdapter = GroupAdapter<ViewHolder>()

    lateinit var sharedViewModelSecondRandomUser: SharedViewModelSecondRandomUser
    private lateinit var sharedViewModelSecondImage: SharedViewModelSecondImage
    private lateinit var sharedViewModelAnswer: SharedViewModelAnswer
    override fun getLayout(): Int = R.layout.answer_layout


    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelAnswer = ViewModelProviders.of(it).get(SharedViewModelAnswer::class.java)
            sharedViewModelSecondRandomUser = ViewModelProviders.of(it).get(SharedViewModelSecondRandomUser::class.java)
            sharedViewModelSecondImage = ViewModelProviders.of(it).get(SharedViewModelSecondImage::class.java)
        }

        val userImage = viewHolder.itemView.single_answer_author_image
        val upvote = viewHolder.itemView.single_answer_upvote
        val downvote = viewHolder.itemView.single_answer_downvote
        val comment = viewHolder.itemView.single_answer_comment_button
        val edit = viewHolder.itemView.single_answer_edit
        val delete = viewHolder.itemView.single_answer_delete
        val deleteBox = viewHolder.itemView.single_answer_delete_box
        val cancel = viewHolder.itemView.single_answer_cancel
        val remove = viewHolder.itemView.single_answer_remove

        val commentsRecycler = viewHolder.itemView.single_answer_comments_recycler
        commentsRecycler.adapter = commentsAdapter
        commentsRecycler.layoutManager = LinearLayoutManager(viewHolder.root.context)

        val imagesRecycler = viewHolder.itemView.single_answer_photos_recycler
        imagesRecycler.adapter = imagesAdapter
        imagesRecycler.layoutManager = GridLayoutManager(viewHolder.root.context, 4)


        if (answer.author == currentUser.uid) {
            delete.visibility = View.VISIBLE
            edit.visibility = View.VISIBLE
        } else {
            delete.visibility = View.GONE
            edit.visibility = View.GONE
        }

        edit.setOnClickListener {
            sharedViewModelAnswer.sharedAnswerObject.postValue(answer)
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.editAnswerFragment, "editAnswerFragment")
                .addToBackStack("editAnswerFragment")
                .commit()
            activity.subActive = activity.editAnswerFragment
            activity.isEditAnswerActive = true
        }

        delete.setOnClickListener {
            deleteBox.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            deleteBox.visibility = View.GONE
        }

        remove.setOnClickListener {
            FirebaseDatabase.getInstance()
                .getReference("/questions/${answer.questionId}/answers/${answer.answerId}").removeValue()
            activity.openedQuestionFragment.answersAdapter.removeGroup(position)
            activity.openedQuestionFragment.listenToAnswers(answer.questionId)
            deleteBox.visibility = View.GONE

            val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)
            firebaseAnalytics.logEvent("question_answer_removed", null)
        }

        userImage.setOnClickListener {
            if (currentUser.uid != answer.author) {
                FirebaseDatabase.getInstance().getReference("/users/${answer.author}/profile")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            sharedViewModelSecondRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                            activity.subFm.beginTransaction().add(
                                R.id.feed_subcontents_frame_container,
                                activity.profileSecondRandomUserFragment,
                                "profileSecondRandomUserFragment"
                            ).addToBackStack("profileSecondRandomUserFragment")
                                .commit()
                            activity.subActive = activity.profileSecondRandomUserFragment
                            activity.isSecondRandomUserProfileActive = true
                        }
                    })
            } else {
                activity.navigateToProfile()
            }
        }


        FirebaseDatabase.getInstance()
            .getReference("/questions/${answer.questionId}/answers/${answer.answerId}/comments")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val singleCommentFromDB = p0.child("body").getValue(AnswerComments::class.java)
                    if (singleCommentFromDB != null) {
                        commentsAdapter.add(SingleAnswerComment(singleCommentFromDB, activity, currentUser))
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })

        if (answer.photos.isNotEmpty()) {
            viewHolder.itemView.single_answer_photos_recycler.visibility = View.VISIBLE

            for (imagePath in answer.photos) {
                FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val imageObject = p0.getValue(Images::class.java)
                            if (imageObject != null) {
                                imagesAdapter.add(FeedImage(imageObject, 1))
                            }
                        }
                    })
            }
        } else {
            viewHolder.itemView.single_answer_photos_recycler.visibility = View.GONE
        }

        executeVote(
            "checkStatus",
            answer.questionId,
            currentUser.uid,
            currentUser.name,
            answer.author,
            1,
            viewHolder.itemView.single_answer_votes,
            viewHolder.itemView.single_answer_upvote,
            viewHolder.itemView.single_answer_downvote,
            answer.answerId,
            0,
            viewHolder.itemView.single_answer_author_reputation,
            activity
        )

        val answerAuthorRef = FirebaseDatabase.getInstance().getReference("/users/${answer.author}/profile")
        answerAuthorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)

                if (author != null) {

                    val date = PrettyTime().format(Date(answer.timestamp))
                    Glide.with(viewHolder.root.context).load(
                        if (author.image.isNotEmpty()) {
                            author.image
                        } else {
                            R.drawable.user_profile
                        }
                    )
                        .into(userImage)

                    viewHolder.itemView.single_answer_author_name.text = author.name
                    viewHolder.itemView.single_answer_content.text = answer.content
                    viewHolder.itemView.single_answer_timestamp.text = date
                    viewHolder.itemView.single_answer_author_reputation.text =
                        "(${numberCalculation(author.reputation)})"
                }
            }
        })

        upvote.setOnClickListener {

            if (answer.author != currentUser.uid) {
                executeVote(
                    "up",
                    answer.questionId,
                    currentUser.uid,
                    currentUser.name,
                    answer.author,
                    1,
                    viewHolder.itemView.single_answer_votes,
                    viewHolder.itemView.single_answer_upvote,
                    viewHolder.itemView.single_answer_downvote,
                    answer.answerId,
                    1,
                    viewHolder.itemView.single_answer_author_reputation,
                    activity
                )
            }
        }

        downvote.setOnClickListener {

            if (answer.author != currentUser.uid) {
                executeVote(
                    "down",
                    answer.questionId,
                    currentUser.uid,
                    currentUser.name,
                    answer.author,
                    1,
                    viewHolder.itemView.single_answer_votes,
                    viewHolder.itemView.single_answer_upvote,
                    viewHolder.itemView.single_answer_downvote,
                    answer.answerId,
                    1,
                    viewHolder.itemView.single_answer_author_reputation,
                    activity
                )
            }
        }

        comment.setOnClickListener {
            activity.answerObject = answer
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.answerCommentFragment, "answerCommentFragment")
                .addToBackStack("answerCommentFragment")
                .commit()
            activity.subActive = activity.answerCommentFragment

        }

        imagesAdapter.setOnItemClickListener { item, _ ->

            val imageItem = item as FeedImage

            sharedViewModelSecondImage.sharedSecondImageObject.postValue(imageItem.image)
            answerAuthorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    sharedViewModelSecondRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))

                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.secondImageFullSizeFragment,
                        "secondImageFullSizeFragment"
                    ).addToBackStack("secondImageFullSizeFragment")
                        .commit()
                    activity.subActive = activity.secondImageFullSizeFragment
                }
            })
        }
    }
}

class SingleAnswerComment(val comment: AnswerComments, val activity: MainActivity, val currentUser: Users) :
    Item<ViewHolder>(), DereMethods {

    lateinit var sharedViewModelSecondRandomUser: SharedViewModelSecondRandomUser

    override fun getLayout(): Int = R.layout.answer_comment_layout


    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelSecondRandomUser = ViewModelProviders.of(it).get(SharedViewModelSecondRandomUser::class.java)
        }

        val commentContent = viewHolder.itemView.answer_comment_comment
        val commentContentEditable = viewHolder.itemView.answer_comment_comment_editable
        val commentContentTimestamp = viewHolder.itemView.answer_comment_timestamp
        val edit = viewHolder.itemView.answer_comment_edit
        val save = viewHolder.itemView.answer_comment_save
        val delete = viewHolder.itemView.answer_comment_delete
        val deleteBox = viewHolder.itemView.answer_comment_delete_box
        val cancel = viewHolder.itemView.answer_comment_cancel
        val remove = viewHolder.itemView.answer_comment_remove
        val authorImage = viewHolder.itemView.answer_comment_author_image

        commentContent.text = comment.content
        commentContentEditable.setText(comment.content)
        commentContentTimestamp.text = PrettyTime().format(Date(comment.timestamp))


        val commentRef = FirebaseDatabase.getInstance()
            .getReference("/questions/${comment.questionId}/answers/${comment.answerId}/comments/${comment.commentId}")
        val refUser = FirebaseDatabase.getInstance().getReference("/users/${comment.author}/profile")


        if (comment.author == currentUser.uid) {
            edit.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        } else {
            edit.visibility = View.GONE
            delete.visibility = View.GONE
        }

        delete.setOnClickListener {
            deleteBox.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            deleteBox.visibility = View.GONE
        }

        remove.setOnClickListener {
            commentRef.removeValue()
            viewHolder.itemView.answer_comment_removed_filler_box.visibility = View.VISIBLE
            deleteBox.visibility = View.GONE
            delete.isClickable = false
            edit.isClickable = false

            val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)
            firebaseAnalytics.logEvent("question_answer_comment_removed", null)
        }

        edit.setOnClickListener {
            commentContent.visibility = View.GONE
            commentContentEditable.visibility = View.VISIBLE
            commentContentEditable.requestFocus()
            commentContentEditable.setSelection(commentContentEditable.text.length)
            delete.visibility = View.GONE
            edit.visibility = View.GONE
            save.visibility = View.VISIBLE
        }

        save.setOnClickListener {
            delete.visibility = View.VISIBLE
            edit.visibility = View.VISIBLE
            save.visibility = View.GONE

            commentContent.visibility = View.VISIBLE
            commentContentEditable.visibility = View.GONE

            commentContent.text = commentContentEditable.text.toString()

            commentRef.child("body").child("content").setValue(commentContentEditable.text.toString())
        }

        authorImage.setOnClickListener {

            if (currentUser.uid != comment.author) {
                refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        sharedViewModelSecondRandomUser.randomUserObject.postValue(p0.getValue(Users::class.java))
                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.profileSecondRandomUserFragment,
                            "profileSecondRandomUserFragment"
                        ).addToBackStack("profileSecondRandomUserFragment")
                            .commit()
                        activity.subActive = activity.profileSecondRandomUserFragment
                    }
                })
            } else {
                activity.navigateToProfile()
            }
        }

        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)

                if (author != null) {

                    viewHolder.itemView.answer_comment_author_name.text = author.name
                    viewHolder.itemView.answer_comment_author_reputation.text =
                        "(${numberCalculation(author.reputation)})"

                    Glide.with(viewHolder.root.context).load(
                        if (author.image.isNotEmpty()) {
                            author.image
                        } else {
                            R.drawable.user_profile
                        }
                    )
                        .into(authorImage)
                }
            }
        })
    }
}
