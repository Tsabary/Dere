package co.getdere.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Users (val uid : String, val name : String, val email : String, val image : String) : Parcelable {

    constructor() : this("", "", "", "")
}
