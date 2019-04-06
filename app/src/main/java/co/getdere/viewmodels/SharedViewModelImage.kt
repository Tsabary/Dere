package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Images

class SharedViewModelImage: ViewModel() {
    var sharedImageObject = MutableLiveData<Images>()
}