package co.getdere.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Images(
    val id: String,
    val imageBig: String,
    val imageSmall: String,
    val private: Boolean,
    val photographer: String,
    val link: String,
    val details: String,
    val location: MutableList<Double>,
    val timestamp: Long
) : Parcelable {
    constructor() : this("", "", "", false, "",  "", "", mutableListOf(), 0)
}