package co.getdere.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Answers(val answerId : String, val questionId : String, val content : String, val timestamp: Long, val author : String) : Parcelable {
    constructor() : this("","","",0,"")
}
