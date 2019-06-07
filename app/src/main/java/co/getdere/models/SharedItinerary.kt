package co.getdere.models

class SharedItinerary(
    val id: String,
    val contributors: Map<String, Boolean>,
    val images: Map<String, Boolean>,
    val days: List<Map<String, Boolean>>,
    val startDay: Int
) {
    constructor() : this("", mapOf(),  mapOf(), listOf(), 0)
}
