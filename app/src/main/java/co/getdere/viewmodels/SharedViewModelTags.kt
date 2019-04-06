package co.getdere.viewmodels

import androidx.lifecycle.ViewModel
import co.getdere.fragments.SingleTagForList

class SharedViewModelTags : ViewModel() {
    var tagList = mutableListOf<SingleTagForList>()
}