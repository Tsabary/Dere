package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot

class SharedViewModelPurchasedItinerary: ViewModel() {
    var itinerary = MutableLiveData<DataSnapshot>()
}