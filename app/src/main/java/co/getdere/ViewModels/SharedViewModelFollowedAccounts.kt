package co.getdere.ViewModels

import androidx.lifecycle.ViewModel
import co.getdere.Fragments.SingleTagForList
import co.getdere.Models.Users

class SharedViewModelFollowedAccounts : ViewModel() {
    var followedAccounts = mutableListOf<String>()
}