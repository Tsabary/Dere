package co.getdere.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.Models.Users

class SharedViewModelNewImageDetails : ViewModel() {
    var imageDescription = MutableLiveData<String>()
    var imageUri = MutableLiveData<String>()
}