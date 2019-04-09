package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.roomclasses.LocalImagePost

class SharedViewModelLocalImagePost: ViewModel() {
    var sharedImagePostObject = MutableLiveData<LocalImagePost>()
}