package co.getdere.models

class NotificationBoard (val postType : Int, val scenarioType: Int, val initiatorId : String, val initiatorName : String, val initiatorImage : String, val mainPostId : String, val specificPostId : String, val timestamp : Long, val seen : Int) {
    constructor() : this(0, 0, "", "", "","","", 0, 0)
}