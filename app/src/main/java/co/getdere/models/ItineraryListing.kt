package co.getdere.models

class ItineraryListing(
    val timestampPublished: Long,
    val timestampUpdated: Long,
    val price: Double,
    val rating: Float,
    val audience: List<Boolean>,
    val video: String,
    val sampleImages: Map<String, Boolean>
) {
    constructor() : this(0, 0, 0.0, 0f, mutableListOf<Boolean>(), "", mapOf())
}
