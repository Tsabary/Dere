package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot

class SharedViewModelItinerary: ViewModel() {
    var itinerary = MutableLiveData<DataSnapshot>()
}