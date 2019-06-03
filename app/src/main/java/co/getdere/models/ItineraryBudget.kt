package co.getdere.models

class ItineraryBudget(
    val budget: Int,
    val food: Boolean,
    val nightlife: Boolean,
    val activities : Boolean,
    val transportation : Boolean,
    val accommodation : Boolean,
    val other : String
    ) {
    constructor() : this(0, false, false, false, false, false, "")
}
