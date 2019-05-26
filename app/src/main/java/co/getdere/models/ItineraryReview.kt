package co.getdere.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ItineraryReview(val id: String, val content: String, val rating: Int, val author : String, val timestamp : Long) : Parcelable {
    constructor() : this("", "", 0, "", 0)
}
