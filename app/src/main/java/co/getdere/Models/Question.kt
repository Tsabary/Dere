package co.getdere.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Question(val id : String, val title : String, val details : String, val tags : MutableList<String>, val timestamp: Long, val author : String, val lastInteraction : Long) : Parcelable {
    constructor() : this("","","", mutableListOf(), 0, "", 0)
}
