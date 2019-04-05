package co.getdere.viewmodels

import androidx.lifecycle.ViewModel

class SharedViewModelFollowedAccounts : ViewModel() {
    var followedAccounts = mutableListOf<String>()
}