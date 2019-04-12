package co.getdere.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Answers(val answerId : String, val questionId : String, val content : String, val timestamp: Long, val author : String, val photos : MutableList<String>) : Parcelable {
    constructor() : this("","","",0,"", mutableListOf())
}
