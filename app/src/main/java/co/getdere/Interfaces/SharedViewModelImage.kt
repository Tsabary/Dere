package co.getdere.Interfaces

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.Models.Images

class SharedViewModelImage: ViewModel() {
    var sharedImageObject = MutableLiveData<Images>()
}