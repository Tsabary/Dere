package co.getdere.Models

class BoardNotification (val postType : Int, val scenarioType: Int, val initiatorId : String, val initiatorName : String, val mainPostId : String, val specificPostId : String) {
    constructor() : this(0, 0, "", "", "","")
}