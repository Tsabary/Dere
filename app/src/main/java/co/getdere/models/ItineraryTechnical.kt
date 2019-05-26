package co.getdere.models

class ItineraryTechnical(
    val timestampPublished: Long,
    val timestampUpdated: Long,
    val price: Int,
    val rating: Double,
    val video: String,
    val sampleImages: Map<String, Boolean>
) {
    constructor() : this(0, 0, 0, 0.0, "", mapOf())
}
