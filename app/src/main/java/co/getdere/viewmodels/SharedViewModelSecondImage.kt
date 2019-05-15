package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Images

class SharedViewModelSecondImage: ViewModel() {
    var sharedSecondImageObject = MutableLiveData<Images>()
}