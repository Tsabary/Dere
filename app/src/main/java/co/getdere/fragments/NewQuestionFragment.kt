package co.getdere.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import co.getdere.MainActivity
import co.getdere.interfaces.DereMethods
import co.getdere.models.Question
import co.getdere.R
import co.getdere.viewmodels.SharedViewModelQuestion
import co.getdere.viewmodels.SharedViewModelRandomUser
import co.getdere.viewmodels.SharedViewModelTags
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.tobiasschuerg.prefixsuffix.PrefixSuffixEditText
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_new_question.*
import kotlinx.android.synthetic.main.tag_auto_complete.view.*


class NewQuestionFragment : Fragment(), DereMethods {

    lateinit var sharedViewModelTags: SharedViewModelTags
    lateinit var sharedViewModelQuestion: SharedViewModelQuestion
    lateinit var sharedViewModelRandomUser: SharedViewModelRandomUser

    val tagsRef = FirebaseDatabase.getInstance().getReference("/tags")

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    lateinit var questionChipGroup: ChipGroup
    var tagsList: MutableList<String> = mutableListOf()
    lateinit var questionDetails: EditText
    lateinit var questionTitle: PrefixSuffixEditText


    override fun onAttach(context: Context) {
        super.onAttach(context)


        activity?.let {
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
            sharedViewModelQuestion = ViewModelProviders.of(it).get(SharedViewModelQuestion::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(SharedViewModelRandomUser::class.java)
        }



        tagsRef.addChildEventListener(object : ChildEventListener {

            var tags: MutableList<SingleTagForList> = mutableListOf()


            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val tagName = p0.key.toString()

                val count = p0.childrenCount.toInt()

                tags.add(SingleTagForList(tagName, count))

                sharedViewModelTags.tagList = tags

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
        inflater.inflate(R.layout.fragment_new_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        questionTitle = new_question_title
        questionTitle.requestFocus()
        questionDetails = new_question_details
        val questionButton = new_question_btn
        val questionTagsInput = new_question_tag_input
        val addTagButton = new_question_add_tag_button
        questionChipGroup = new_question_chip_group

        questionTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (questionTitle.text!!.isEmpty()) {
                    questionTitle.suffix = ""
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (questionTitle.text!!.isNotEmpty()) {
                    questionTitle.suffix = "?"
                }

            }

        })


        questionButton.setOnClickListener {


            if (questionTitle.text!!.length < 15) {
                Toast.makeText(this.context, "Question title is too short", Toast.LENGTH_SHORT).show()
            } else if (questionDetails.text.isEmpty()) {
                Toast.makeText(this.context, "Please give your question some more details", Toast.LENGTH_SHORT).show()
            } else if (questionChipGroup.childCount == 0) {
                Toast.makeText(this.context, "Please add at least one tag_unactive to your question", Toast.LENGTH_SHORT).show()
            } else {


                for (i in 0 until questionChipGroup.childCount) {
                    val chip = questionChipGroup.getChildAt(i) as Chip
                    tagsList.add(chip.text.toString())
                }



                postQuestion(
                    questionTitle.text.toString(),
                    questionDetails.text.toString(),
                    tagsList,
                    System.currentTimeMillis()
                )


            }

        }


        val tagSuggestionRecycler =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.new_question_tag_recycler)
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsFilteredAdapter



        activity!!.title = "New question"


        questionTagsInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString().toLowerCase()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

//                        tagSuggestionRecycler.visibility = View.VISIBLE
//                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                        var countTagMatches = 0
                        for (i in 0 until questionChipGroup.childCount) {
                            val chip = questionChipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {

                                countTagMatches += 1

                            }

                        }


                        if (countTagMatches == 0) {
                            tagSuggestionRecycler.visibility = View.VISIBLE
                            tagsFilteredAdapter.add(SingleTagSuggestion(t))

                        }


                    }

                }

            }


            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

        })


        addTagButton.setOnClickListener {

            if (questionTagsInput.text.isNotEmpty()){

                var tagsMatchCount = 0

                for (i in 0 until questionChipGroup.childCount) {
                    val chip = questionChipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == questionTagsInput.text.toString()) {
                        tagsMatchCount += 1
                    }
                }

                if (tagsMatchCount == 0) {
                    if (questionChipGroup.childCount < 5) {
                        onTagSelected(questionTagsInput.text.toString().toLowerCase())
                        questionTagsInput.text.clear()
                    } else {
                        Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this.context, "Tag had already been added", Toast.LENGTH_LONG).show()
                }
            }
        }




        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (questionChipGroup.childCount < 5) {
                onTagSelected(row.tag.tagString)
                questionTagsInput.text.clear()
            } else {
                Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
            }
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
            questionChipGroup.removeView(it)
        }

        questionChipGroup.addView(chip)
        questionChipGroup.visibility = View.VISIBLE

    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
//    }


    private fun postQuestion(title: String, details: String, tags: MutableList<String>, timestamp: Long) {

        val activity = activity as MainActivity

        val uid = FirebaseAuth.getInstance().uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("/questions").push()
        val refQuestionMain = FirebaseDatabase.getInstance().getReference("/questions/${ref.key}/main/body")

        val newQuestion = Question(ref.key!!, title, details, tags, timestamp, uid, timestamp)

        sharedViewModelQuestion.questionObject.postValue(newQuestion)
        sharedViewModelRandomUser.randomUserObject.postValue(activity.sharedViewModelCurrentUser.currentUserObject)

        refQuestionMain.setValue(newQuestion)
            .addOnSuccessListener {

                for (t in tags) {
                    val refTag = FirebaseDatabase.getInstance().getReference("/tags/$t/${ref.key}")
                    val refUserTags = FirebaseDatabase.getInstance().getReference("users/$uid/interests/$t")

                    refTag.setValue("question")
                    refUserTags.setValue(true)
                }



                activity.subFm.beginTransaction().hide(activity.subActive).show(activity.openedQuestionFragment)
                    .commit()
                activity.subActive = activity.openedQuestionFragment

                activity.fm.beginTransaction().detach(activity.boardFragment).commit()
                activity.fm.beginTransaction().attach(activity.boardFragment).commit()
                activity.fm.beginTransaction().show(activity.boardFragment).commit()

                questionTitle.text!!.clear()
                questionDetails.text.clear()
                questionChipGroup.removeAllViews()
                tagsList.clear()

                closeKeyboard(activity)

                Log.d("postQuestionActivity", "Saved question to Firebase Database")
//                val action = NewQuestionFragmentDirections.actionDestinationNewQuestionToDestinationBoard()
//                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }


    companion object {
        fun newInstance(): NewQuestionFragment = NewQuestionFragment()
    }

}

class SingleTagSuggestion(val tag: SingleTagForList) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.tag_auto_complete
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tag_name.text = tag.tagString
        viewHolder.itemView.tag_count.text = tag.tagCount.toString()
    }
}

class SingleTagForList(val tagString: String, val tagCount: Int)