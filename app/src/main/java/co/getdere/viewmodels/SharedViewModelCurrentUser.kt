package co.getdere.viewmodels

import androidx.lifecycle.ViewModel
import co.getdere.Models.Users

class SharedViewModelCurrentUser : ViewModel() {
    var currentUserObject = Users()
}