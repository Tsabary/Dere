package co.getdere.viewmodels

import androidx.lifecycle.ViewModel
import co.getdere.fragments.SingleTagForList

class SharedViewModelInterests : ViewModel() {
    var interestList = mutableListOf<String>()
}