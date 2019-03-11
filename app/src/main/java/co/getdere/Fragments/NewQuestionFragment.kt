package co.getdere.Fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import co.getdere.MainActivity
import co.getdere.Models.Question
import co.getdere.Models.Tag
import co.getdere.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.tag_auto_complete.view.*


class NewQuestionFragment : Fragment() {

    val tags = listOf(
        "John Smith",
        "Kate Eckhart",
        "Emily Sun",
        "Frodo Baggins",
        "John Smith",
        "Kate Eckhart",
        "Emily Sun",
        "Frodo Baggins"
    )

    val tagsAdapter = GroupAdapter<ViewHolder>()
    val tagsFiltredAdapter = GroupAdapter<ViewHolder>()
    lateinit var questionChipGroup: ChipGroup
    lateinit var tagsList: MutableList<String>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainActivity = activity as MainActivity

        val myView = inflater.inflate(R.layout.fragment_new_question, container, false)


//        val adapter = ArrayAdapter<String>(mainActivity, android.R.layout.simple_dropdown_item_1line, tags)


//
//        myView.recyclerView.setAdapter(ArrayAdapter<String>(this.context))
//        myView.autoCompleteTextView.setOnItemClickListener { parent, arg1, position, arg3 ->
//            myView.autoCompleteTextView.text = null
//            val selected = parent.getItemAtPosition(position) as String
//            addChipToGroup(selected, myView.chipGroup2)
//        }

        return myView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionTitle: EditText = view.findViewById(R.id.new_question_title)
        val questionDetails = view.findViewById<EditText>(R.id.new_question_details)
        val questionTags = view.findViewById<EditText>(R.id.new_question_tags)
        val questionButton = view.findViewById<Button>(R.id.new_question_btn)
        val questionTagsInput = view.findViewById<EditText>(R.id.new_question_tag_input)
        questionChipGroup = view.findViewById<ChipGroup>(R.id.new_question_chip_group)

        val tagSuggestionRecycler =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.new_question_recycler)
        val tagSuggestionLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.layoutManager = tagSuggestionLayoutManager
        tagSuggestionRecycler.adapter = tagsAdapter

        for (t in tags) {
            tagsAdapter.add(singleTagSuggestion(t))
        }

        questionTagsInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val userInput = s.toString()
                val relevantTags: List<String> = tags.filter { it.contains(userInput) }
                if (userInput == "") {
                    tagSuggestionRecycler.adapter = tagsAdapter
                } else {
                    for (t in relevantTags) {
                        tagsFiltredAdapter.clear()
                        tagsFiltredAdapter.add(singleTagSuggestion(t))
                    }
                    tagSuggestionRecycler.adapter = tagsFiltredAdapter
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

        })


        questionButton.setOnClickListener {

            for (item in questionChipGroup){

            }





            postQuestion(
                questionTitle.text.toString(),
                questionDetails.text.toString(),
                questionTags.text.toString(),
                System.currentTimeMillis().toString()
            )

        }

        tagsAdapter.setOnItemClickListener { item, view2 ->
            val row = item as singleTagSuggestion

            onTagSelected(row.tagString)
        }

    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.setText(selectedTag)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }


    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

    private fun postQuestion(title: String, details: String, tags: MutableList<String>, timestamp: String) {

        val uid = FirebaseAuth.getInstance().uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("/questions").push()

        val newQuestion = Question(ref.key!!, title, details, tags, timestamp, uid)

        ref.setValue(newQuestion)
            .addOnSuccessListener {
                Log.d("postQuestionActivity", "Saved question to Firebase Database")
                val action = NewQuestionFragmentDirections.actionDestinationNewQuestionToDestinationBoard()
                findNavController().navigate(action)


            }.addOnFailureListener {
                Log.d("postQuestionActivity", "Failed to save question to database")
            }
    }

//
//    private fun addChipToGroup(person: String, chipGroup: ChipGroup) {
//        val chip = Chip(context)
//        chip.text = person
//        chip.isCloseIconEnabled = true
//        chip.setChipIconTintResource(R.color.main_green)
//
//        // necessary to get single selection working
//        chip.isClickable = true
//        chip.isCheckable = false
//        chipGroup.addView(chip as View)
//        chip.setOnCloseIconClickListener { chipGroup.removeView(chip as View) }
}


//}

class singleTagSuggestion(val tagString: String) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.tag_auto_complete
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.one_tag.text = tagString
    }


}