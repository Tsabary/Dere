package co.getdere.Models

import java.io.File

class ImagesNewWithThumbnail(
    val id: String,
    val imageBig: String,
    val imageSmall: String,
    val photographer: String,
    val link: String,
    val details: String,
    val location: MutableList<Double>,
    val timestamp: Long
) {
    constructor() : this("", "", "", "", "", "", mutableListOf(), 0)
}