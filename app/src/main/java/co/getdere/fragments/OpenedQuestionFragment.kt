package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.*
import co.getdere.R
import co.getdere.groupieAdapters.FeedImage
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelQuestion
import co.getdere.viewmodels.SharedViewModelRandomUser
import com.bumptech.glide.Glide
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

    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var questionObject: Question


    lateinit var currentUserObject: Users
    lateinit var randomUserObject: Users

    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    lateinit var saveButton: TextView

    lateinit var openedQuestionAuthorReputation: TextView


    lateinit var buo: BranchUniversalObject
    lateinit var lp: LinkProperties

    val answersAdapter = GroupAdapter<ViewHolder>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
//            questionObject = sharedViewModelQuestion.questionObject.value!!

            currentUserObject = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject

            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_opened_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val shareButton = opened_question_share
        saveButton = view.findViewById(R.id.opened_question_save)
        val answerButton = view.findViewById<TextView>(R.id.opened_question_answer_btn)
        val answersRecycler = view.findViewById<RecyclerView>(R.id.opened_question_answers_recycler)
        val openedQuestionTitle = view.findViewById<TextView>(R.id.opened_question_title)
        val openedQuestionContent = view.findViewById<TextView>(R.id.opened_question_content)
        val openedQuestionTimeStamp = view.findViewById<TextView>(R.id.opened_question_timestamp)
        val openedQuestionTags = view.findViewById<TextView>(R.id.opened_question_tags)
        val openedQuestionUpVote = view.findViewById<ImageButton>(R.id.opened_question_upvote)
        val openedQuestionDownVote = view.findViewById<ImageButton>(R.id.opened_question_downvote)
        val openedQuestionVotes = view.findViewById<TextView>(R.id.opened_question_votes)
        val openedQuestionAuthorImage = view.findViewById<ImageView>(R.id.opened_question_author_image)
        val openedQuestionAuthorName = view.findViewById<TextView>(R.id.opened_question_author_name)
        openedQuestionAuthorReputation = view.findViewById(R.id.opened_question_author_reputation)


        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question
                checkIfQuestionSaved(0)

                val stampMills = questionObject.timestamp
                val pretty = PrettyTime()
                val date = pretty.format(Date(stampMills))


                openedQuestionTitle.text = "${questionObject.title}?"
                openedQuestionContent.text = questionObject.details
                openedQuestionTimeStamp.text = date
                openedQuestionTags.text = "tags: ${questionObject.tags.joinToString()}"
                listenToAnswers(questionObject.id)

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
                    openedQuestionAuthorReputation
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
                Glide.with(this).load(user.image).into(openedQuestionAuthorImage)
                openedQuestionAuthorName.text = user.name

                openedQuestionAuthorReputation.text = "(${numberCalculation(user.reputation)})"

                randomUserObject = user // remove later this is just from pasr bad code - is it?
            }
        }
        )



        openedQuestionUpVote.setOnClickListener {
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
                openedQuestionAuthorReputation
            )
        }

        openedQuestionDownVote.setOnClickListener {
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
                openedQuestionAuthorReputation
            )
        }


        saveButton.setOnClickListener {
            checkIfQuestionSaved(1)
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
                override fun onChannelSelected(channelName: String) {}

            })
        }

        answerButton.setOnClickListener {

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.answerFragment).commit()
            activity.subActive = activity.answerFragment
        }

        val answersRecyclerLayoutManager = LinearLayoutManager(this.context)
        answersRecycler.adapter = answersAdapter
        answersRecycler.layoutManager = answersRecyclerLayoutManager
    }
//
//    override fun onDetach() {
//        super.onDetach()
//        sharedViewModelRandomUser.randomUserObject.postValue(Users())
//        sharedViewModelQuestion.questionObject.postValue(Question())
//    }

    private fun listenToAnswers(qId: String) {

        answersAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions/$qId/answers")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleAnswerFromDB = p0.child("body").getValue(Answers::class.java)

                if (singleAnswerFromDB != null) {

                    answersAdapter.add(
                        SingleAnswer(
                            singleAnswerFromDB,
                            currentUserObject, activity as MainActivity
                        )
                    )
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun checkIfQuestionSaved(ranNum: Int) {

        val refCurrentUserSavedQuestions =
            FirebaseDatabase.getInstance().getReference("/users/${currentUserObject.uid}/saved-questions")

        refCurrentUserSavedQuestions.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(questionObject.id)) {
                    if (ranNum == 1) {
                        saveButton.text = getString(R.string.Save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900))

                        refCurrentUserSavedQuestions.child(questionObject.id).removeValue().addOnSuccessListener {
                            changeReputation(
                                11,
                                questionObject.id,
                                questionObject.id,
                                currentUserObject.uid,
                                currentUserObject.name,
                                questionObject.author,
                                openedQuestionAuthorReputation,
                                "questionsave"
                            )
                        }

                    } else {
                        saveButton.text = getString(R.string.Saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700))
                    }


                } else {
                    if (ranNum == 1) {

                        saveButton.text = getString(R.string.Saved)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.green700))

                        val refQuestionInUserSavedQuestions =
                            FirebaseDatabase.getInstance()
                                .getReference("/users/${currentUserObject.uid}/saved-questions")
                        refQuestionInUserSavedQuestions.setValue(mapOf(questionObject.id to true))
                            .addOnSuccessListener {

                                for (t in questionObject.tags) {
                                    val refUserTags = FirebaseDatabase.getInstance()
                                        .getReference("users/${currentUserObject.uid}/interests/$t")
                                    refUserTags.setValue(true)
                                }

                                changeReputation(
                                    10,
                                    questionObject.id,
                                    questionObject.id,
                                    currentUserObject.uid,
                                    currentUserObject.name,
                                    questionObject.author,
                                    openedQuestionAuthorReputation,
                                    "questionsave"
                                )

                            }

                    } else {
                        saveButton.text = getString(R.string.Save)
                        saveButton.setTextColor(ContextCompat.getColor(context!!, R.color.gray900))
                    }
                }
            }
        })
    }
}


