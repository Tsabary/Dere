package co.getdere.models

class Notification (val postType : Int, val scenarioType: Int, val initiatorId : String, val initiatorName : String, val mainPostId : String, val specificPostId : String, val timestamp : Long, val seen : Int) {
    constructor() : this(0, 0, "", "", "","", 0, 0)
}