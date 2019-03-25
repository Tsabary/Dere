package co.getdere.Models

class Answers(val answerId : String, val questionId : String, val content : String, val timestamp: Long, val author : String) {
    constructor() : this("","","",0,"")
}
