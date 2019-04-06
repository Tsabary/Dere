package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Users

class SharedViewModelRandomUser : ViewModel() {
    var randomUserObject = MutableLiveData<Users>()
}