class SingleAnswer(
    val answer: Answers,
    val currentUser: Users, val activity: MainActivity
) : Item<ViewHolder>(), DereMethods {

    val commentsAdapter = GroupAdapter<ViewHolder>()
    val imagesAdapter = GroupAdapter<ViewHolder>()

    override fun getLayout(): Int {
        return R.layout.answer_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

//        val uid = FirebaseAuth.getInstance().uid

        val upvote = viewHolder.itemView.findViewById<ImageButton>(R.id.single_answer_upvote)
        val downvote = viewHolder.itemView.findViewById<ImageButton>(R.id.single_answer_downvote)
        val comment = viewHolder.itemView.findViewById<TextView>(R.id.single_answer_comment_button)

        val commentsRecycler = viewHolder.itemView.findViewById<RecyclerView>(R.id.single_answer_comments_recycler)
        val imagesRecycler = viewHolder.itemView.findViewById<RecyclerView>(R.id.single_answer_photos_recycler)

        val commentsLayoutManager = LinearLayoutManager(viewHolder.root.context)
        commentsRecycler.adapter = commentsAdapter
        commentsRecycler.layoutManager = commentsLayoutManager

        val imageLayoutManager = GridLayoutManager(viewHolder.root.context, 4)
        imagesRecycler.adapter = imagesAdapter
        imagesRecycler.layoutManager = imageLayoutManager

        val refAnswerComments = FirebaseDatabase.getInstance()
            .getReference("/questions/${answer.questionId}/answers/${answer.answerId}/comments")

        refAnswerComments.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleCommentFromDB = p0.getValue(AnswerComments::class.java)

                if (singleCommentFromDB != null) {

                    commentsAdapter.add(SingleAnswerComment(singleCommentFromDB))

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

        for (imagePath in answer.photos) {
            val refAnswerImages = FirebaseDatabase.getInstance().getReference("/images/$imagePath/body")
            refAnswerImages.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val imageObject = p0.getValue(Images::class.java)
                    imagesAdapter.add(FeedImage(imageObject!!))
                }

            })

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
            viewHolder.itemView.single_answer_author_reputation
        )


        val ref = FirebaseDatabase.getInstance().getReference("/users/${answer.author}/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)

                if (author != null) {

                    val stampMills = answer.timestamp
                    val pretty = PrettyTime()
                    val date = pretty.format(Date(stampMills))

                    Glide.with(viewHolder.root.context).load(author.image)
                        .into(viewHolder.itemView.single_answer_author_image)
                    viewHolder.itemView.single_answer_author_name.text = author.name
                    viewHolder.itemView.single_answer_content.text = answer.content
                    viewHolder.itemView.single_answer_timestamp.text = date
                    viewHolder.itemView.single_answer_author_reputation.text =
                        "(${numberCalculation(author.reputation)})"
                }
            }
        })


        upvote.setOnClickListener {
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
                viewHolder.itemView.single_answer_author_reputation
            )
        }

        downvote.setOnClickListener {
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
                viewHolder.itemView.single_answer_author_reputation
            )
        }

        comment.setOnClickListener {

            activity.answerObject = answer

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.answerCommentFragment).commit()
            activity.subActive = activity.answerCommentFragment

        }

        imagesAdapter.setOnItemClickListener { item, _ ->

            val imageItem = item as FeedImage

            activity.sharedViewModelImage.sharedImageObject.postValue(imageItem.image)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.imageFullSizeFragment).commit()
            activity.subActive = activity.imageFullSizeFragment
            activity.isOpenedQuestionActive = true
        }
    }
}

class SingleAnswerComment(val comment: AnswerComments) : Item<ViewHolder>(), DereMethods {
    override fun getLayout(): Int {
        return R.layout.answer_comment_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.answer_comment_comment.text = comment.content

        val ref = FirebaseDatabase.getInstance().getReference("/users/${comment.author}/profile")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val author = p0.getValue(Users::class.java)

                if (author != null) {

                    viewHolder.itemView.answer_comment_author_name.text = author.name
                    viewHolder.itemView.answer_comment_author_reputation.text =
                        "(${numberCalculation(author.reputation)})"
                    Glide.with(viewHolder.root.context).load(author.image)
                        .into(viewHolder.itemView.answer_comment_author_image)

                }
            }
        })


    }


}
