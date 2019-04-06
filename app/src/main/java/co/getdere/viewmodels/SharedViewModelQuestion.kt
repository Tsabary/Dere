package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Question

class SharedViewModelQuestion: ViewModel() {
    var questionObject = MutableLiveData<Question>()
}