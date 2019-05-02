package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.getdere.MainActivity
import co.getdere.R
import co.getdere.groupieAdapters.SingleQuestion
import co.getdere.models.Question
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelInterests
import co.getdere.viewmodels.SharedViewModelQuestion
import co.getdere.viewmodels.SharedViewModelRandomUser
import co.getdere.viewmodels.SharedViewModelTags
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_toolbar.*
import kotlinx.android.synthetic.main.fragment_board.*

class BoardFragment : Fragment() {

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    val searchedQuestionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    lateinit var questionRecyclerLayoutManager : androidx.recyclerview.widget.LinearLayoutManager

    lateinit var sharedViewModelForQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser
    lateinit var sharedViewModelInterests: SharedViewModelInterests
    lateinit var sharedViewModelTags: SharedViewModelTags

    private lateinit var questionsRecycler: RecyclerView
    private lateinit var searchedQuestionsRecycler: RecyclerView

    var interestsList: MutableList<String> = mutableListOf()

    private lateinit var boardFilterChipGroup: ChipGroup

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            sharedViewModelForQuestion.questionObject.postValue(Question())

            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(SharedViewModelInterests::class.java)

            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)

        }

        val uid = FirebaseAuth.getInstance().uid

        val tagsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/interests")

        tagsRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val tagName = p0.key.toString()

                interestsList.add(tagName)

                sharedViewModelInterests.interestList = interestsList
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationBadge = board_toolbar_notifications_badge
        val activity = activity as MainActivity

        activity.boardNotificationsCount.observe(this, Observer {
            it?.let { notCount ->
                notificationBadge.setNumber(notCount)
            }
        })

        val boardSearchBox = board_toolbar_search_box
        val tagSuggestionRecycler = board_search_recycler
        boardFilterChipGroup = board_toolbar_filter_chipgroup
        val fab: FloatingActionButton = board_fab

        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

        board_swipe_refresh.setOnRefreshListener {
            listenToQuestions()
            board_swipe_refresh.isRefreshing = false
        }


        val boardNotificationIcon = board_toolbar_notifications_icon
        val boardSavedQuestionIcon = board_toolbar_saved_questions_icon

        notificationBadge.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.boardNotificationsFragment)
                .commit()
            activity.subActive = activity.boardNotificationsFragment

            activity.switchVisibility(1)
            activity.isBoardNotificationsActive = true
            activity.boardNotificationsFragment.listenToNotifications()
        }

        boardNotificationIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.boardNotificationsFragment)
                .commit()
            activity.subActive = activity.boardNotificationsFragment

            activity.switchVisibility(1)
            activity.isBoardNotificationsActive = true
            activity.boardNotificationsFragment.listenToNotifications()
        }

        boardSavedQuestionIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.savedQuestionFragment).commit()
            activity.subActive = activity.savedQuestionFragment

            activity.switchVisibility(1)
        }

        fab.setOnClickListener {

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.newQuestionFragment).commit()
            activity.subActive = activity.newQuestionFragment

            activity.switchVisibility(1)
        }

        searchedQuestionsRecycler = board_question_feed_search
        val searchedQuestionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        searchedQuestionRecyclerLayoutManager.reverseLayout = true
        searchedQuestionsRecycler.adapter = searchedQuestionsRecyclerAdapter
        searchedQuestionsRecycler.layoutManager = searchedQuestionRecyclerLayoutManager


        questionsRecycler = board_question_feed
        questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionRecyclerLayoutManager.reverseLayout = true
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        listenToQuestions()
        recyclersVisibility(0)

        boardSearchBox.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {

                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {
                        tagSuggestionRecycler.visibility = View.VISIBLE
                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                    }

                }

            }


            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

        })

        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (boardFilterChipGroup.childCount == 0) {
                onTagSelected(row.tag.tagString)
                boardSearchBox.text.clear()
                searchQuestions(row.tag.tagString)
                recyclersVisibility(1)
            } else {
                Toast.makeText(this.context, "You can only search one tag_unactive at a time", Toast.LENGTH_LONG).show()
            }
        }


        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleQuestion
            val author = row.question.author
            val question = row.question


            sharedViewModelForQuestion.questionObject.postValue(question)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment).commit()

            activity.switchVisibility(1)

            activity.subActive = activity.openedQuestionFragment


            // meanwhile in the background it will load the random user object

            val refRandomUser = FirebaseDatabase.getInstance().getReference("/users/$author/profile")

            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val randomUserFromDB = p0.getValue(Users::class.java)

                    sharedViewModelRandomUser.randomUserObject.postValue(randomUserFromDB)
                }

            })
        }



        searchedQuestionsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val row = item as SingleQuestion
            val author = row.question.author
            val question = row.question


            sharedViewModelForQuestion.questionObject.postValue(question)

            activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment).commit()

            activity.switchVisibility(1)

            activity.subActive = activity.openedQuestionFragment


            // meanwhile in the background it will load the random user object

            val refRandomUser = FirebaseDatabase.getInstance().getReference("/users/$author/profile")

            refRandomUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    val randomUserFromDB = p0.getValue(Users::class.java)

                    sharedViewModelRandomUser.randomUserObject.postValue(randomUserFromDB)
                }

            })
        }

    }

    private fun recyclersVisibility(case: Int) {

        if (case == 0) {
            searchedQuestionsRecycler.visibility = View.GONE
            questionsRecycler.visibility = View.VISIBLE
        } else {
            searchedQuestionsRecycler.visibility = View.VISIBLE
            questionsRecycler.visibility = View.GONE
        }

    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.green700)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            boardFilterChipGroup.removeView(it)
            recyclersVisibility(0)
        }

        boardFilterChipGroup.addView(chip)
        boardFilterChipGroup.visibility = View.VISIBLE

    }


    private fun searchQuestions(searchTag: String) {

        searchedQuestionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val singleQuestionFromDB = p0.child("main").child("body").getValue(Question::class.java)


                if (singleQuestionFromDB != null) {


                    runThroughTags@ for (tag in singleQuestionFromDB.tags) {

                        if (searchTag == tag) {
                            searchedQuestionsRecyclerAdapter.add(SingleQuestion(singleQuestionFromDB))
                            break@runThroughTags
                        }
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


    fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/questions")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                for (i in p0.children) {
                    val singleQuestionFromDB = i.child("main").child("body").getValue(Question::class.java)

                    if (singleQuestionFromDB != null) {

                        singleQuestionLoop@ for (interest in interestsList) {

                            for (tag in singleQuestionFromDB.tags) {

                                if (interest == tag) {
                                    questionsRecyclerAdapter.add(SingleQuestion(singleQuestionFromDB))
                                    questionRecyclerLayoutManager
                                    break@singleQuestionLoop
                                }
                            }
                        }
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

}