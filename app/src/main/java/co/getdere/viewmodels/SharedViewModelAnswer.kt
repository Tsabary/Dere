package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Answers
import co.getdere.models.Images

class SharedViewModelAnswer: ViewModel() {
    var sharedAnswerObject = MutableLiveData<Answers>()
}