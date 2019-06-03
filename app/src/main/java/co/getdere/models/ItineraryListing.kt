package co.getdere.models

class ItineraryListing(
    val timestampPublished: Long,
    val timestampUpdated: Long,
    val price: Int,
    val rating: Float,
    val audience : List<Boolean>,
    val lengthMin : Int,
    val lengthMax : Int,
    val video: String,
    val sampleImages: Map<String, Boolean>
) {
    constructor() : this(0, 0, 0, 0f, mutableListOf<Boolean>(),0,0,"", mapOf())
}
