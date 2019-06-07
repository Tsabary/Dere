package co.getdere.models

class ItineraryBody(
    val id: String,
    val public: Boolean,
    val creator: String,
    val title: String,
    val description: String,
    val images: Map<String, Boolean>,
    val days: List<Map<String, Boolean>>,
    val startDay : Int,
    val locationId: String,
    val locationName: String
) {
    constructor() : this("", false, "", "", "", mapOf(), listOf(),0, "","")
}
