package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Images
import co.getdere.models.Itineraries
import com.google.firebase.database.DataSnapshot

class SharedViewModelItinerary: ViewModel() {
    var itinerary = MutableLiveData<Itineraries>()
}