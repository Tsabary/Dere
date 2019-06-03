package co.getdere.models

class ItineraryInformational(
    val authorKnowledge : Int,
    val aboutAuthor : String,
    val aboutFood : String,
    val aboutNightlife : String,
    val aboutNature : String,
    val aboutActivities : String,
    val aboutAccommodation : String,
    val aboutTransportation : String) {
    constructor() : this(0, "", "", "", "", "", "", "")
}
