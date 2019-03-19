package co.getdere.ViewModels

import androidx.lifecycle.ViewModel
import co.getdere.Fragments.SingleTagForList
import co.getdere.Models.Users

class SharedViewModelTags : ViewModel() {
    lateinit var tagList : MutableList<SingleTagForList>
}