package co.getdere.Models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ImageFile (var uri: Uri, var realPath : String) : Parcelable {
    constructor() : this(Uri.parse(""),"")
}