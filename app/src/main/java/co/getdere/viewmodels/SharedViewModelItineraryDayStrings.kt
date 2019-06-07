package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.fragments.SingleTagForList
import co.getdere.models.Images
import com.google.firebase.database.DataSnapshot

class SharedViewModelItineraryDayStrings : ViewModel() {
    var daysList = MutableLiveData<MutableList<MutableMap<String, Boolean>>>()
}