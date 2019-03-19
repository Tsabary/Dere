package co.getdere.Models

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
) {
    constructor() : this("", "", "", false, "",  "", "", mutableListOf(), 0)
}