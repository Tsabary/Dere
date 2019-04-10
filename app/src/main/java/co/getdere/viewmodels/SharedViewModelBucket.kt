package co.getdere.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.getdere.models.Images
import com.google.firebase.database.DataSnapshot

class SharedViewModelBucket: ViewModel() {
    var sharedBucketObject = MutableLiveData<DataSnapshot>()
}