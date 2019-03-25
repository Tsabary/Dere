package co.getdere.ViewModels

import androidx.lifecycle.ViewModel
import co.getdere.Fragments.SingleTagForList
import co.getdere.Models.Users

class SharedViewModelTags : ViewModel() {
    var tagList = mutableListOf<SingleTagForList>()
}