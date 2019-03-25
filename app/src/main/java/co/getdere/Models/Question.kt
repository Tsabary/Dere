package co.getdere.Models

class Question(val id : String, val title : String, val details : String, val tags : MutableList<String>, val timestamp: Long, val author : String) {
    constructor() : this("","","", mutableListOf(), 0, "")
}
