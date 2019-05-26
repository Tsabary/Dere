package co.getdere.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import co.getdere.MainActivity

import co.getdere.R
import co.getdere.models.Users
import co.getdere.viewmodels.SharedViewModelCurrentUser
import co.getdere.viewmodels.SharedViewModelTags
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_edit_interests.*


class EditInterestsFragment : Fragment() {

    lateinit var currentUser: Users
    lateinit var sharedViewModelTags: SharedViewModelTags

    lateinit var chipGroup: ChipGroup
    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    var tagsInChipGroup = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_interests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            sharedViewModelTags = ViewModelProviders.of(it).get(SharedViewModelTags::class.java)
            currentUser = ViewModelProviders.of(it).get(SharedViewModelCurrentUser::class.java).currentUserObject
        }

        chipGroup = edit_interests_chipgroup
        val searchInput = edit_interests_search_input
        val tagSuggestionRecycler = edit_interests_recycler
        tagSuggestionRecycler.adapter = tagsFilteredAdapter
        tagSuggestionRecycler.layoutManager = LinearLayoutManager(this.context)



        val interestsRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/interests")

        interestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                for (interest in p0.children) {
                    tagsInChipGroup.add(interest.key!!)
                    populateChipGroup()
                }
            }
        })


        searchInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()
                val userInput = s.toString().toLowerCase().replace(" ","-")
                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE
                } else {
                    tagSuggestionRecycler.visibility = View.VISIBLE

                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

                        var countTagMatches = 0
                        for (i in 0 until chipGroup.childCount) {
                            val chip = chipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {
                                countTagMatches += 1
                            }
                        }

                        if (countTagMatches == 0) {
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

        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            tagsInChipGroup.add(row.tag.tagString)
            populateChipGroup()
            val tagRef =
                FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/interests/${row.tag.tagString}")
            tagRef.setValue(true)
            searchInput.text.clear()
            tagSuggestionRecycler.visibility = View.GONE

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("interests_edited", null)
        }

    }

    private fun populateChipGroup() {
        chipGroup.removeAllViews()
        tagsInChipGroup.sortBy { it }
        for (tag in tagsInChipGroup) {
            onTagSelected(tag)
        }
    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.white)
        chip.chipStrokeWidth = 1f
        chip.setChipStrokeColorResource(R.color.gray500)
        chip.setCloseIconTintResource(R.color.gray500)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chipGroup.addView(chip)
        chipGroup.visibility = View.VISIBLE
        chip.setOnCloseIconClickListener {
            tagsInChipGroup.remove(chip.text!!)
            chipGroup.removeView(it)
            val tagRef = FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}/interests/${chip.text}")
            tagRef.removeValue()
            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("interests_edited", null)
        }
    }


}
