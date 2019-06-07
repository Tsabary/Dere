package co.getdere.models

class SharedItineraryBody(
    val originalId: String,
    val id: String,
    val public: Boolean,
    val creator: String,
    val contributors: Map<String, Boolean>,
    val title: String,
    val description: String,
    val images: Map<String, Boolean>,
    val days: List<Map<String, Boolean>>,
    val originalImages: Map<String, Boolean>,
    val originalDays: List<Map<String, Boolean>>,
    val startDay: Int,
    val locationId: String,
    val locationName: String,
    val originalPrice: Double
) {
    constructor() : this("", "", false, "", mapOf(), "", "", mapOf(), listOf(), mapOf(), listOf(), 0, "", "", 0.0)
}
