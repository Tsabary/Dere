package co.getdere.ViewModels

import androidx.lifecycle.ViewModel
import co.getdere.Models.Users

class SharedViewModelCurrentUser : ViewModel() {
    var currentUserObject = Users()
}