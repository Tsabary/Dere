package co.getdere.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.Models.Question

class SharedViewModelQuestion: ViewModel() {
    var questionObject = MutableLiveData<Question>()
}