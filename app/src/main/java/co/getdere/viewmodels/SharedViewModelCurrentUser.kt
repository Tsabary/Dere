package co.getdere.viewmodels

import androidx.lifecycle.ViewModel
import co.getdere.models.Users

class SharedViewModelCurrentUser : ViewModel() {
    var currentUserObject = Users()
}