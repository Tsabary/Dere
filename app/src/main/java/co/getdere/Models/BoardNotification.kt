package co.getdere.Models

//class BoardNotification (val initiator : String, val receiver : String, val questionId : String, val content : String) {
//}

class BoardNotification (val contentDecider : Int, val initiatorName : String, val postId : String, val initiatorId : String) {
    constructor() : this(0,"", "", "")
}