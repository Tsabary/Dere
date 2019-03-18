package co.getdere.Interfaces

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.Models.Users

class SharedViewModelRandomUser : ViewModel() {
    var randomUserObject = MutableLiveData<Users>()
}