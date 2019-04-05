package co.getdere.viewmodels

import androidx.lifecycle.ViewModel
import co.getdere.Fragments.SingleTagForList

class SharedViewModelTags : ViewModel() {
    var tagList = mutableListOf<SingleTagForList>()
}