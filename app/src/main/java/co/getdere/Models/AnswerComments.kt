package co.getdere.Models

class AnswerComments(val commentId : String, val answerId : String, val questionId : String, val content : String, val timestamp: Long, val author : String) {
    constructor() : this("","","","",0,"")
}
