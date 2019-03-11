package co.getdere.Models

import java.util.*

class Question(val id : String, val title : String, val details : String, val tags : MutableList<String>, val timestamp: String, val author : String) {
    constructor() : this("","","", mutableListOf(""), "", "")
}
