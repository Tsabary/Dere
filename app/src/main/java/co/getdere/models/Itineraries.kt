package co.getdere.models

class Itineraries(
    val id: String,
    val publishedStatus: Boolean,
    val creator: String,
    val title: String,
    val description: String,
    val location: String,
    val price: Double,
    val coverImage: String,
    val video: String,
    val images: Map<String, Boolean>,
    val sampleImages: Map<String, Boolean>,
    val timestampPublished: Long,
    val timestampUpdated: Long
) {
    constructor() : this("", false, "", "", "", "", 0.0, "", "", mapOf(), mapOf(), 0, 0)
}
