package co.getdere.models

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
    val timestampTaken: Long,
    val timestampUpload : Long,
    val tags : MutableList<String>
) : Parcelable {
    constructor() : this("", "", "", false, "",  "", "", mutableListOf(32.018374, 34.829601), 0, 0,  mutableListOf())
}