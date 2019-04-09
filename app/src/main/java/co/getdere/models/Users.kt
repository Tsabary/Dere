package co.getdere.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Users (val uid : String, val name : String, val email : String, val image : String, val reputation : Long, val tagline : String, val joinDate : Long) : Parcelable {

    constructor() : this("", "", "", "",0, "", 0)
}
